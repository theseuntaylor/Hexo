package com.theseuntaylor.hexo.computer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.services.storage.TestStorage
import com.theseuntaylor.hexo.feature.computer.ComputerOpponent
import com.theseuntaylor.hexo.feature.computer.DevilishStrategy
import com.theseuntaylor.hexo.feature.computer.Difficulty
import com.theseuntaylor.hexo.feature.game.CellValue
import com.theseuntaylor.hexo.feature.game.TicTacToeLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * Plays every level against every possible sequence of human moves (the human always
 * starts as X) and checks failure modes 1-5, 7-10 and 14-15 listed in [ComputerGameE2ETest].
 *
 * Writes computer-opponent/exhaustive-report.md via TestStorage.
 */
@RunWith(AndroidJUnit4::class)
class ComputerOpponentExhaustiveTest {

    private class Tally {
        var games = 0
        var humanWins = 0
        var computerWins = 0
        var draws = 0
        var gamesWithOverwrites = 0
        var shortestGame = Int.MAX_VALUE
        var longestGame = 0
        val lengths = sortedMapOf<Int, Int>()
        var hopeOffered = 0
        var humanGotOneAway = 0
        var winStolen = 0
    }

    private enum class Outcome { HUMAN, COMPUTER, DRAW }

    /** What happened along one line of play. */
    private data class Path(
        val humanMoves: Int = 0,
        val overwrites: Int = 0,
        /** After some house move, the human had a move that would leave them one square from winning. */
        val hopeOffered: Boolean = false,
        /** The human actually got two in a line with the third square open. */
        val oneAway: Boolean = false,
    )

    @Test
    fun everyLevelAgainstEveryHumanStrategy() {
        val report = StringBuilder("# Computer opponent: exhaustive check\n\n")
        report.appendLine("Every possible sequence of human moves was played against each level (human = X, moves first).\n")

        val solveMs = measureTimeMillis {
            DevilishStrategy.solve()
            DevilishStrategy.planWindow()
        }
        report.appendLine("Devilish solve time on this device: **$solveMs ms**\n")

        report.appendLine("| Level | Games | Human wins | Computer wins | Draws | Games with overwrites | Game length (human moves) |")
        report.appendLine("|---|---|---|---|---|---|---|")

        val tallies = Difficulty.entries.associateWith { level ->
            Tally().also { tally ->
                explore(level, Random(42), tally, List(9) { CellValue.Empty }, Path())
                report.appendLine(
                    "| ${level.label} | ${tally.games} | ${tally.humanWins} | ${tally.computerWins} | ${tally.draws} " +
                        "| ${tally.gamesWithOverwrites} | ${tally.shortestGame}–${tally.longestGame} |"
                )
            }
        }

        val devilish = tallies.getValue(Difficulty.DEVILISH)
        report.appendLine("\n## Devilish drama\n")
        report.appendLine("- Plan: the house wins on its move ${DevilishStrategy.EARLIEST_WIN_TURN}–${DevilishStrategy.TARGET_TURN}")
        report.appendLine("- Game lengths (human moves → games): ${devilish.lengths}")
        report.appendLine("- Hope offered (a move that would put the human one square from winning): ${devilish.hopeOffered}/${devilish.games} games")
        report.appendLine("- Human actually got one square from winning: ${devilish.humanGotOneAway}/${devilish.games} games")
        report.appendLine("- Final move overwrote an X from a line the human was about to complete: ${devilish.winStolen}/${devilish.games} games")

        TestStorage().openOutputFile("computer-opponent/exhaustive-report.md").use {
            it.write(report.toString().toByteArray())
        }

        assertEquals("Devilish lost a game (failure 1)", 0, devilish.humanWins)
        assertEquals("Devilish drew a game (failure 2)", 0, devilish.draws)
        assertTrue("Devilish never needed to cheat — suspicious", devilish.gamesWithOverwrites > 0)
        assertTrue(
            "Devilish won after only ${devilish.shortestGame} human moves (failure 14)",
            devilish.shortestGame >= MIN_DEVILISH_HUMAN_MOVES,
        )
        assertTrue(
            "Devilish ran past its deadline: ${devilish.longestGame} human moves (failure 3)",
            devilish.longestGame <= DevilishStrategy.TARGET_TURN,
        )
        assertEquals("Devilish offered no hope in some games (failure 15)", devilish.games, devilish.hopeOffered)

        val hard = tallies.getValue(Difficulty.HARD)
        val medium = tallies.getValue(Difficulty.MEDIUM)
        assertTrue("Hard should be beatable sometimes (failure 10)", hard.humanWins > 0)
        assertTrue(
            "Hard (${hard.humanWins} losses) should lose less than Medium (${medium.humanWins})",
            hard.humanWins < medium.humanWins,
        )
        assertTrue("Devilish solve took ${solveMs}ms (failure 7)", solveMs < MAX_SOLVE_MS)
    }

    private fun explore(level: Difficulty, random: Random, tally: Tally, board: List<CellValue>, path: Path) {
        for (cell in board.indices) {
            if (board[cell] != CellValue.Empty) continue
            val afterHuman = TicTacToeLogic.makeMove(board, cell, CellValue.X)
            val humanPath = path.copy(
                humanMoves = path.humanMoves + 1,
                oneAway = path.oneAway || threatLines(afterHuman).isNotEmpty(),
            )
            assertTrue("$level: game ran past $MAX_HUMAN_MOVES human moves (failure 3)", humanPath.humanMoves <= MAX_HUMAN_MOVES)

            when {
                TicTacToeLogic.checkWin(afterHuman, CellValue.X) -> tally.end(Outcome.HUMAN, humanPath, stolen = false)
                // Against Devilish a full board isn't over: the house can still overwrite a square.
                TicTacToeLogic.isBoardFull(afterHuman) && level != Difficulty.DEVILISH ->
                    tally.end(Outcome.DRAW, humanPath, stolen = false)
                else -> {
                    val houseTurn = humanPath.humanMoves
                    val move = ComputerOpponent.chooseMove(afterHuman, level, houseTurn = houseTurn, random = random)
                    val where = "$level turn $houseTurn on ${afterHuman.pretty()} chose ${move.index}"
                    assertTrue("$where: off the board (failure 4)", move.index in 0..8)
                    assertTrue("$where: already its own square (failure 4)", afterHuman[move.index] != CellValue.O)
                    assertEquals("$where: overwrite flag wrong", afterHuman[move.index] == CellValue.X, move.overwrites)
                    if (level != Difficulty.DEVILISH) assertFalse("$where: overwrote (failure 8)", move.overwrites)

                    val afterComputer = TicTacToeLogic.makeMove(afterHuman, move.index, CellValue.O)
                    val won = TicTacToeLogic.checkWin(afterComputer, CellValue.O)
                    if (level != Difficulty.DEVILISH && completing(afterHuman, CellValue.O).isNotEmpty()) {
                        assertTrue("$where: missed an immediate win (failure 9)", won)
                    }
                    if (level in BLOCKERS && !won) {
                        val threats = completing(afterHuman, CellValue.X)
                        if (threats.isNotEmpty()) assertTrue("$where: failed to block $threats (failure 9)", move.index in threats)
                    }

                    val stolen = won && move.overwrites && threatLines(afterHuman).any { move.index in it }
                    val housePath = humanPath.copy(
                        overwrites = humanPath.overwrites + if (move.overwrites) 1 else 0,
                        hopeOffered = humanPath.hopeOffered || (!won && hopeFor(afterComputer)),
                    )
                    when {
                        won -> tally.end(Outcome.COMPUTER, housePath, stolen)
                        TicTacToeLogic.isBoardFull(afterComputer) -> tally.end(Outcome.DRAW, housePath, stolen = false)
                        else -> explore(level, random, tally, afterComputer, housePath)
                    }
                }
            }
        }
    }

    private fun Tally.end(outcome: Outcome, path: Path, stolen: Boolean) {
        games++
        when (outcome) {
            Outcome.HUMAN -> humanWins++
            Outcome.COMPUTER -> computerWins++
            Outcome.DRAW -> draws++
        }
        if (path.overwrites > 0) gamesWithOverwrites++
        shortestGame = minOf(shortestGame, path.humanMoves)
        longestGame = maxOf(longestGame, path.humanMoves)
        lengths[path.humanMoves] = (lengths[path.humanMoves] ?: 0) + 1
        if (path.hopeOffered) hopeOffered++
        if (path.oneAway) humanGotOneAway++
        if (stolen) winStolen++
    }

    /** Lines where the human has two marks and the third square is empty. */
    private fun threatLines(board: List<CellValue>): List<List<Int>> = LINES.filter { line ->
        line.count { board[it] == CellValue.X } == 2 && line.count { board[it] == CellValue.Empty } == 1
    }

    /** Whether the human has a move that would leave them one square from winning. */
    private fun hopeFor(board: List<CellValue>): Boolean = board.indices.any { i ->
        board[i] == CellValue.Empty && threatLines(TicTacToeLogic.makeMove(board, i, CellValue.X)).isNotEmpty()
    }

    /** Empty squares where [symbol] would complete a line. */
    private fun completing(board: List<CellValue>, symbol: CellValue): List<Int> =
        board.indices.filter { board[it] == CellValue.Empty && TicTacToeLogic.checkWin(TicTacToeLogic.makeMove(board, it, symbol), symbol) }

    private fun List<CellValue>.pretty() = chunked(3).joinToString("|") { row ->
        row.joinToString("") { when (it) { CellValue.X -> "X"; CellValue.O -> "O"; CellValue.Empty -> "·" } }
    }

    private companion object {
        const val MAX_HUMAN_MOVES = 20
        const val MAX_SOLVE_MS = 1_000L
        const val MIN_DEVILISH_HUMAN_MOVES = DevilishStrategy.EARLIEST_WIN_TURN
        val BLOCKERS = setOf(Difficulty.MEDIUM, Difficulty.HARD)
        val LINES = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6),
        )
    }
}
