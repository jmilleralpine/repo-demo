package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.installation.InstallResult;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalMetadataResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

/**
 * Provides maven-like artifact services, to deal with building classloaders
 * for plugins or installing jdbc drivers or who knows what else?
 *
 * @author jason miller
 */
public interface ArtifactManagementService {

    LocalArtifactResult find(Artifact artifact);

    LocalMetadataResult find(Metadata metadata);

    DependencyResult resolve(Artifact artifact) throws DependencyResolutionException;

    InstallResult install(Artifact artifact) throws InstallationException;
}
