// Copyright 2024 Jelly Terra
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

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import java.awt.Color
import javax.swing.Icon

class TipInlay(private val project: Project) {

    private val editor = FileEditorManager.getInstance(project).selectedTextEditor!!

    private fun lineOf(offset: Int) = editor.document.getLineNumber(offset)
    private fun startOf(lineNum: Int) = editor.document.getLineStartOffset(lineNum)
    private fun endOf(lineNum: Int) = editor.document.getLineEndOffset(lineNum)

    private fun clearAll() {
        for (h in editor.markupModel.allHighlighters) {
            if (h.gutterIconRenderer is TipGutterRenderer) {
                h.dispose()
                editor.markupModel.removeHighlighter(h)
            }
        }

        for (inlay in editor.inlayModel.getAfterLineEndElementsInRange(
            0,
            endOf(editor.document.lineCount - 1),
            TipInlayRenderer::class.java
        )) {
            inlay.dispose()
        }
    }

    private fun addTip(line: Int, text: String, color: Color, icon: Icon) {
        try {
            if (editor.document.lineCount - 1 < line) return

            editor.markupModel.addRangeHighlighter(
                startOf(line),
                endOf(line),
                0,
                TextAttributes(editor.colorsScheme.defaultForeground, color, null, null, EditorFontType.PLAIN.ordinal),
                HighlighterTargetArea.LINES_IN_RANGE
            ).gutterIconRenderer = TipGutterRenderer(text, icon)

            editor.inlayModel.addAfterLineEndElement(endOf(line), true, TipInlayRenderer(text, color))
        } catch (_: IndexOutOfBoundsException) {
            // Ignore.
        }
    }

    fun process() {
        clearAll()

        val map = HashMap<Int, HighlightInfo>()

        val highlights = DocumentMarkupModel.forDocument(editor.document, project, false)
            .allHighlighters
            .filter { it.errorStripeTooltip is HighlightInfo }
            .map { it.errorStripeTooltip as HighlightInfo }

        for (h in highlights) if (h.severity == HighlightSeverity.WEAK_WARNING) map[lineOf(h.startOffset)] = h
        for (h in highlights) if (h.severity == HighlightSeverity.WARNING) map[lineOf(h.startOffset)] = h
        for (h in highlights) if (h.severity == HighlightSeverity.ERROR) map[lineOf(h.startOffset)] = h

        for (h in map.values) {
            val (color, icon) = when (h.severity) {
                HighlightSeverity.ERROR -> Pair(Color(0x9e, 0x29, 0x27, 0x22), AllIcons.General.Error)
                HighlightSeverity.WARNING -> Pair(Color(0x75, 0x6d, 0x56, 0x22), AllIcons.General.Warning)
                HighlightSeverity.WEAK_WARNING -> Pair(Color(0xbe, 0x91, 0x17, 0x22), AllIcons.General.Warning)
                else -> continue
            }

            addTip(lineOf(h.startOffset), h.description, color, icon)
        }
    }
}
