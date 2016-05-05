package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;

public class PluginLoadingServiceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    ArtifactManagementService artifactManagementService;

    @InjectMocks
    PluginLoadingService service;

    private static final String coords = "coords:coords:1.13";

    @Test
    public void testClassLoader() throws Exception {

        File file = Paths.get("build").resolve("classes").toFile();

        given(artifactManagementService.resolve(new DefaultArtifact(coords))).willReturn(
            Collections.singleton(new DefaultArtifact(coords).setFile(file))
        );

        URLClassLoader classLoader = service.pluginClassLoader(coords, ClassLoader.getSystemClassLoader());

        assertThat(classLoader.getURLs(), is(arrayContaining(file.toURI().toURL())));
    }

    @Test
    public void testClassLoaderError() throws Exception {

        DependencyResolutionException cause = new DependencyResolutionException(null, new Exception());

        given(artifactManagementService.resolve(any(Artifact.class))).willThrow(cause);

        try {
            service.pluginClassLoader(coords, ClassLoader.getSystemClassLoader());
            fail();
        } catch (PluginLoadingService.CannotLoadException cle) {
            assertThat(cle.getCause(), is((Throwable)cause));
        }
    }
}