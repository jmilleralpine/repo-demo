package com.alpinenow.artifacts.uploader

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue

@Ignore
class ArtifactUploaderPluginTest {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;
    private String build = """
        plugin apply: 'com.alpinenow.artifact-uploader'
        group = 'jason'
        version = '1.0'
        dependencies {
            compile 'junit:junit:4.12'
        }
        repositories {
            mavenCentral()
        }
    """

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        Files.write(buildFile.toPath(), build.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING)
    }

    @Test
    public void tryGradleRunner() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("install")
            .build();

        println result
    }

    //@Test
    public void addsTaskToProject() {
        Project project = ProjectBuilder.builder()
            .withName("miller")
            .build()
        project.group = "jason"
        project.version = "1.0"
        project.pluginManager.apply 'com.alpinenow.artifact-uploader'
        project.dependencies.add("compile", "junit:junit:4.12")
        project.repositories.mavenCentral()

        def sources = project.projectDir.toPath().resolve('src').resolve('main').resolve('java')
        def main = Files.createDirectories(sources).resolve('Main.java')
        Files.write(main,
            'public class Main { public static void main(String[] args) { System.out.println("hello, world!"); } }'.getBytes(StandardCharsets.UTF_8)
        )

        assertTrue(Files.exists(main));

        println project.gradle.taskGraph.allTasks

        def archivesConfiguration = project.getConfigurations().getByName(Dependency.ARCHIVES_CONFIGURATION);
        println archivesConfiguration.artifacts
        println archivesConfiguration.artifacts.buildDependencies


        def assemble = project.tasks.getByName("assemble");
        println assemble
        assemble.execute();

        println "yo"
        project.rootDir.eachFileRecurse { file ->
            println file
        }

        def install = project.tasks.getByName("install")
        install.execute()

//        def alpineUpload = (AlpineUpload)project.tasks.getByName(ArtifactUploaderPlugin.UPLOAD_TASK_NAME)
//
//        assertThat alpineUpload, is(notNullValue())
//
//        alpineUpload.execute()
    }
}
