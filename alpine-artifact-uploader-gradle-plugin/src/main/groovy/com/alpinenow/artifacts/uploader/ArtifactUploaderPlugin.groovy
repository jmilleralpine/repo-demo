package com.alpinenow.artifacts.uploader

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.tasks.Upload

import java.nio.file.Files

/**
 * Adds a task to upload an artifact and its dependencies to
 * an alpine instance
 */
class ArtifactUploaderPlugin implements Plugin<Project> {

    public static final String INTERNAL_INSTALLER_TASK_NAME = "alpineInternalInstallerTask";
    public static final String UPLOAD_TASK_NAME = "alpineUpload";


    def internalRepo = Files.createTempDirectory("AlpineUpload").resolve("repo");

    @Override
    void apply(Project target) {
        if (!target.plugins.hasPlugin(ArtifactUploaderPlugin)) {

            // these are both required for us to do anything reasonable
            target.plugins.apply(MavenPlugin)
            target.plugins.apply(JavaPlugin)

            target.plugins.add(this)



            AlpineUpload alpineUpload =
                target.getTasks().create(UPLOAD_TASK_NAME, AlpineUpload.class);
            alpineUpload.setInternalRepo(internalRepo);
            alpineUpload.dependsOn configureRepoInstall(target)
            target.afterEvaluate {

            }
        }
    }

    private Upload configureRepoInstall(Project project) {
        Upload installUpload = project.getTasks().create(INTERNAL_INSTALLER_TASK_NAME, Upload.class);
        installUpload.repositories.maven { repository ->
            repository.name = INTERNAL_INSTALLER_TASK_NAME
            repository.url = internalRepo.toUri().toString()
        }
        Configuration configuration = project.getConfigurations().getByName(Dependency.ARCHIVES_CONFIGURATION);
        installUpload.setConfiguration(configuration);
        installUpload.dependsOn(configuration.artifacts.buildDependencies)

        MavenRepositoryHandlerConvention repositories = new DslObject(installUpload.getRepositories()).getConvention().getPlugin(MavenRepositoryHandlerConvention.class);
        repositories.mavenInstaller()
        installUpload.setDescription("Installs the 'archives' artifacts into the local Maven repository.");
        installUpload
    }
}