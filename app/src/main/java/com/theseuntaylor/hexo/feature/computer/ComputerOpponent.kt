package com.theseuntaylor.hexo.feature.computer

import com.theseuntaylor.hexo.feature.game.CellValue
import com.theseuntaylor.hexo.feature.game.TicTacToeLogic
import kotlin.random.Random

/** A square the computer claims. [overwrites] is true when it takes one of the human's squares. */
data class ComputerMove(val index: Int, val overwrites: Boolean)

/**
 * Picks the computer's moves. The human is always X and moves first; the computer is O.
 */
object ComputerOpponent {

    private const val HARD_MISTAKE_RATE = 0.2f

    /** [houseTurn] is 1 for the computer's first move of the game, 2 for its second, and so on. */
    fun chooseMove(
        board: List<CellValue>,
        difficulty: Difficulty,
        houseTurn: Int = 1,
        random: Random = Random.Default,
    ): ComputerMove = when (difficulty) {
        Difficulty.EASY ->
            completingMove(board, CellValue.O) ?: randomMove(board, random)

        Difficulty.MEDIUM ->
            completingMove(board, CellValue.O) ?: completingMove(board, CellValue.X) ?: randomMove(board, random)

        // Never misses a win or a block; its occasional mistakes are in the quieter moves.
        Difficulty.HARD ->
            completingMove(board, CellValue.O) ?: completingMove(board, CellValue.X)
                ?: if (random.nextFloat() < HARD_MISTAKE_RATE) randomMove(board, random)
                else PerfectPlay.bestMove(board, random)

        Difficulty.DEVILISH -> DevilishStrategy.chooseMove(board, houseTurn)
    }

    /** Solves the Devilish game up front so the first move doesn't stall. */
    fun warmUp() = DevilishStrategy.warmUp()

    /** An empty square where [symbol] would complete a line, if any. */
    private fun completingMove(board: List<CellValue>, symbol: CellValue): ComputerMove? =
        board.indices.firstOrNull { i ->
            board[i] == CellValue.Empty && TicTacToeLogic.checkWin(TicTacToeLogic.makeMove(board, i, symbol), symbol)
        }?.let { ComputerMove(it, overwrites = false) }

    private fun randomMove(board: List<CellValue>, random: Random): ComputerMove =
        ComputerMove(board.indices.filter { board[it] == CellValue.Empty }.random(random), overwrites = false)
}

// ── Board encoding shared by the solvers ────────────────────────────────────
// A board is a base-3 number: square i contributes cell * 3^i (0 = empty, 1 = human, 2 = computer).

private const val EMPTY = 0
private const val HUMAN = 1
private const val HOUSE = 2
private const val STATES = 19_683 // 3^9
private val POW3 = IntArray(9).also { p -> p[0] = 1; for (i in 1 until 9) p[i] = p[i - 1] * 3 }
private val LINES = arrayOf(
    intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
    intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
    intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
)

private fun cellAt(state: Int, i: Int) = state / POW3[i] % 3
private fun withCell(state: Int, i: Int, value: Int) = state + (value - cellAt(state, i)) * POW3[i]
private fun hasLine(state: Int, who: Int) = LINES.any { line -> line.all { cellAt(state, it) == who } }

/** Lines where the human has two marks and the third square is empty: one move from winning. */
private fun threatLines(state: Int) = LINES.filter { line ->
    line.count { cellAt(state, it) == HUMAN } == 2 && line.count { cellAt(state, it) == EMPTY } == 1
}

private fun encode(board: List<CellValue>): Int = board.indices.sumOf { i ->
    POW3[i] * when (board[i]) {
        CellValue.Empty -> EMPTY
        CellValue.X -> HUMAN
        CellValue.O -> HOUSE
    }
}

/** Classic minimax: never loses a fair game, and prefers quicker wins. */
private object PerfectPlay {
    private val scores = HashMap<Int, Int>()

    @Synchronized
    fun bestMove(board: List<CellValue>, random: Random): ComputerMove {
        val state = encode(board)
        val moves = (0 until 9).filter { cellAt(state, it) == EMPTY }
        val scored = moves.associateWith { score(withCell(state, it, HOUSE), houseToMove = false) }
        val best = scored.values.max()
        return ComputerMove(scored.filterValues { it == best }.keys.random(random), overwrites = false)
    }

    /** Positive when the house wins; larger means sooner. */
    private fun score(state: Int, houseToMove: Boolean): Int {
        val key = state * 2 + if (houseToMove) 1 else 0
        scores[key]?.let { return it }
        val empties = (0 until 9).count { cellAt(state, it) == EMPTY }
        val result = when {
            hasLine(state, HOUSE) -> 1 + empties
            hasLine(state, HUMAN) -> -(1 + empties)
            empties == 0 -> 0
            houseToMove -> (0 until 9).filter { cellAt(state, it) == EMPTY }
                .maxOf { score(withCell(state, it, HOUSE), houseToMove = false) }
            else -> (0 until 9).filter { cellAt(state, it) == EMPTY }
                .minOf { score(withCell(state, it, HUMAN), houseToMove = true) }
        }
        scores[key] = result
        return result
    }
}

/**
 * Devilish: the house may place its mark on an empty square *or* over one of the human's.
 *
 * [solve] works backwards from every position where the house has a line (retrograde analysis)
 * and records, for each position, how many more house turns it needs to force a win.
 *
 * The house plays for drama, not speed. [planWindow] extends that analysis with the move number,
 * so the house only considers moves that still force a win on a move between [EARLIEST_WIN_TURN]
 * and [TARGET_TURN] — the outcome is never in doubt, the game can't loop, and it can't end early.
 * Within that plan it holds back its win, plays fair where it can, and leaves the human room to
 * line up two in a row. For the final blow it prefers to overwrite an X from the line the human
 * was about to complete. [solve]'s fastest-win distances are the fallback if the plan runs out.
 */
internal object DevilishStrategy {

    internal class Solution(
        /** House turns left to force a win with the human to move, or [UNKNOWN]. */
        val humanToMove: IntArray,
        /** House turns left to force a win with the house to move, or [UNKNOWN]. */
        val houseToMove: IntArray,
    )

    internal const val UNKNOWN = -1

    /** The latest house move on which Devilish wins (the human has made this many moves by then). */
    const val TARGET_TURN = 5

    /** Devilish never wins before this move, so the human always gets a real game. */
    const val EARLIEST_WIN_TURN = 4

    /** `house[t][s]`: with the house to play its move t on board s, it can win on plan. `human[t][s]`: same, human to move first. */
    internal class Plan(val house: Array<BooleanArray>, val human: Array<BooleanArray>)

    private val solution by lazy { solve() }
    private val plan by lazy { planWindow() }
    private val houseLine by lazy { BooleanArray(STATES) { hasLine(it, HOUSE) } }
    private val humanLine by lazy { BooleanArray(STATES) { hasLine(it, HUMAN) } }

    fun warmUp() {
        solution
        plan
    }

    /** Whether the house playing square [i] as its move [turn] on board [state] keeps it on plan. */
    private fun onPlan(state: Int, i: Int, turn: Int, humanPlan: Array<BooleanArray>): Boolean {
        val next = withCell(state, i, HOUSE)
        return if (houseLine[next]) turn >= EARLIEST_WIN_TURN else turn < TARGET_TURN && humanPlan[turn + 1][next]
    }

    /** Backward induction over the move number, from [TARGET_TURN] down to the first move. */
    internal fun planWindow(): Plan {
        val house = Array(TARGET_TURN + 2) { BooleanArray(STATES) }
        val human = Array(TARGET_TURN + 2) { BooleanArray(STATES) }
        for (turn in TARGET_TURN downTo 1) {
            for (s in 0 until STATES) {
                if (houseLine[s] || humanLine[s]) continue
                house[turn][s] = (0 until 9).any { i -> cellAt(s, i) != HOUSE && onPlan(s, i, turn, human) }
            }
            for (s in 0 until STATES) {
                if (houseLine[s] || humanLine[s]) continue
                var hasMove = false
                var allOnPlan = true
                for (e in 0 until 9) {
                    if (cellAt(s, e) != EMPTY) continue
                    hasMove = true
                    val next = withCell(s, e, HUMAN)
                    if (humanLine[next] || !house[turn][next]) {
                        allOnPlan = false
                        break
                    }
                }
                human[turn][s] = hasMove && allOnPlan
            }
        }
        return Plan(house, human)
    }

    private class Candidate(
        val index: Int,
        val overwrites: Boolean,
        val wins: Boolean,
        /** Overwrites an X from a line the human was one move from completing. */
        val steals: Boolean,
        /** How many replies would leave the human one square from winning. */
        val hope: Int,
        val turnsLeft: Int,
    )

    private val dramaOrder = compareBy<Candidate>(
        { it.wins }, // Hold the win back while the deadline allows.
        { if (it.wins) !it.steals else it.overwrites }, // Final blow: steal their line. Before that: play fair.
        { it.wins && !it.overwrites },
        { -it.hope },
        { it.index },
    )

    fun chooseMove(board: List<CellValue>, houseTurn: Int): ComputerMove {
        val state = encode(board)
        val threatened = threatLines(state).flatMap { it.asIterable() }.filter { cellAt(state, it) == HUMAN }.toSet()
        val candidates = (0 until 9).mapNotNull { i ->
            val current = cellAt(state, i)
            if (current == HOUSE) return@mapNotNull null
            val next = withCell(state, i, HOUSE)
            val wins = hasLine(next, HOUSE)
            val turnsLeft = if (wins) 0 else solution.humanToMove[next].takeIf { it != UNKNOWN } ?: return@mapNotNull null
            Candidate(
                index = i,
                overwrites = current == HUMAN,
                wins = wins,
                steals = current == HUMAN && i in threatened,
                hope = if (wins) 0 else (0 until 9).count { e ->
                    cellAt(next, e) == EMPTY && threatLines(withCell(next, e, HUMAN)).isNotEmpty()
                },
                turnsLeft = turnsLeft,
            )
        }
        // Moves that keep the win on plan; if the plan ever runs out, just win as fast as possible.
        val onPlan = if (houseTurn in 1..TARGET_TURN) {
            candidates.filter { onPlan(state, it.index, houseTurn, plan.human) }
        } else {
            emptyList()
        }
        val chosen = onPlan.minWithOrNull(dramaOrder)
            ?: candidates.minWithOrNull(compareBy<Candidate>({ it.turnsLeft }, { it.overwrites }, { it.index }))
        // Unreachable when the human moves first: the solver proves every such position is a forced win.
        return chosen?.let { ComputerMove(it.index, it.overwrites) }
            ?: (0 until 9).first { cellAt(state, it) != HOUSE }
                .let { ComputerMove(it, overwrites = cellAt(state, it) == HUMAN) }
    }

    internal fun solve(): Solution {
        val humanToMove = IntArray(STATES) { UNKNOWN }
        val houseToMove = IntArray(STATES) { UNKNOWN }
        val finished = BooleanArray(STATES) { hasLine(it, HUMAN) || hasLine(it, HOUSE) }
        val houseLine = BooleanArray(STATES) { hasLine(it, HOUSE) }
        val humanLine = BooleanArray(STATES) { hasLine(it, HUMAN) }

        var turns = 0
        var changed = true
        while (changed) {
            changed = false
            turns++

            // House to move: solved once some move wins now or reaches a position solved last round.
            val newlySolved = ArrayList<Int>()
            for (s in 0 until STATES) {
                if (finished[s] || houseToMove[s] != UNKNOWN) continue
                for (i in 0 until 9) {
                    if (cellAt(s, i) == HOUSE) continue
                    val next = withCell(s, i, HOUSE)
                    if (houseLine[next] || (humanToMove[next] != UNKNOWN && humanToMove[next] < turns)) {
                        newlySolved += s
                        break
                    }
                }
            }
            for (s in newlySolved) houseToMove[s] = turns
            if (newlySolved.isNotEmpty()) changed = true

            // Human to move: solved once every reply loses. A full board (no reply) is a draw, not a win.
            for (s in 0 until STATES) {
                if (finished[s] || humanToMove[s] != UNKNOWN) continue
                var worst = 0
                var hasMove = false
                var allLose = true
                for (i in 0 until 9) {
                    if (cellAt(s, i) != EMPTY) continue
                    hasMove = true
                    val next = withCell(s, i, HUMAN)
                    if (humanLine[next] || houseToMove[next] == UNKNOWN) {
                        allLose = false
                        break
                    }
                    worst = maxOf(worst, houseToMove[next])
                }
                if (hasMove && allLose) {
                    humanToMove[s] = worst
                    changed = true
                }
            }
        }
        return Solution(humanToMove, houseToMove)
    }
}
