package com.alpine.plugins;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Represents the configuration of the repository system
 */
class RepositorySystemConfiguration {

    static final class Timeout {
        private final long length;
        private final TimeUnit unit;

        Timeout(long length, TimeUnit unit) {
            this.length = length;
            this.unit = unit;
        }

        long length() {
            return length;
        }

        TimeUnit unit() {
            return unit;
        }

        long toMillis() {
            return TimeUnit.MILLISECONDS.convert(length, unit);
        }
    }

    private Path localRepo;
    private boolean allowSnapshots;
    private List<Repo> remoteRepos;
    private Timeout connectTimeout;
    private Timeout requestTimeout;

    RepositorySystemConfiguration(
        Path localRepo,
        boolean allowSnapshots,
        List<Repo> remoteRepos,
        Timeout connectTimeout,
        Timeout requestTimeout
    ) {
        this.localRepo = localRepo;
        this.allowSnapshots = allowSnapshots;
        this.remoteRepos = remoteRepos;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    Path localRepo() {
        return localRepo;
    }

    boolean allowSnapshots() {
        return allowSnapshots;
    }

    List<Repo> remoteRepos() {
        return remoteRepos;
    }

    Timeout connectTimeout() {
        return connectTimeout;
    }

    Timeout requestTimeout() {
        return requestTimeout;
    }
}
