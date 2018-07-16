package ru.nsu.tester.gui

import ru.nsu.tester.comparison.ComparisonError
import java.awt.*
import javax.swing.*
import javax.imageio.ImageIO
import java.io.File

private const val MAX_PANEL_WIDTH = 600
private const val MAX_PANEL_HEIGHT = 600
private const val OFFSET = 20
private val LABEL_FONT = Font("Tahoma", Font.PLAIN, 16)

class AnalysisRenderer(comparisonError: ComparisonError) {
    private val antlrPanel = TreePanel(comparisonError.antlrTree)
    private val psiPanel = TreePanel(comparisonError.psiTree)

    private fun generateInfoPanel(insidePanel: JPanel, labelText: String, size: Dimension) : JPanel {
        val label = JLabel(labelText)
        label.font = LABEL_FONT
        label.border = BorderFactory.createEmptyBorder(5, 5, 5 ,5)

        val scroll = JScrollPane(insidePanel)
        scroll.preferredSize = size
        scroll.border = BorderFactory.createEmptyBorder()

        val panel = JPanel()
        panel.border = BorderFactory.createMatteBorder(1, 1, 1, 0, Color.LIGHT_GRAY)
        panel.layout = BorderLayout()
        panel.add(label, BorderLayout.NORTH)
        panel.add(scroll, BorderLayout.SOUTH)

        return panel
    }

    fun display() {
        val f = JFrame("Tree comparison")
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        f.layout = GridBagLayout()

        val mainPanel = JPanel()
        mainPanel.layout = GridBagLayout()
        val gc = GridBagConstraints()

        var width = maxOf(antlrPanel.treeLayout.bounds.bounds.width, psiPanel.treeLayout.bounds.bounds.width) + OFFSET
        if (width > MAX_PANEL_WIDTH) width = MAX_PANEL_WIDTH
        var height = maxOf(antlrPanel.treeLayout.bounds.bounds.height, psiPanel.treeLayout.bounds.bounds.height) + OFFSET
        if (height > MAX_PANEL_HEIGHT) height = MAX_PANEL_HEIGHT

        gc.gridy = 0
        gc.gridx = 0
        mainPanel.add(generateInfoPanel(antlrPanel, "ANTLR", Dimension(width, height)), gc)
        gc.gridx = 1
        mainPanel.add(generateInfoPanel(psiPanel, "PSI", Dimension(width, height)), gc)

        val utilPanel = JPanel()
        val screenshotButton = JButton("Snapshot")
        utilPanel.add(screenshotButton)
        screenshotButton.addActionListener({
            try {
                val robot = Robot()
                val bounds = mainPanel.bounds
                bounds.x = mainPanel.locationOnScreen.x
                bounds.y = mainPanel.locationOnScreen.y
                val snapShot = robot.createScreenCapture(bounds)
                ImageIO.write(snapShot, "png", File("Snapshot.png"))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        })

        val c = f.contentPane
        gc.gridy = 0
        gc.gridx = 0
        c.add(mainPanel, gc)
        gc.gridy = 1
        c.add(utilPanel, gc)

        f.pack()
        f.setLocationRelativeTo(null)
        f.isVisible = true
        f.isResizable = false
    }
}
