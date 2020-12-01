package com.wouter.crudcodegen.generators.impl

import com.wouter.crudcodegen.engine.Variable
import com.wouter.crudcodegen.generators.Generator
import com.wouter.crudcodegen.generators.GeneratorSettings
import com.wouter.crudcodegen.generators.ProjectProperties
import com.wouter.crudcodegen.generators.helpers.NameHelper
import java.io.File

abstract class AbstractRelationalGenerator(private val nameHelper: NameHelper) : Generator {

    override fun initializeGenerator(targetPath: File, properties: ProjectProperties, args: List<String>): GeneratorSettings {
        val name = args.first()
        val fields = args.drop(1)

        val groupId = properties.groupId
                ?: error("groupId required but not set.")

        val artifactId = properties.artifactId
                ?: error("artifactId required but not set.")

        return GeneratorSettings(
                updatedProperties = properties,
                variables = listOf(
                        Variable("groupId", groupId),
                        Variable("artifactId", artifactId),
                        Variable("name", nameHelper.toLowerCamelCase(name)),
                        Variable("Name", nameHelper.toUpperCamelCase(name)),
                        Variable("names", nameHelper.pluralize(nameHelper.toLowerCamelCase(name))),
                        Variable("name_lowercase", name.toLowerCase()),
                        Variable("Names", nameHelper.pluralize(nameHelper.toUpperCamelCase(name))),
                        Variable("_name", nameHelper.toDuckName(name)),
                        Variable("_names", nameHelper.pluralize(nameHelper.toDuckName(name))),
                        Variable("name_dash", nameHelper.toDashName(name)),
                        Variable("names_dash", nameHelper.pluralize(nameHelper.toDashName(name))),
                        Variable("NAME", nameHelper.capitalize(nameHelper.toDuckName(name))),
                        Variable("NAMES", nameHelper.capitalize(nameHelper.pluralize(nameHelper.toDuckName(name)))),
                        Variable("hasRelations", fields.any { isRelationship(it.split(":")[1]) }),
                        Variable("anyNullable", fields.any { it.split(":")[1].endsWith("?") }),
                        Variable("fields", fields.mapIndexed { idx, it ->
                            it.split(":").let {
                                mapOf(
                                        "field_name" to nameHelper.toDuckName(it[0]),
                                        "field_name_dash" to nameHelper.toDashName(it[0]),
                                        "isNullable" to it[1].endsWith("?"),
                                        "isLast" to (fields.size - 1 == idx),
                                        "FieldName" to nameHelper.toUpperCamelCase(it[0]),
                                        "isTextInput" to (it[1] == "text" || it[1] == "text?"),
                                        "fieldName" to nameHelper.toLowerCamelCase(it[0]),
                                        "fieldInputType" to determineFieldInputType(it[1]),
                                        "FieldType" to nameHelper.toUpperCamelCase(it[1]),
                                        "FieldTypes" to nameHelper.toUpperCamelCase(nameHelper.pluralize(it[1])),
                                        "fieldTypes" to nameHelper.toLowerCamelCase(nameHelper.pluralize(it[1])),
                                        "fieldType_lowercase" to it[1].toLowerCase(),
                                        "fieldTypes_lowercase" to nameHelper.pluralize(it[1]).toLowerCase(),
                                        "FIELD_TYPE" to nameHelper.toDuckName(it[1]).toUpperCase(),
                                        "fieldType" to nameHelper.toLowerCamelCase(it[1]),
                                        "field_types" to nameHelper.toLowerCamelCase(nameHelper.pluralize(it[1])),
                                        "FIELD_DATABASE_TYPE" to determineDatabaseType(it[1]),
                                        "FieldKotlinType" to determineKotlinTypeNullable(it[1]),
                                        "FieldKotlinTypeNotNullable" to determineKotlinType(it[1]),
                                        "NullFieldGraphQLType" to determineNullableGraphQLType(it[1]),
                                        "FieldGraphQLType" to determineGraphQLType(it[1]),
                                        "fieldKotlinAnnotations" to determineKotlinAnnotations(it[1]),
                                        "fieldTestValue" to determineSomeTestValue(nameHelper.toUpperCamelCase(it[0]), it[1]),
                                        "fieldRelationship" to isRelationship(it[1])
                                )
                            }
                        }),
                        Variable("nextMigrationPrefix", findNextMigrationPrefix(targetPath))
                ))
    }

    private fun findNextMigrationPrefix(targetPath: File): String {
        val i = findLastMigrationNumber(targetPath) + 1
        return "$i".padStart(3, '0')
    }

    private fun findLastMigrationNumber(targetPath: File): Int {
        val files = File("$targetPath/src/main/resources/db/migration").listFiles()
                ?: return 0

        return files
                .filter { it.name.endsWith(".sql") }
                .map { it.name }
                .map { findNumberPrefix(it) }
                .maxBy { it } ?: 0
    }

    private fun findNumberPrefix(s: String): Int {
        // TODO test entire class...
        return Integer.parseInt(s.replace(Regex("V(\\d+).+"), "\$1"))
    }

    private fun determineDatabaseType(type: String): String {
        return when (type) {
            "string" -> "VARCHAR(255) NOT NULL"
            "int" -> "INT NOT NULL"
            "text" -> "TEXT NOT NULL"
            "date" -> "DATE NOT NULL"
            "dateTime" -> "TIMESTAMP NOT NULL"
            "boolean" -> "BOOLEAN NOT NULL"
            "string?" -> "VARCHAR(255) NULL"
            "int?" -> "INT NULL"
            "text?" -> "TEXT NULL"
            "date?" -> "DATE NULL"
            "dateTime?" -> "TIMESTAMP NULL"
            "boolean?" -> "BOOLEAN NULL"
            else -> "INT NOT NULL"
        }
    }

    private fun determineSomeTestValue(name: String, type: String): String {
        return when (type) {
            "string" -> "\"Some $name\""
            "int" -> "1"
            "text" -> "\"Some $name\""
            "date" -> "LocalDate.of(2000, 1, 1)"
            "dateTime" -> "LocalDateTime.of(2000, 1, 1, 0, 0)"
            "boolean" -> "true"
            "string?" -> "\"Some $name\""
            "int?" -> "1"
            "text?" -> "\"Some $name\""
            "date?" -> "LocalDate.of(2000, 1, 1)"
            "dateTime?" -> "LocalDateTime.of(2000, 1, 1, 0, 0)"
            "boolean?" -> "true"
            else -> "10"
        }
    }

    private fun determineKotlinAnnotations(type: String): String? {
        return when (type) {
            "string" -> null
            "int" -> null
            "text" -> "@Lob"
            "date" -> null
            "boolean" -> null
            "dateTime" -> null
            "string?" -> null
            "int?" -> null
            "text?" -> "@Lob"
            "date?" -> null
            "boolean?" -> null
            "dateTime?" -> null
            else -> null
        }
    }

    private fun isRelationship(type: String): Boolean {
        return when (type) {
            "string" -> false
            "int" -> false
            "text" -> false
            "date" -> false
            "dateTime" -> false
            "boolean" -> false
            "string?" -> false
            "int?" -> false
            "text?" -> false
            "date?" -> false
            "boolean?" -> false
            "dateTime?" -> false
            else -> true
        }
    }

    private fun determineKotlinType(type: String): String {
        return when (type) {
            "string" -> "String"
            "int" -> "Int"
            "text" -> "String"
            "date" -> "LocalDate"
            "dateTime" -> "LocalDateTime"
            "boolean" -> "Boolean"
            "string?" -> "String"
            "int?" -> "Int"
            "text?" -> "String"
            "date?" -> "LocalDate"
            "dateTime?" -> "LocalDateTime"
            "boolean?" -> "Boolean"
            else -> "Int"
        }
    }

    private fun determineFieldInputType(type: String): String {
        return when (type) {
            "string" -> "text"
            "int" -> "number"
            "date" -> "date"
            "dateTime" -> "datetime-local"
            "boolean" -> "checkbox"
            "string?" -> "text"
            "int?" -> "number"
            "date?" -> "date"
            "dateTime?" -> "datetime-local"
            "boolean?" -> "checkbox"
            else -> "text"
        }
    }

    private fun determineKotlinTypeNullable(type: String): String {
        return when (type) {
            "string" -> "String"
            "int" -> "Int"
            "text" -> "String"
            "date" -> "LocalDate"
            "dateTime" -> "LocalDateTime"
            "boolean" -> "Boolean"
            "string?" -> "String?"
            "int?" -> "Int?"
            "text?" -> "String?"
            "date?" -> "LocalDate?"
            "dateTime?" -> "LocalDateTime?"
            "boolean?" -> "Boolean?"
            else -> "Int"
        }
    }

    private fun determineNullableGraphQLType(type: String): String {
        return when (type) {
            "string" -> "String"
            "int" -> "Int"
            "text" -> "String"
            "date" -> "Date"
            "dateTime" -> "Date"
            "string?" -> "String"
            "int?" -> "Int"
            "text?" -> "String"
            "date?" -> "Date"
            "dateTime?" -> "Date"
            else -> "Int"
        }
    }

    private fun determineGraphQLType(type: String): String {
        return when (type) {
            "string" -> "String!"
            "int" -> "Int!"
            "text" -> "String!"
            "date" -> "Date!"
            "datetime" -> "DateTime!"
            "string?" -> "String"
            "int?" -> "Int"
            "text?" -> "String"
            "date?" -> "Date"
            "datetime?" -> "DateTime"
            else -> "Int!"
        }
    }

    data class Field(
            val name: String,
            val uName: String,
            val lName: String,
            val unames: String,
            val _name: String,
            val dbType: String,
            val kotlinType: String,
            val kotlinAnnotations: String?,
            val someTestValue: String,
            val relationship: Boolean
    )
}
