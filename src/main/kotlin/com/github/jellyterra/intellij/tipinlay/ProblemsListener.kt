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

import com.intellij.analysis.problemsView.Problem
import com.intellij.analysis.problemsView.ProblemsListener
import com.intellij.openapi.application.ApplicationManager

class ProblemsListener : ProblemsListener {

    override fun problemAppeared(problem: Problem) = process(problem)
    override fun problemDisappeared(problem: Problem) = process(problem)
    override fun problemUpdated(problem: Problem) = process(problem)

    private fun process(problem: Problem) {
        ApplicationManager.getApplication().invokeLater {
            applyTipInlay(problem.provider.project)
        }
    }
}