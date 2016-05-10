package com.alpine.plugins;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple configuration of a repo for loading dependencies
 */
public class Repo {

    private static final Map<String, String> KNOWN_REPOS;

    static {
        Map<String, String> map = new HashMap<>(7);
        map.put("central", "https://repo1.maven.org/maven2/");
        map.put("jcenter", "https://jcenter.bintray.com/");
        map.put("alpine-artifactory", "http://repo.alpinenow.local/artifactory/repo/");
        KNOWN_REPOS = Collections.unmodifiableMap(map);
    }

    final String id;
    final URI uri;

    public Repo(String id) {
        assert KNOWN_REPOS.containsKey(id) : id + " is not a known repo";
        this.id = id;
        this.uri = URI.create(KNOWN_REPOS.get(id));
    }

    public Repo(String id, URI uri) {
        this.id = id;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return id + "(" + uri + ")";
    }
}
