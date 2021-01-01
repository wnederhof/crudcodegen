package com.wouter.crudcodegen.generators.filters.fields

import com.nhaarman.mockitokotlin2.whenever
import com.wouter.crudcodegen.generators.EntityType
import com.wouter.crudcodegen.generators.filters.EntityField.PrimitiveEntityField
import com.wouter.crudcodegen.generators.filters.FieldTemplateFilter.FieldTemplateSettings
import com.wouter.crudcodegen.generators.helpers.NameHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FieldNameFilterTest {

    @Mock
    private lateinit var nameHelper: NameHelper

    @InjectMocks
    private lateinit var fieldNameFilter: FieldNameFilter

    @Test
    fun `enrichProperties creates snake case, pascal case and camel case props per field from camel case`() {
        val fields = listOf(PrimitiveEntityField("dateOfBirth", EntityType.DATE))

        whenever(nameHelper.toDuckCase("dateOfBirth")).thenCallRealMethod()
        whenever(nameHelper.toDashCase("dateOfBirth")).thenCallRealMethod()
        whenever(nameHelper.toUpperCamelCase("dateOfBirth")).thenCallRealMethod()
        whenever(nameHelper.toLowerCamelCase("dateOfBirth")).thenCallRealMethod()

        val actual = fieldNameFilter.enrichProperties(0, FieldTemplateSettings(fields))

        assertThat(actual).hasSize(4)

        assertThat(actual.single { it.name == "fieldNameSnakeCase" }.value).isEqualTo("date_of_birth")
        assertThat(actual.single { it.name == "fieldNameKebabCase" }.value).isEqualTo("date-of-birth")
        assertThat(actual.single { it.name == "fieldNamePascalCase" }.value).isEqualTo("DateOfBirth")
        assertThat(actual.single { it.name == "fieldNameCamelCase" }.value).isEqualTo("dateOfBirth")
    }

    private companion object {
        const val SOME_STRING = "some-irrelevant-string"
    }
}
