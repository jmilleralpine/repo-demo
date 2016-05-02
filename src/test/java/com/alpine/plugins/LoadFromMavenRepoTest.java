package com.alpine.plugins;

import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoadFromMavenRepoTest {

    // local repo location [$HOME/ALPINE_DATA_REPOSITORY/m2]
    // remote repos [none]
    // allow snapshots? [false]
    // connect timeout ms [5000]
    // request timeout ms [10000]

    private static final Path LOCAL_REPO = Paths.get(System.getProperty("user.home")).resolve("ALPINE_TEST_REPO").toAbsolutePath();
    private static final URI M2_REPO = Paths.get(System.getProperty("user.home")).resolve(".m2").resolve("repository").toAbsolutePath().toUri();

    @Test
    public void test() throws Exception {

        List<Repo> repos = new ArrayList<>();
        // repos.add(new Repo("central")); // maven central via HTTPS is well-known
        // repos.add(new Repo("jcenter")); // bintray via HTTPS is well-known
        // repos.add(new Repo("alpine-artifactory", URI.create("http://repo.alpinenow.local/artifactory/repo/")));
        repos.add(new Repo("local-m2", M2_REPO));
        PluginResolver r = new PluginResolver(
            LOCAL_REPO, repos, PluginResolver.AllowSnapshotsOption.NO_SNAPSHOTS, 5000, 10000
        );
        //r.resolve("com.alpinenow:plugin-core:1.5.1");
        final DependencyResult result = r.resolve("junit:junit:4.11");

        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            System.out.println(artifactResult.getArtifact());
            System.out.println(artifactResult.getRepository());
            System.out.println(artifactResult.getArtifact().getFile().toURI().toURL());
            System.out.println();
        }

        // each artifact result holds a file pointing to a jar suitable to
        // be stuffed into a URLClassLoader
    }

}
