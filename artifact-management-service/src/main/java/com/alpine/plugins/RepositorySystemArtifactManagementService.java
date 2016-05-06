package com.alpine.plugins;

import io.takari.filemanager.FileManager;
import io.takari.filemanager.internal.DefaultFileManager;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.SyncContextFactory;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallResult;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.ArtifactResult;
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

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manages artifacts using the maven repository system
 *
 * @author jasonmiller
 */
class RepositorySystemArtifactManagementService implements ArtifactManagementService {

    private final Path localRepo;
    private final List<RemoteRepository> repos;
    private final long connectTimeoutMillis;
    private final long requestTimeoutMillis;

    private final RepositorySystem system;
    private final RepositorySystemSession session;

    private final RepositoryPolicy snapshotPolicy;
    private static final RepositoryPolicy RELEASE_POLICY =
        new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_FAIL);

    @Inject
    RepositorySystemArtifactManagementService(RepositorySystemConfiguration configuration) {

        this.localRepo = configuration.localRepo();
        this.connectTimeoutMillis = configuration.connectTimeout().toMillis();
        this.requestTimeoutMillis = configuration.requestTimeout().toMillis();

        assert localRepo != null : "local repo must not be null";
        assert connectTimeoutMillis > 0 : "connect timeout must be > 0ms";
        assert requestTimeoutMillis > 0 : "request timeout must be > 0ms";

        snapshotPolicy =
            new RepositoryPolicy(
                configuration.allowSnapshots(),
                RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                RepositoryPolicy.CHECKSUM_POLICY_WARN
            );

        ArrayList<RemoteRepository> repoMaker = new ArrayList<>(configuration.remoteRepos().size());
        for (Repo repo : configuration.remoteRepos()) {
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

    @SuppressWarnings("WeakerAccess") // needs to be public so the locator can see it
    public static class LockingFileProcessor extends io.takari.aether.concurrency.LockingFileProcessor {
        public LockingFileProcessor() {
            super(takariFileManager);
        }
    }

    @SuppressWarnings("WeakerAccess") // needs to be public so the locator can see it
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

        session.setOffline(repos.isEmpty());
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);

        session.setDependencyGraphTransformer(newConflictResolver());

        LocalRepository localRepository = new LocalRepository(localRepo.toFile());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));

        session.setTransferListener(new LoggingTransferListener());

        session.setRepositoryListener(new LoggingRepositoryListener());

        return session;
    }

    @Override
    public Artifact find(Artifact artifact) {
        assert artifact != null && artifact.getFile() == null : "can only find a non-null, unresolved artifact";

        LocalArtifactResult result =
            session.getLocalRepositoryManager().find(session, new LocalArtifactRequest(artifact, null, null));
        return artifact.setFile(result.getFile());
    }

    @Override
    public Metadata find(Metadata metadata) {
        assert metadata != null && metadata.getFile() == null : "can only find a non-null, unresolved metadata";

        LocalMetadataResult result =
            session.getLocalRepositoryManager().find(session, new LocalMetadataRequest(metadata, null, null));
        return metadata.setFile(result.getFile());
    }

    @Override
    public Collection<Artifact> resolve(Artifact artifact) throws DependencyResolutionException {
        assert artifact != null && artifact.getFile() == null : "can only resolve a non-null, unresolved artifact";

        DependencyResult result = system.resolveDependencies(
            session,
            // yay java, objects in objects in objects!
            new DependencyRequest(new CollectRequest(new Dependency(artifact, "runtime"), repos), null)
        );

        ArrayList<Artifact> response = new ArrayList<>(result.getArtifactResults().size());

        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            response.add(artifactResult.getArtifact());
        }

        return Collections.unmodifiableList(response);
    }

    @Override
    public boolean install(Artifact artifact) throws InstallationException {
        assert artifact != null && artifact.getFile() != null : "can only install a non-null, resolved artifact";

        InstallResult result = system.install(session, new InstallRequest().addArtifact(artifact));
        return result.getArtifacts().size() == 1 &&
                result.getArtifacts().iterator().next().toString().equals(artifact.toString());
    }
}
