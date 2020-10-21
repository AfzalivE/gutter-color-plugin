package com.github.afzalive.guttercolorplugin.services

import com.intellij.openapi.project.Project
import com.github.afzalive.guttercolorplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
