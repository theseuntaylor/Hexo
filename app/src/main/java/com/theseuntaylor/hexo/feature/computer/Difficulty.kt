package com.theseuntaylor.hexo.feature.computer

enum class Difficulty(val label: String, val description: String) {
    EASY("Easy", "Makes plenty of mistakes."),
    MEDIUM("Medium", "Wins when it can, blocks when it must."),
    HARD("Hard", "Near-perfect. Beatable, barely."),
    DEVILISH("Devilish", "The house always wins. It doesn't play fair."),
}
