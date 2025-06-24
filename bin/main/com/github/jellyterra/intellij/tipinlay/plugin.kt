// Copyright 2025 Jelly Terra <jellyterra@proton.me>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.jellyterra.intellij.tipinlay

import com.intellij.analysis.problemsView.Problem
import com.intellij.analysis.problemsView.ProblemsListener
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.preferredWidth
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.Icon

fun applyInlay(project: Project) {
    val state = PluginState().getInstance()!!

    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

    fun lineOf(offset: Int) = editor.document.getLineNumber(offset)
    fun startOf(lineNum: Int) = editor.document.getLineStartOffset(lineNum)
    fun endOf(lineNum: Int) = editor.document.getLineEndOffset(lineNum)

    fun clearAll() {
        for (h in editor.markupModel.allHighlighters) {
            if (h.gutterIconRenderer is GutterRenderer) {
                h.dispose()
                editor.markupModel.removeHighlighter(h)
            }
        }

        for (inlay in editor.inlayModel.getAfterLineEndElementsInRange(
            0, endOf(editor.document.lineCount - 1), Render::class.java
        )) {
            inlay.dispose()
        }
    }

    fun addInlay(line: Int, text: String, color: Color, icon: Icon) {
        editor.markupModel.addRangeHighlighter(
            startOf(line),
            endOf(line),
            0,
            TextAttributes(Color(color.rgb), color, null, null, EditorFontType.PLAIN.ordinal),
            HighlighterTargetArea.LINES_IN_RANGE
        ).gutterIconRenderer = GutterRenderer(text, icon)

        editor.inlayModel.addAfterLineEndElement(endOf(line), true, Render(text, color))
    }

    fun process() {
        clearAll()

        val map = HashMap<Int, HighlightInfo>()

        val highlights = DocumentMarkupModel.forDocument(
            editor.document,
            project,
            false
        ).allHighlighters.filter {
            it.errorStripeTooltip is HighlightInfo
        }.map {
            it.errorStripeTooltip as HighlightInfo
        }

        for (h in highlights) if (h.severity == HighlightSeverity.WEAK_WARNING) map[lineOf(h.startOffset)] = h
        for (h in highlights) if (h.severity == HighlightSeverity.WARNING) map[lineOf(h.startOffset)] = h
        for (h in highlights) if (h.severity == HighlightSeverity.ERROR) map[lineOf(h.startOffset)] = h

        for (h in map.values) {
            val (color, icon) = when (h.severity) {
                HighlightSeverity.ERROR -> Pair(Color(state.colorError.toInt(), true), AllIcons.General.Error)
                HighlightSeverity.WARNING -> Pair(Color(state.colorWarning.toInt(), true), AllIcons.General.Warning)
                HighlightSeverity.WEAK_WARNING -> Pair(Color(state.colorHint.toInt(), true), AllIcons.Empty)
                HighlightSeverity.INFORMATION -> Pair(Color(state.colorInfo.toInt(), true), AllIcons.Empty)

                else -> continue
            }

            addInlay(lineOf(h.startOffset), h.description, color, icon)
        }
    }

    try {
        process()
    } catch (_: IndexOutOfBoundsException) {
        // Ignore.
    }
}

class GutterRenderer(private val text: String, private val icon: Icon) : GutterIconRenderer() {
    override fun getIcon() = icon
    override fun getTooltipText() = text
    override fun equals(other: Any?) = this === other
    override fun hashCode() = icon.hashCode()
}

class Render(private val text: String, private val color: Color) : EditorCustomElementRenderer {
    override fun calcWidthInPixels(inlay: Inlay<*>) = JBLabel(text).preferredWidth

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        g.font = inlay.editor.colorsScheme.getFont(EditorFontType.PLAIN)
        g.color = Color(color.rgb)
        g.drawString(text, targetRegion.x, targetRegion.y + inlay.editor.ascent)
    }
}

class PsiListener : PsiTreeChangeAdapter() {
    override fun childAdded(event: PsiTreeChangeEvent) = process(event)
    override fun childRemoved(event: PsiTreeChangeEvent) = process(event)
    override fun childMoved(event: PsiTreeChangeEvent) = process(event)
    override fun childReplaced(event: PsiTreeChangeEvent) = process(event)
    override fun childrenChanged(event: PsiTreeChangeEvent) = process(event)

    private fun process(event: PsiTreeChangeEvent) {
        ApplicationManager.getApplication().invokeLater {
            applyInlay((event.file ?: return@invokeLater).project)
        }
    }
}

class ProblemsListener : ProblemsListener {
    override fun problemAppeared(problem: Problem) = process(problem)
    override fun problemDisappeared(problem: Problem) = process(problem)
    override fun problemUpdated(problem: Problem) = process(problem)

    private fun process(problem: Problem) {
        ApplicationManager.getApplication().invokeLater {
            applyInlay(problem.provider.project)
        }
    }
}

class TypingListener : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        ApplicationManager.getApplication().invokeLater { applyInlay(project) }

        return Result.CONTINUE
    }
}

@State(
    name = "com.github.jellyterra.intellij.tipinlay.State",
    storages = [Storage("TipInlayState.xml")]
)
class PluginState : PersistentStateComponent<PluginState> {
    var colorError = 0x22E45454u
    var colorWarning = 0x22FF942Fu
    var colorHint = 0x222FAF64u
    var colorInfo = 0x2200B7E4u

    override fun getState(): PluginState {
        return this
    }

    override fun loadState(state: PluginState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun getInstance(): PluginState? {
        if (ApplicationManager.getApplication().isDisposed) return null
        return ApplicationManager.getApplication().getService(javaClass)
    }
}

class PostStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        while (!project.isDisposed) {
            ApplicationManager.getApplication().invokeLater {
                applyInlay(project)
            }

            Thread.sleep(1000)
        }
    }
}
