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

import com.intellij.openapi.editor.markup.GutterIconRenderer
import javax.swing.Icon

class TipGutterRenderer(private val text: String, private val icon: Icon) : GutterIconRenderer() {

    override fun getIcon() = icon
    override fun getTooltipText() = text
    override fun equals(other: Any?) = this === other
    override fun hashCode() = icon.hashCode()
}