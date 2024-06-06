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

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiFile

class TypingListener : TypedHandlerDelegate() {

    private var changed = false

    init {
        ApplicationManager.getApplication().executeOnPooledThread {
            Thread.sleep(3000)
            if (!changed) return@executeOnPooledThread
            for (project in ProjectManager.getInstance().openProjects) applyTipInlay(project)
        }
    }

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        changed = true

        return Result.CONTINUE
    }
}