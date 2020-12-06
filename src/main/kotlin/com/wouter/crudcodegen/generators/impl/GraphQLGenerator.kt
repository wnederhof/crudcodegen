package com.wouter.crudcodegen.generators.impl

import com.wouter.crudcodegen.generators.Generator
import com.wouter.crudcodegen.generators.GeneratorSettings
import com.wouter.crudcodegen.generators.ProjectProperties
import com.wouter.crudcodegen.generators.filters.EntityTemplateFilter
import com.wouter.crudcodegen.generators.filters.FieldTemplateFilter
import com.wouter.crudcodegen.generators.filters.ProjectTemplateFilter
import com.wouter.crudcodegen.generators.helpers.FieldArgsHelper
import com.wouter.crudcodegen.generators.helpers.VariablesHelper
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File

@Component
@Order(3)
class GraphQLGenerator(
        private val entityTemplateFilters: List<EntityTemplateFilter>,
        private val projectTemplateFilters: List<ProjectTemplateFilter>,
        private val fieldTemplateFilters: List<FieldTemplateFilter>,
        private val fieldArgsHelper: FieldArgsHelper,
        private val variablesHelper: VariablesHelper
) : Generator {

    override fun getSyntax() =
            "graphql <name> (<name>:<type>)+"

    override fun getSyntaxDescription() =
            "Generates a new GraphQL Schema and resolvers."

    override fun acceptsGeneratorName(name: String) = name == "graphql"

    override fun templateName() = "graphql"

    override fun initializeGenerator(targetPath: File, properties: ProjectProperties, args: List<String>): GeneratorSettings {
        val fields = fieldArgsHelper.mapArgsToEntityFields(args.drop(1))
        val filters = entityTemplateFilters + projectTemplateFilters + fieldTemplateFilters
        return GeneratorSettings(
                updatedProperties = properties,
                variables = variablesHelper.createVariables(targetPath, properties, args[0], fields, filters)
        )
    }

}
