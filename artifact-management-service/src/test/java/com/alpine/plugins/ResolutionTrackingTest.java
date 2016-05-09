package com.alpine.plugins;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static com.alpine.plugins.TestHelpers.*;

public class ResolutionTrackingTest {

    private final DirectoryKiller dk = new DirectoryKiller();

    @After
    public void after() {
        // if there were failures this will clean up after them, but we ignore issues
        try { dk.kill(LOCAL_REPO); } catch (IOException ignored) { /* ignored! */ }
    }



    @Test
    public void test() throws Exception {

        final HashSet<Artifact> artifacts = new HashSet<>();

        RepositorySystemArtifactManagementService ams = service(MAVEN_CENTRAL);
        ams.add(new AbstractRepositoryListener() {
            @Override
            public void artifactResolved(RepositoryEvent event) {
                if (event.getExceptions().isEmpty()) {
                    artifacts.add(event.getArtifact());
                }
            }
        });

        ams.resolve(new DefaultArtifact(JUNIT_JAR_COORDS));

        System.out.println(artifacts);
    }

}
