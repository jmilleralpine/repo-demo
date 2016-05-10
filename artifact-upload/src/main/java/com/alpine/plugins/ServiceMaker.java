package com.alpine.plugins;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Helper to create the repo service the CLI tool wants
 */
public class ServiceMaker {

    public static ArtifactManagementService service(Path localRepo, Repo remoteRepo) {
        return new RepositorySystemArtifactManagementService(new RepositorySystemConfiguration(
            localRepo,
            true,
            Collections.singletonList(remoteRepo),
            // our internet isn't great sometimes
            new RepositorySystemConfiguration.Timeout(1, TimeUnit.MINUTES),
            new RepositorySystemConfiguration.Timeout(1, TimeUnit.MINUTES)
        ));
    }
}
