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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent

class PsiListener : PsiTreeChangeAdapter() {

    override fun childAdded(event: PsiTreeChangeEvent) = process(event)
    override fun childRemoved(event: PsiTreeChangeEvent) = process(event)
    override fun childMoved(event: PsiTreeChangeEvent) = process(event)
    override fun childReplaced(event: PsiTreeChangeEvent) = process(event)
    override fun childrenChanged(event: PsiTreeChangeEvent) = process(event)

    private fun process(event: PsiTreeChangeEvent) {
        ApplicationManager.getApplication().invokeLater {
            TipInlay((event.file ?: return@invokeLater).project).process()
        }
    }

}