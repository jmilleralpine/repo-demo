package com.alpine.plugins;

import io.takari.filemanager.FileManager;
import io.takari.filemanager.internal.DefaultFileManager;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.SyncContextFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.io.FileProcessor;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves artifact coordinates in similar fashion to maven, leaving jar files
 * in a configured local repository, and producing a list of paths to the relevant
 * jars in the repository, suitable for making into a URLClassLoader, for instance
 */
class PluginResolver {

    enum AllowSnapshotsOption {
        NO_SNAPSHOTS,
        ALLOW_SNAPSHOTS
    }

    private final Path localRepo;
    private final List<RemoteRepository> repos;
    private final long connectTimeoutMillis;
    private final long requestTimeoutMillis;

    private final RepositorySystem system;
    private final RepositorySystemSession session;

    private final RepositoryPolicy snapshotPolicy;
    private static final RepositoryPolicy RELEASE_POLICY =
        new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_FAIL);

    PluginResolver(
        Path localRepo,
        List<Repo> repos,
        AllowSnapshotsOption allowSnapshot,
        long connectTimeoutMillis,
        long requestTimeoutMillis
    ) {
        this.localRepo = localRepo;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.requestTimeoutMillis = requestTimeoutMillis;

        snapshotPolicy =
            new RepositoryPolicy(
                allowSnapshot == AllowSnapshotsOption.ALLOW_SNAPSHOTS,
                RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                RepositoryPolicy.CHECKSUM_POLICY_FAIL
            );

        ArrayList<RemoteRepository> repoMaker = new ArrayList<>(repos.size());
        for (Repo repo : repos) {
            repoMaker.add(newRepo(repo.id, repo.uri));
        }
        this.repos = Collections.unmodifiableList(repoMaker);

        system = newRepositorySystem();
        session = newRepositorySystemSession(system);
    }

    private RemoteRepository newRepo(String id, URI uri) {
        return new RemoteRepository.Builder(id, "default", uri.toString())
            .setReleasePolicy(RELEASE_POLICY)
            .setSnapshotPolicy(snapshotPolicy)
            .build();
    }

    private RepositorySystem newRepositorySystem() {

        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new RuntimeException("Could not create service " + type.getName() + " implemented by " + impl, exception);
            }
        });

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.setService(SyncContextFactory.class, LockingSyncContextFactory.class);
        locator.setService(FileProcessor.class, LockingFileProcessor.class);

        return locator.getService(RepositorySystem.class);
    }

    private static final FileManager takariFileManager = new DefaultFileManager();

    public static class LockingFileProcessor extends io.takari.aether.concurrency.LockingFileProcessor {
        public LockingFileProcessor() {
            super(takariFileManager);
        }
    }

    public static class LockingSyncContextFactory extends io.takari.aether.concurrency.LockingSyncContextFactory {
        public LockingSyncContextFactory() {
            super(takariFileManager);
        }
    }

    private ConflictResolver newConflictResolver() {
        return new ConflictResolver(
            new NearestVersionSelector(),
            new JavaScopeSelector(),
            new SimpleOptionalitySelector(),
            new JavaScopeDeriver()
        );
    }

    private RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        session.setConfigProperty(ConfigurationProperties.CONNECT_TIMEOUT, connectTimeoutMillis);
        session.setConfigProperty(ConfigurationProperties.REQUEST_TIMEOUT, requestTimeoutMillis);
        // this possibly has a weird conflict resolution impact and I'm not convinced it's
        // helpful anyway
        //session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);

        session.setOffline(false);
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);

        session.setDependencyGraphTransformer(newConflictResolver());

        LocalRepository localRepository = new LocalRepository(localRepo.toFile());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));

        session.setTransferListener(new LoggingTransferListener());

        session.setRepositoryListener(new LoggingRepositoryListener());

        return session;
    }

    public DependencyResult resolve(String coordinates) throws DependencyResolutionException {
        return system.resolveDependencies(
            session,
            new DependencyRequest(new CollectRequest(
                new Dependency(new DefaultArtifact(coordinates), "runtime"),
                repos
            ), null)
        );
    }
}
