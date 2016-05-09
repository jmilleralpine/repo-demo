package com.alpine.plugins;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

enum TestHelpers {
    ;

    static final Path LOCAL_REPO = Paths.get("build").resolve("test_repo").toAbsolutePath();
    static final Path LOCAL_REPO_2 = Paths.get("build").resolve("test_repo2").toAbsolutePath();

    static final Repo MAVEN_CENTRAL = new Repo("central");
    static final Repo ALPINE_ARTIFACTORY = new Repo("alpine-artifactory");

    static final String JUNIT_JAR_COORDS = "junit:junit:jar:4.11";
    static final String HAMCREST_JAR_COORDS = "org.hamcrest:hamcrest-core:jar:1.3";
    static final String JUNIT_POM_COORDS = "junit:junit:pom:4.11";
    static final String HAMCREST_POM_COORDS = "org.hamcrest:hamcrest-core:pom:1.3";
    static final String HAMCREST_PARENT_POM_COORDS = "org.hamcrest:hamcrest-parent:pom:1.3";



    public static RepositorySystemArtifactManagementService service() {
        return service(LOCAL_REPO, null);
    }

    public static RepositorySystemArtifactManagementService service(Repo repo) {
        return service(LOCAL_REPO, repo);
    }

    public static RepositorySystemArtifactManagementService service(Path localRepo, Repo repo) {
        return new RepositorySystemArtifactManagementService(new RepositorySystemConfiguration(
            localRepo,
            false,
            repo == null ? Collections.<Repo>emptyList() : Collections.singletonList(repo),
            // our internet isn't great sometimes
            new RepositorySystemConfiguration.Timeout(1, TimeUnit.MINUTES),
            new RepositorySystemConfiguration.Timeout(1, TimeUnit.MINUTES)
        ));
    }

    public static RepositorySystemArtifactManagementService withLogs(RepositorySystemArtifactManagementService service) {
        service.add(new LoggingTransferListener());
        service.add(new LoggingRepositoryListener());
        return service;
    }
}
