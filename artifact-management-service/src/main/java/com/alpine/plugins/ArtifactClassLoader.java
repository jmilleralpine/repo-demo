package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * A classloader built from a collection of installed artifacts
 */
class ArtifactClassLoader extends URLClassLoader {

    private static URL[] urlsFromArtifacts(Collection<Artifact> artifacts) {
        try {
            URL[] result = new URL[artifacts.size()];
            int i = 0;
            for (Artifact artifact : artifacts) {
                assert artifact.getFile() != null : "cannot add unresolved artifacts to a classloader!";
                result[i++] = artifact.getFile().toURI().toURL();
            }
            return result;
        } catch (MalformedURLException mue) {
            // should never happen, hence assertion error
            throw new AssertionError("malformed artifacts", mue);
        }
    }

    ArtifactClassLoader(Collection<Artifact> artifacts, ClassLoader parent) {
        super(urlsFromArtifacts(artifacts), parent);
    }
}
