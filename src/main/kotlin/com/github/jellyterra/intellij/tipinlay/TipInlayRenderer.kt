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

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.preferredWidth
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

class TipInlayRenderer(private val text: String, private val color: Color) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>) = JBLabel(text).preferredWidth

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        g.font = inlay.editor.colorsScheme.getFont(EditorFontType.PLAIN)
        g.color = Color(color.rgb, false)
        g.drawString(text, targetRegion.x, targetRegion.y + inlay.editor.ascent)
    }
}