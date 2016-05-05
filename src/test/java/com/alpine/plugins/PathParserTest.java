package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PathParserTest {


    PathParser pta = new PathParser();


    @Test
    public void test() {

        Artifact a;

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12.jar");
        assertThat(a.toString(), is("junit:junit:jar:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12.pom");
        assertThat(a.toString(), is("junit:junit:pom:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12-javadoc.jar");
        assertThat(a.toString(), is("junit:junit:jar:javadoc:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12-sources.jar");
        assertThat(a.toString(), is("junit:junit:jar:sources:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.jar");
        assertThat(a.toString(), is("junit:junit:jar:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.pom");
        assertThat(a.toString(), is("junit:junit:pom:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-javadoc.jar");
        assertThat(a.toString(), is("junit:junit:jar:javadoc:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-sources.jar");
        assertThat(a.toString(), is("junit:junit:jar:sources:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12.jar.sha1");
        assertThat(a.toString(), is("junit:junit:jar.sha1:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12.pom.sha1");
        assertThat(a.toString(), is("junit:junit:pom.sha1:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12-javadoc.jar.sha1");
        assertThat(a.toString(), is("junit:junit:jar.sha1:javadoc:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.12/junit-4.12-sources.jar.sha1");
        assertThat(a.toString(), is("junit:junit:jar.sha1:sources:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.jar");
        assertThat(a.toString(), is("junit:junit:jar:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.pom");
        assertThat(a.toString(), is("junit:junit:pom:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-javadoc.jar");
        assertThat(a.toString(), is("junit:junit:jar:javadoc:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-sources.jar");
        assertThat(a.toString(), is("junit:junit:jar:sources:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12.pom");
        assertThat(a.toString(), is("org.artifact.junit:junit:pom:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12-javadoc.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:javadoc:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12-sources.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:sources:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.pom");
        assertThat(a.toString(), is("org.artifact.junit:junit:pom:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-javadoc.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:javadoc:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-sources.jar");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar:sources:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12.pom.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:pom.md5:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12-javadoc.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:javadoc:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.12/junit-4.12-sources.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:sources:4.12"));
        assertFalse(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1.pom.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:pom.md5:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-javadoc.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:javadoc:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());

        a = pta.parseArtifact("org/artifact/junit/junit/4.13-SNAPSHOT/junit-4.13-20160504.201445-1-sources.jar.md5");
        assertThat(a.toString(), is("org.artifact.junit:junit:jar.md5:sources:4.13-SNAPSHOT"));
        assertTrue(a.isSnapshot());
    }
}