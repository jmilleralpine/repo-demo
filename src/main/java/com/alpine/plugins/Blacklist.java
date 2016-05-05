package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;

import java.util.*;

/**
 * List of group/artifacts to disallow, such as logging packages
 * and plugin implementation libraries
 */
public class Blacklist {

    private static Set<String> asSet(String...strings) {
        Set<String> result = new HashSet<>(strings.length);
        Collections.addAll(result, strings);
        return Collections.unmodifiableSet(result);
    }

    private static final Set<String> WHOLE_GROUP = Collections.unmodifiableSet(new HashSet<String>());

    private final Map<String, Set<String>> DISALLOWED;

    {
        Map<String, Set<String>> builder = new HashMap<>();
        builder.put("org.apache.logging.log4j", WHOLE_GROUP);
        builder.put("log4j", WHOLE_GROUP);
        builder.put("org.slf4j", WHOLE_GROUP);
        builder.put("com.alpine", asSet( // make this meaningful!
            "plugin-engine",
            "plugin-core"
        ));
        DISALLOWED = Collections.unmodifiableMap(builder);
    }

    boolean permitted(Artifact artifact) {

        Set<String> dropArtifacts = DISALLOWED.get(artifact.getGroupId());

        return dropArtifacts != null &&
                (dropArtifacts == WHOLE_GROUP || dropArtifacts.contains(artifact.getArtifactId()));
    }

}
