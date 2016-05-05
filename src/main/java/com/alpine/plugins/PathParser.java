package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts incoming paths in m2 repository form into
 * artifact or metadata instances
 */
class PathParser {

    private static final Pattern VERSIONED_METADATA_PATTERN =
        Pattern.compile("^((?:[\\w-]+/)+?)([\\w-]+)/(\\d[\\w\\.-]+)/(maven-metadata\\.xml(?:\\.sha1|\\.md5)?)$");

    private static final Pattern ARTIFACT_METADATA_PATTERN =
        Pattern.compile("^((?:[\\w-]+/)+?)([\\w-]+)/(maven-metadata\\.xml(?:\\.sha1|\\.md5)?)$");

    /** matches the canonical release path, and snapshots that aren't named with timestamps */
    private static final Pattern RELEASE_PATH_PATTERN =
        Pattern.compile("^((?:[\\w-]+/)+?)([\\w-]+)/(\\d[\\w\\.-]+)/\\2-\\3(?:-([a-zA-Z]+))?\\.([\\w\\.-]+)?$");

    /** matches snapshots named with timestamps */
    private static final Pattern SNAPSHOT_PATH_PATTERN =
        Pattern.compile("^((?:[\\w-]+/)+?)([\\w-]+)/(\\d[\\w\\.-]*?)-SNAPSHOT/\\2-\\3-\\d{8}\\.\\d{6}-\\d+(?:-([a-zA-Z]+))?\\.([\\w\\.-]+)?$");

    Metadata parseMetadata(String pathSegment) {

        Matcher v = VERSIONED_METADATA_PATTERN.matcher(pathSegment);
        if (v.matches()) {
            return new DefaultMetadata(
                processGroupId(v.group(1)),
                v.group(2),
                v.group(3),
                v.group(4),
                Metadata.Nature.RELEASE_OR_SNAPSHOT
            );
        }

        Matcher a = ARTIFACT_METADATA_PATTERN.matcher(pathSegment);
        if (a.matches()) {
            return new DefaultMetadata(
                processGroupId(a.group(1)),
                a.group(2),
                a.group(3),
                Metadata.Nature.RELEASE_OR_SNAPSHOT
            );
        }

        return null;
    }

    Artifact parseArtifact(String pathSegment) {

        Matcher r = RELEASE_PATH_PATTERN.matcher(pathSegment);
        if (r.matches()) {
            return new DefaultArtifact(
                processGroupId(r.group(1)),
                r.group(2),
                r.group(4),
                r.group(5),
                r.group(3)
            );
        }

        Matcher s = SNAPSHOT_PATH_PATTERN.matcher(pathSegment);
        if (s.matches()) {
            return new DefaultArtifact(
                processGroupId(s.group(1)),
                s.group(2),
                s.group(4),
                s.group(5),
                s.group(3) + "-SNAPSHOT"
            );
        }

        return null;
    }

    private String processGroupId(String groupIdWithSlashes) {
        return groupIdWithSlashes.substring(0, groupIdWithSlashes.length() - 1).replace('/', '.');
    }
}
