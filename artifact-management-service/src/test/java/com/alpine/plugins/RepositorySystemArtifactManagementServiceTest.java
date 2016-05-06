package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.alpine.plugins.TestHelpers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RepositorySystemArtifactManagementServiceTest {

    private final DirectoryKiller dk = new DirectoryKiller();

    @After
    public void after() {
        // if there were failures this will clean up after them, but we ignore issues
        try { dk.kill(LOCAL_REPO); } catch (IOException ignored) { /* ignored! */ }
        try { dk.kill(LOCAL_REPO_2); } catch (IOException ignored) { /* ignored! */ }
    }

    private void testResolution(RepositorySystemArtifactManagementService service) throws Exception {

        final Collection<Artifact> result =
            service.resolve(new DefaultArtifact(JUNIT_JAR_COORDS));

        assertThat(result.size(), is(2));
        Iterator<Artifact> i = result.iterator();
        Artifact junit = i.next();
        Artifact hamcrest = i.next();

        assertThat(junit.toString(), is(JUNIT_JAR_COORDS));
        assertThat(junit.getFile().getName(), is("junit-4.11.jar"));

        assertThat(hamcrest.toString(), is(HAMCREST_JAR_COORDS));
        assertThat(hamcrest.getFile().getName(), is("hamcrest-core-1.3.jar"));

        // and we should also have the poms available now
        Artifact junitPom = service.find(new DefaultArtifact(JUNIT_POM_COORDS));
        assertThat(junitPom.getFile(), is(notNullValue()));
        assertThat(junitPom.getFile().getName(), is("junit-4.11.pom"));

        Artifact hamcrestPom = service.find(new DefaultArtifact(HAMCREST_POM_COORDS));
        assertThat(hamcrestPom.getFile(), is(notNullValue()));
        assertThat(hamcrestPom.getFile().getName(), is("hamcrest-core-1.3.pom"));
    }

    @Test
    public void testResolution() throws Exception {
        // load from alpine and from maven, to prove that we can, that the basics work

        testResolution(service(ALPINE_ARTIFACTORY));
        dk.kill(LOCAL_REPO);

        testResolution(service(TestHelpers.MAVEN_CENTRAL));
        dk.kill(LOCAL_REPO);
    }

    @Test
    public void testInstallation() throws Exception {

        // one for grabbing the jars we're going to install, connected to our lovely artifactory
        RepositorySystemArtifactManagementService grabber = service(LOCAL_REPO_2, ALPINE_ARTIFACTORY);
        Collection<Artifact> result = grabber.resolve(new DefaultArtifact(JUNIT_JAR_COORDS));

        assertThat(result.size(), is(2));

        // one for installing, connected to nothing
        RepositorySystemArtifactManagementService installer = service();

        // copy the interesting artifacts over
        assertTrue(installer.install(grabber.find(new DefaultArtifact(JUNIT_JAR_COORDS))));
        assertTrue(installer.install(grabber.find(new DefaultArtifact(JUNIT_POM_COORDS))));
        assertTrue(installer.install(grabber.find(new DefaultArtifact(HAMCREST_JAR_COORDS))));
        assertTrue(installer.install(grabber.find(new DefaultArtifact(HAMCREST_POM_COORDS))));
        assertTrue(installer.install(grabber.find(new DefaultArtifact(HAMCREST_PARENT_POM_COORDS))));

        // now we should be able to kill the grabber and resolve Junit successfully from the installer
        dk.kill(LOCAL_REPO_2);
        // verify it is not involved anymore
        assertThat(grabber.find(new DefaultArtifact(JUNIT_JAR_COORDS)).getFile(), is(nullValue()));

        // and now we should be able to resolve junit in our offline repo
        testResolution(installer);
        dk.kill(LOCAL_REPO);
    }

}
