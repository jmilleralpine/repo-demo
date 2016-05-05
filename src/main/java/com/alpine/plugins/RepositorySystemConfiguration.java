package com.alpine.plugins;

import java.util.Collections;
import java.util.List;

/**
 * Represents the configuration of the repository system
 */
class RepositorySystemConfiguration {

    private boolean allowSnapshots;

    private List<Repo> remoteRepos;

    private String connectTimeout;

    private String requestTimeout;

    boolean allowSnapshots() {
        return allowSnapshots;
    }

    List<Repo> remoteRepos() {
        return Collections.unmodifiableList(remoteRepos);
    }
}
