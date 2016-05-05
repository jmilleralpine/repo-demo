package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallResult;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class RepositorySystemArtifactManagementServiceTest {

    static class DirectoryKiller extends SimpleFileVisitor<Path> {

        void kill(Path path) throws IOException {
            Files.walkFileTree(path, this);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    DirectoryKiller directoryKiller = new DirectoryKiller();

    // local repo location [$HOME/ALPINE_DATA_REPOSITORY/m2]
    // remote repos [none]
    // allow snapshots? [false]
    // connect timeout ms [5000]
    // request timeout ms [10000]

    private static final Path LOCAL_REPO = Paths.get(System.getProperty("user.home")).resolve("ALPINE_TEST_REPO").toAbsolutePath();
    private static final Path M2_REPO = Paths.get(System.getProperty("user.home")).resolve(".m2").resolve("repository").toAbsolutePath();

    @After
    public void after() throws IOException {
        //directoryKiller.kill(LOCAL_REPO);
    }

    //@Ignore
    @Test
    public void basicSillyTest() throws Exception {

        List<Repo> repos = new ArrayList<>();
        // repos.add(new Repo("central")); // maven central via HTTPS is well-known
        // repos.add(new Repo("jcenter")); // bintray via HTTPS is well-known
        repos.add(new Repo("alpine-artifactory")); // our artifactory! only works on the network
        //repos.add(new Repo("local-m2", M2_REPO.toUri()));
        RepositorySystemArtifactManagementService r = new RepositorySystemArtifactManagementService(
            LOCAL_REPO, repos,
            RepositorySystemArtifactManagementService.AllowSnapshotsOption.NO_SNAPSHOTS,
            5, TimeUnit.SECONDS,
            10, TimeUnit.SECONDS
        );
        //r.resolve("com.alpinenow:plugin-core:1.5.1");
        //r.resolve("junit:junit:4.11");
        final DependencyResult result =
            r.resolve(new DefaultArtifact("maven.upload:test-test-test:1.0-SNAPSHOT"));

        System.out.println(result.getRequest().getCollectRequest().getTrace());
        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            System.out.println(artifactResult.getArtifact());
            System.out.println(artifactResult.getRepository());
            System.out.println(artifactResult.getArtifact().getFile().toURI().toURL());
            System.out.println(artifactResult.getArtifact().getProperties());
            System.out.println();
        }

        // each artifact result holds a file pointing to a jar suitable to
        // be stuffed into a URLClassLoader
    }

    @Ignore
    @Test
    public void testInstall() throws Exception {

        RepositorySystemArtifactManagementService r = new RepositorySystemArtifactManagementService(
                LOCAL_REPO, Collections.<Repo>emptyList(),
                RepositorySystemArtifactManagementService.AllowSnapshotsOption.NO_SNAPSHOTS,
                5, TimeUnit.SECONDS,
                10, TimeUnit.SECONDS
        );




        //  String groupId, String artifactId, String classifier, String extension, String version


        InstallResult installResult =
            r.install(
                new DefaultArtifact("jballs:jballs:4.12")
                    .setFile(M2_REPO.resolve("junit/junit/4.12/junit-4.12.pom").toFile()));

        for (Artifact artifact : installResult.getArtifacts()) {
            System.out.println(artifact);
        }

    }

}
