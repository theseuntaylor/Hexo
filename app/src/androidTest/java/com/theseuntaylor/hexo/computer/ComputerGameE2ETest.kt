package com.theseuntaylor.hexo.computer

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.services.storage.TestStorage
import com.theseuntaylor.hexo.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * End-to-end tests for "Play vs Computer", driven through the real app UI.
 *
 * Ways this feature can fail (written before the implementation):
 *
 *  Devilish ("the house always wins")
 *   1. The human wins a game.
 *   2. A game ends in a draw, or the human is left with no empty square to play.
 *   3. A game never ends because the house keeps overwriting squares in a loop.
 *   4. The house returns an invalid move: outside the board, or a square it already owns.
 *   5. The house changes more than one square in a single turn.
 *   6. The house overwrites a mark without telling the player.
 *   7. Solving the game takes long enough to freeze the UI before the first move.
 *  14. The house wins so fast the game is over before it starts (before the human's 4th move).
 *  15. The house never lets the human get close to winning, so there's no hope to take away.
 *
 *  Every level
 *   8. Easy, Medium or Hard overwrite a human mark.
 *   9. A level misses an immediate win, or Medium/Hard fail to block an immediate loss.
 *  10. Hard is unbeatable (it is meant to slip up occasionally).
 *  11. The player can move while the computer is thinking, or after the game is over.
 *  12. The status text disagrees with the board (e.g. "You Win!" while O has a line).
 *  13. "Play Again" leaves stale state behind (old marks, cheat banner, wrong turn).
 *
 * 1-5, 7-10, 14-15 are proven exhaustively in [ComputerOpponentExhaustiveTest]; this class
 * covers 1, 2, 5, 6, 8, 11, 12, 13, 14 on the real screens.
 *
 * Artifacts (game logs + screenshots) are written via TestStorage and pulled by Gradle into
 * app/build/outputs/connected_android_test_additional_output/.
 */
@RunWith(AndroidJUnit4::class)
class ComputerGameE2ETest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val storage = TestStorage()
    private val log = StringBuilder()

    @Test
    fun devilishHouseWinsEveryGameAndAnnouncesItsCheats() {
        log.appendLine("# Devilish: the house always wins\n")
        openLevel("Devilish")
        screenshot("devilish-start")

        var cheatsSeen = 0
        try {
            repeat(DEVILISH_GAMES) { game ->
                val strategy = if (game % 2 == 0) "greedy" else "random(seed=$game)"
                val rng = Random(game)
                val result = playGame(
                    level = "Devilish",
                    strategyName = strategy,
                    screenshotFirstCheat = cheatsSeen == 0,
                ) { board -> if (game % 2 == 0) greedyMove(board, opening = game / 2) else randomMove(board, rng) }

                assertEquals("game ${game + 1}: failures 1/2", "The House Wins!", result.status)
                assertTrue("game ${game + 1} lasted ${result.humanMoves} moves (failure 14)", result.humanMoves >= 4)
                cheatsSeen += result.cheats
                if (game == 0) screenshot("devilish-house-wins")

                playAgainAndAssertFreshBoard()
            }
            log.appendLine("\n**Result:** house won $DEVILISH_GAMES/$DEVILISH_GAMES games, cheated $cheatsSeen times.")
            assertTrue("the house should have cheated at least once across $DEVILISH_GAMES games", cheatsSeen > 0)
        } finally {
            writeLog("devilish-games.md")
        }
    }

    @Test
    fun easyMediumAndHardPlayFair() {
        log.appendLine("# Easy, Medium, Hard: fair play\n")
        try {
            for (level in listOf("Easy", "Medium", "Hard")) {
                openLevel(level)
                repeat(FAIR_GAMES_PER_LEVEL) { game ->
                    val rng = Random(game)
                    val result = playGame(level, if (game % 2 == 0) "greedy" else "random(seed=$game)") { board ->
                        if (game % 2 == 0) greedyMove(board) else randomMove(board, rng)
                    }
                    assertEquals("$level must never overwrite (failure 8)", 0, result.cheats)
                    playAgainAndAssertFreshBoard()
                }
                composeRule.onNodeWithText("Back to Menu").performClick()
            }
        } finally {
            writeLog("fair-games.md")
        }
    }

    @Test
    fun playerCannotMoveDuringComputerTurnOrAfterGameOver() {
        log.appendLine("# Input locking\n")
        try {
            openLevel("Medium")

            // Hammer the board while the computer is thinking.
            cell(0).performClick()
            assertTrue(statusText() in THINKING)
            cell(1).performClick()
            cell(2).performClick()
            waitForComputer()
            val board = readBoard()
            assertEquals("only one human move should land (failure 11): $board", 1, board.count { it == "X" })
            assertEquals("computer should answer exactly once: $board", 1, board.count { it == "O" })
            log.appendLine("- Tapped 3 squares during the computer's turn; board after: `${board.pretty()}`")

            // Finish the game, then try to keep playing.
            playGame("Medium", "greedy") { greedyMove(it) }
            val finished = readBoard()
            val finishedStatus = statusText()
            finished.indices.filter { finished[it].isEmpty() }.forEach { cell(it).performClick() }
            assertEquals("board must not change after game over (failure 11)", finished, readBoard())
            assertEquals(finishedStatus, statusText())
            log.appendLine("- Tapped every empty square after \"$finishedStatus\"; board unchanged.")

            playAgainAndAssertFreshBoard()
            log.appendLine("- Play Again reset the board.")
        } finally {
            writeLog("input-locking.md")
        }
    }

    // ── Game driver ─────────────────────────────────────────────────────────

    private data class GameResult(val status: String, val cheats: Int, val humanMoves: Int)

    private fun playGame(
        level: String,
        strategyName: String,
        screenshotFirstCheat: Boolean = false,
        humanMove: (List<String>) -> Int,
    ): GameResult {
        val isDevilish = level == "Devilish"
        var cheats = 0
        var humanMoves = 0
        var shotTaken = false
        log.appendLine("## $level vs $strategyName")

        while (statusText() == "Your Turn") {
            val index = humanMove(readBoard())
            cell(index).performClick()
            humanMoves++
            val afterHuman = readBoard()
            assertEquals("human move at $index should land", "X", afterHuman[index])
            log.append("- You → $index")

            if (statusText() in THINKING) {
                waitForComputer()
                val afterComputer = readBoard()
                val changed = afterHuman.indices.filter { afterHuman[it] != afterComputer[it] }
                assertEquals("computer must change exactly one square (failure 5): $changed", 1, changed.size)
                val target = changed.single()
                assertEquals("O", afterComputer[target])

                if (afterHuman[target] == "X") {
                    cheats++
                    assertTrue("$level overwrote square $target (failure 8)", isDevilish)
                    composeRule.onNodeWithTag("cheat_banner").assertExists("cheat was not announced (failure 6)")
                    log.append(", house OVERWRITES $target")
                    if (screenshotFirstCheat && !shotTaken) {
                        screenshot("devilish-cheat")
                        shotTaken = true
                    }
                } else {
                    log.append(", computer → $target")
                }
            }
            log.appendLine("  `${readBoard().pretty()}`")
            assertStatusMatchesBoard(isDevilish)
        }

        val status = statusText()
        log.appendLine("- **$status** (overwrites: $cheats)\n")
        return GameResult(status, cheats, humanMoves)
    }

    private fun assertStatusMatchesBoard(isDevilish: Boolean) {
        val board = readBoard()
        val expected = when {
            hasLine(board, "X") -> "You Win!"
            hasLine(board, "O") -> if (isDevilish) "The House Wins!" else "Computer Wins!"
            board.none { it.isEmpty() } -> "It's a Draw!"
            else -> "Your Turn"
        }
        assertEquals("status disagrees with board ${board.pretty()} (failure 12)", expected, statusText())
    }

    private fun playAgainAndAssertFreshBoard() {
        composeRule.onNodeWithText("Play Again").performClick()
        assertEquals("board should be empty after Play Again (failure 13)", List(9) { "" }, readBoard())
        assertEquals("Your Turn", statusText())
        composeRule.onNodeWithTag("cheat_banner").assertDoesNotExist()
    }

    // ── Human strategies ────────────────────────────────────────────────────

    /** Wins if it can, blocks if it must, otherwise centre → corners → edges (rotated by [opening]). */
    private fun greedyMove(board: List<String>, opening: Int = 0): Int {
        val preference = listOf(4, 0, 2, 6, 8, 1, 3, 5, 7).let { it.drop(opening % 9) + it.take(opening % 9) }
        return completingMove(board, "X") ?: completingMove(board, "O") ?: preference.first { board[it].isEmpty() }
    }

    private fun randomMove(board: List<String>, rng: Random): Int =
        board.indices.filter { board[it].isEmpty() }.random(rng)

    private fun completingMove(board: List<String>, symbol: String): Int? =
        LINES.firstNotNullOfOrNull { line ->
            val empty = line.filter { board[it].isEmpty() }
            if (empty.size == 1 && line.count { board[it] == symbol } == 2) empty.single() else null
        }

    private fun hasLine(board: List<String>, symbol: String) = LINES.any { line -> line.all { board[it] == symbol } }

    // ── UI helpers ──────────────────────────────────────────────────────────

    private fun openLevel(level: String) {
        composeRule.onNodeWithText("Play vs Computer").performClick()
        composeRule.onNodeWithText("Choose Your").assertExists()
        composeRule.onNodeWithText(level).performClick()
        composeRule.waitUntil(5_000) { runCatching { statusText() == "Your Turn" }.getOrDefault(false) }
    }

    private fun cell(index: Int) = composeRule.onNodeWithTag("cell_$index")

    private fun readBoard(): List<String> = (0..8).map { index ->
        cell(index).fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
            ?.joinToString("") { it.text }.orEmpty()
    }

    private fun statusText(): String =
        composeRule.onNodeWithTag("game_status").fetchSemanticsNode().config
            .getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text }.orEmpty()

    private fun waitForComputer() {
        composeRule.waitUntil(COMPUTER_TIMEOUT_MS) { statusText() !in THINKING }
    }

    private fun List<String>.pretty() = chunked(3).joinToString(" | ") { row -> row.joinToString("") { it.ifEmpty { "·" } } }

    // ── Artifacts ───────────────────────────────────────────────────────────

    private fun screenshot(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        storage.openOutputFile("computer-opponent/screens/$name.png").use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun writeLog(name: String) {
        storage.openOutputFile("computer-opponent/$name").use { it.write(log.toString().toByteArray()) }
    }

    private companion object {
        const val DEVILISH_GAMES = 8
        const val FAIR_GAMES_PER_LEVEL = 4
        const val COMPUTER_TIMEOUT_MS = 5_000L
        val THINKING = setOf("Computer is thinking…", "The House is plotting…")
        val LINES = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6),
        )
    }
}
