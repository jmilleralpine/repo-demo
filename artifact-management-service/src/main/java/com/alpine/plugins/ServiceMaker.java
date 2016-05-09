package com.alpine.plugins;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by jasonmiller on 5/9/16.
 */
public class ServiceMaker {

    public static RepositorySystemConfiguration.Timeout timeout(long seconds) {
        return new RepositorySystemConfiguration.Timeout(seconds, TimeUnit.SECONDS);
    }

    public static ArtifactManagementService localOnly(Path repo) {
        return new RepositorySystemArtifactManagementService(new RepositorySystemConfiguration(
            repo, true, Collections.<Repo>emptyList(), timeout(1), timeout(1)
        ));
    }
}
