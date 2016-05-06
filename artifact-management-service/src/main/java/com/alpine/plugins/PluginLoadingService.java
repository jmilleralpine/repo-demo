package com.alpine.plugins;

import org.eclipse.aether.artifact.DefaultArtifact;

import javax.inject.Inject;
import java.net.URLClassLoader;

/**
 * Produces a plugin object
 */
public class PluginLoadingService {

    public static class CannotLoadException extends RuntimeException {

        CannotLoadException(String coords, Throwable cause) {
            super("cannot load plugin for coordinates " + coords, cause);
        }
    }

    private final ArtifactManagementService artifactManagementService;

    @Inject
    PluginLoadingService(ArtifactManagementService artifactManagementService) {
        this.artifactManagementService = artifactManagementService;
    }

    public URLClassLoader pluginClassLoader(String pluginCoords, ClassLoader parentClassLoader) throws CannotLoadException {

        try {

            return new ArtifactClassLoader(
                parentClassLoader,
                artifactManagementService.resolve(new DefaultArtifact(pluginCoords))
            );

        } catch (Exception e) {
            throw new CannotLoadException(pluginCoords, e);
        }

    }
}
