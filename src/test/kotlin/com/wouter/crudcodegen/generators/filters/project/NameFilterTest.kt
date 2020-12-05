package com.wouter.crudcodegen.generators.filters.project

import com.nhaarman.mockitokotlin2.mock
import com.wouter.crudcodegen.generators.filters.ProjectTemplateFilter
import com.wouter.crudcodegen.generators.filters.ProjectTemplateFilter.ProjectTemplateSettings
import com.wouter.crudcodegen.generators.helpers.NameHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class NameFilterTest {

    @Autowired
    private lateinit var nameFilter: NameFilter

    @Test
    fun `enrichProperties creates different variants of the name`() {
        val settings = ProjectTemplateSettings(
                groupId = "com.somecorp",
                artifactId = "projects",
                name = "ProjectManager",
                targetPath = mock()
        )

        val actual = nameFilter.enrichProperties(settings)

        assertThat(actual).hasSize(9)

        assertThat(actual.single { it.name == "nameCamelCase"}.value).isEqualTo("projectManager")
        assertThat(actual.single { it.name == "namePascalCase"}.value).isEqualTo("ProjectManager")
        assertThat(actual.single { it.name == "namePluralCamelCase"}.value).isEqualTo("projectManagers")
        assertThat(actual.single { it.name == "nameLowerCase"}.value).isEqualTo("projectmanager")
        assertThat(actual.single { it.name == "namePluralPascalCase"}.value).isEqualTo("ProjectManagers")
        assertThat(actual.single { it.name == "namePluralSnakeCase"}.value).isEqualTo("project_managers")
        assertThat(actual.single { it.name == "nameKebabCase"}.value).isEqualTo("project-manager")
        assertThat(actual.single { it.name == "namePluralKebabCase"}.value).isEqualTo("project-managers")
        assertThat(actual.single { it.name == "nameScreamingSnakeCase"}.value).isEqualTo("PROJECT_MANAGER")
    }
}