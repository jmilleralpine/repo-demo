package com.alpine.plugins;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.URLClassLoader;

import static com.alpine.plugins.TestHelpers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

public class PluginLoadingIntegrationTest {

    private final DirectoryKiller dk = new DirectoryKiller();

    @After
    public void after() {
        // if there were failures this will clean up after them, but we ignore issues
        try { dk.kill(LOCAL_REPO); } catch (IOException ignored) { /* ignored! */ }
    }


    @Test
    public void test() throws Exception {

        RepositorySystemArtifactManagementService ams = service(MAVEN_CENTRAL);
        PluginLoadingService pls = new PluginLoadingService(withLogs(ams));

        // we want a classloader that does not include junit or hamcrest
        // some JVMs may not cooperate, which is fine but this test will be meaningless
        // in general, however, this should get us a pre-classpath classloader
        ClassLoader primordial = ClassLoader.getSystemClassLoader().getParent();
        assumeNotNull("could not retrieve a pre-classpath classloader", primordial);
        try {
            primordial.loadClass(Test.class.getCanonicalName());
            assumeTrue("was able to load Test from the presumed primordial classloader", false);
        } catch (ClassNotFoundException cnfe) { /* YES WE WANT THIS EXCEPTION TO HAPPEN */ }

        try (URLClassLoader cl = pls.pluginClassLoader(JUNIT_JAR_COORDS, primordial)) {
            Class<?> testAnnotation = cl.loadClass(Test.class.getCanonicalName());
            assertThat(testAnnotation.getCanonicalName(), is(Test.class.getCanonicalName()));
            assertFalse(testAnnotation == Test.class);

            Class<?> matchers = cl.loadClass(CoreMatchers.class.getCanonicalName());
            assertThat(matchers.getCanonicalName(), is(CoreMatchers.class.getCanonicalName()));
            assertFalse(matchers == CoreMatchers.class);
        }

        // basically if we get here, that worked. yay on us!
    }

}
