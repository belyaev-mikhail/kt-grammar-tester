package ru.nsu.tester.gui

import org.abego.treelayout.NodeExtentProvider
import ru.nsu.tester.comparison.wrapper.TreeWrapper

private const val CHAR_WIDTH = 7.7
private const val CHAR_HEIGHT = 20.0

class MyNodeExtentProvider: NodeExtentProvider<TreeWrapper> {
    override fun getWidth(p0: TreeWrapper?): Double {
        if (p0 == null) return 0.0
        val text: String = if (p0.childrenCount() == 0) p0.getText() else p0.getName()
        return text.length.times(CHAR_WIDTH)
    }

    override fun getHeight(p0: TreeWrapper?): Double {
        return CHAR_HEIGHT
    }
}