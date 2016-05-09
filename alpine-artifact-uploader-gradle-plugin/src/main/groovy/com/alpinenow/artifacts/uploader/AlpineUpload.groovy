package com.alpinenow.artifacts.uploader

import com.alpine.plugins.ArtifactManagementService
import com.alpine.plugins.ServiceMaker
import org.eclipse.aether.AbstractRepositoryListener
import org.eclipse.aether.RepositoryEvent
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.publish.Publication
import org.gradle.api.publish.internal.PublishOperation
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.internal.publisher.MavenNormalizedPublication
import org.gradle.api.publish.maven.internal.publisher.MavenProjectIdentity
import org.gradle.api.publish.maven.internal.publisher.MavenPublisher
import org.gradle.api.publish.maven.internal.publisher.MavenRemotePublisher
import org.gradle.api.publish.maven.internal.publisher.StaticLockingMavenPublisher
import org.gradle.api.publish.maven.internal.publisher.ValidatingMavenPublisher
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Upload

import java.nio.file.Files
import java.nio.file.Path

class AlpineUpload extends PublishToMavenRepository {

    private def Path internalRepo

    void setInternalRepo(Path internalRepo) {
        this.internalRepo = internalRepo;
    }

    private static void detailDependencies(ResolvedDependency dependency) {
        println dependency.name
        println dependency.allModuleArtifacts
        println dependency.children
        for (child in dependency.children.findAll { c -> c.configuration == dependency.configuration }) {
            detailDependencies(child)
        }
    }

    @TaskAction
    @Override
    public void publish() {

        // this is a bit of a pain, perhaps there's an easier way to get the full dependency structure suitable
        // for offline resolution but I don't know how else to get parent poms

        def upload = project.tasks.getByName(ArtifactUploaderPlugin.INTERNAL_INSTALLER_TASK_NAME)
        upload.execute()

        def localRepo = ServiceMaker.localOnly(internalRepo)
        localRepo.add(new AbstractRepositoryListener() {
            @Override
            void artifactResolved(RepositoryEvent event) {
                println event
            }
        })
        String coordinates = project.group + ':' + project.name + ':' + project.version
        def artifacts = localRepo.resolve(new DefaultArtifact(coordinates));
        println artifacts

        def publication = getPublicationInternal()
        if (publication == null) {
            throw new InvalidUserDataException("The 'publication' property is required");
        }

        MavenArtifactRepository repository = getRepository()
        if (repository == null) {
            throw new InvalidUserDataException("The 'repository' property is required");
        }

        MavenPublisher publisher = publisher()



        new PublishOperation(publication, repository.getName()) {
            @Override
            protected void publish() throws Exception {
                publisher.publish(publication.asNormalisedPublication(), repository);
            }
        }// .run();
    }

    private MavenPublisher publisher() {
        MavenPublisher remotePublisher =
                new MavenRemotePublisher(getLoggingManagerFactory(), getMavenRepositoryLocator(), getTemporaryDirFactory(), getRepositoryTransportFactory());
        MavenPublisher staticLockingPublisher = new StaticLockingMavenPublisher(remotePublisher);
        MavenPublisher validatingPublisher = new ValidatingMavenPublisher(staticLockingPublisher);
        validatingPublisher
    }

    private AlpineNormalizedPublication publication(
        String name, File pomFile, MavenProjectIdentity projectIdentity, MavenArtifact mainArtifact
    ) {
        new AlpineNormalizedPublication(name, pomFile, projectIdentity, Collections.emptySet(), mainArtifact)
    }
}

class AlpineNormalizedPublication extends MavenNormalizedPublication implements Publication {

    AlpineNormalizedPublication(String name, File pomFile, MavenProjectIdentity projectIdentity, Set<MavenArtifact> artifacts, MavenArtifact mainArtifact) {
        super(name, pomFile, projectIdentity, artifacts, mainArtifact)
    }
}
