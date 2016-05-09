package com.alpine.plugins;

import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.transfer.TransferListener;

import java.util.Collection;

/**
 * <p>
 * Exposes maven-like artifact services, to deal with building classloaders
 * for plugins or installing jdbc drivers or who knows what else? Presumably
 * connects to one or more repositories to find its artifacts, although it might
 * just be a giant buffer in the heap for all you know
 *
 * <p>
 * The methods are specified to take interfaces from the aether library as
 * parameters. If you don't have a reason to use something different,
 * {@link org.eclipse.aether.artifact.DefaultArtifact} and
 * {@link org.eclipse.aether.metadata.DefaultMetadata} will do what you need
 *
 * <p>
 * Generally speaking, all methods must be safe for multiple threads to invoke them
 * simultaneously, and all methods will likely perform some sort of synchronous,
 * blocking I/O. If this is unacceptable for your context, you are responsible
 * to ameliorate, e.g. put it in a threadpool or stick it in an actor
 *
 * <p>
 * There should be higher level services that expose something more useful than
 * a bare list of jars
 *
 * @author jason miller
 */
public interface ArtifactManagementService {

    /**
     * check for the existence of the given artifact. If present, the file on the
     * returned instance will be set
     */
    Artifact find(Artifact artifact);

    /**
     * check for the existence of the given metadata. If present, the file on the
     * returned instance will be set
     */
    Metadata find(Metadata metadata);

    /**
     * <p>
     * Resolves a given artifact and its dependency graph, returning a collection
     * of resolved artifacts that point to local files
     *
     * <p>
     * This method can potentially perform a tremendous amount of synchronous I/O,
     * including internet traversal, so keep that in mind when invoking it
     *
     * @throws DependencyResolutionException when the artifact cannot be resolved
     */
    Collection<Artifact> resolve(Artifact artifact) throws DependencyResolutionException;

    /**
     * <p>
     * Installs the given artifact, which must include a file, returning a success flag
     *
     * @throws InstallationException when the artifact cannot be installed
     */
    boolean install(Artifact artifact) throws InstallationException;

    void add(RepositoryListener listener);

    void remove(RepositoryListener listener);

    void add(TransferListener listener);

    void remove(TransferListener listener);
}
