package com.wouter.crudcodegen.generators.filters

import com.wouter.crudcodegen.engine.Variable
import java.io.File

interface ProjectTemplateFilter: GeneratorFilter {
    fun enrichProperties(settings: ProjectTemplateSettings): Iterable<Variable>

    data class ProjectTemplateSettings(
            val groupId: String?,
            val artifactId: String?,
            val name: String,
            val targetPath: File
    )
}
