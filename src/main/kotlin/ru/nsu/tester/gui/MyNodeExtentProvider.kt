package ru.nsu.tester.gui

import org.abego.treelayout.NodeExtentProvider
import ru.nsu.tester.comparison.wrapper.TreeWrapper

private const val LOWER_CHAR_WIDTH = 6.6
private const val UPPER_CHAR_WIDTH = 8.0
private const val CHAR_HEIGHT = 23.0

class MyNodeExtentProvider: NodeExtentProvider<TreeWrapper> {
    override fun getWidth(p0: TreeWrapper?): Double {
        if (p0 == null) return 0.0
        val text: String = if (p0.childrenCount() == 0) p0.getText() else p0.getName()

        return if (Character.isUpperCase(text[text.lastIndex]))
            text.length.times(UPPER_CHAR_WIDTH)
        else
            text.length.times(LOWER_CHAR_WIDTH)
    }

    override fun getHeight(p0: TreeWrapper?): Double {
        return CHAR_HEIGHT
    }
}