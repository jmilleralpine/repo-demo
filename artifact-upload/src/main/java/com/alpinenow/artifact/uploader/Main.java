package com.alpinenow.artifact.uploader;

import com.alpine.plugins.ArtifactManagementService;
import com.alpine.plugins.Repo;
import com.alpine.plugins.ServiceMaker;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;

public class Main {

    private static Exception initException;

    public static void main(String[] args) throws Exception {

        Path localRepo =
            Paths.get(System.getProperty("user.home")).resolve(".m2").resolve("repository").toAbsolutePath();
        Repo remoteRepo = new Repo("central");
        Artifact artifactToInstall = null;
        URI alpine = null;

        try {
            if (args.length == 2) {
                // only the artifact!
                artifactToInstall = new DefaultArtifact(args[0]);
                alpine = URI.create(args[1]);
            } else if (args.length == 3) {
                // arg 1 is the remote, arg 2 are the coordinates
                remoteRepo = makeRepo(args[0]);
                artifactToInstall = new DefaultArtifact(args[1]);
                alpine = URI.create(args[2]);
            } else if (args.length == 4) {
                // arg 1 is the local repo, 2 is the remote, 3 is the artifact
                localRepo = Paths.get(args[0]).toAbsolutePath();
                remoteRepo = makeRepo(args[1]);
                artifactToInstall = new DefaultArtifact(args[2]);
                alpine = URI.create(args[3]);
            }
        } catch (Exception eaten) { initException = eaten; }

        if (artifactToInstall == null || remoteRepo == null || localRepo == null || alpine == null) {
            usage();
        } else {
            doIt(localRepo, remoteRepo, artifactToInstall, alpine);
        }
    }

    private static void doIt(Path localRepo, Repo remoteRepo, final Artifact artifactSource, URI alpine) throws Exception {

        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(alpine);
        int status;
        try {
            status = checkAlpineForInstallation(localRepo, remoteRepo, artifactSource, target);
        } finally {
            client.close();
        }

        System.exit(status);
    }

    private static int checkAlpineForInstallation(Path localRepo, Repo remoteRepo, final Artifact artifactSource, WebTarget alpine) throws Exception {

        System.out.format("Checking Alpine server at %s for %s%n", alpine.getUri(), artifactSource);

        Response resolveResponse = alpine.path("resolve").path(artifactSource.toString()).request().get();

        if (resolveResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            return doInstall(localRepo, remoteRepo, artifactSource, alpine.path("repo"));
        } else if (resolveResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            // verify it has everything?
            System.out.format("%s already understands %s%nNothing to do%n", alpine.getUri(), artifactSource);
            return 0;
        }

        System.out.format("An error occurred communicating with Alpine");
        return 1;
    }

    private static int doInstall(Path localRepo, Repo remoteRepo, Artifact artifactSource, WebTarget alpine) throws DependencyResolutionException {

        int status = 0;
        System.out.format("Resolving %s from %s%n", artifactSource, remoteRepo);
        long now = System.currentTimeMillis();

        ArtifactManagementService service = ServiceMaker.service(localRepo, remoteRepo);

        final Set<Artifact> artifacts = new HashSet<>();
        service.add(new AbstractRepositoryListener() {

            @Override
            public void artifactResolved(RepositoryEvent event) {
                Artifact artifact = event.getArtifact();
                System.out.format("resolved %s%n", artifact);
                artifacts.add(artifact);
            }
        });

        service.resolve(artifactSource);

        System.out.format("resolution complete in %dms, uploading to %s%n", (System.currentTimeMillis() - now), alpine.getUri());

        now = System.currentTimeMillis();
        Map<Artifact, Future<Response>> responses = new HashMap<>();
        // kick em all off! ALL OF EM!
        for (Artifact artifact : artifacts) {

            responses.put(artifact,
                alpine.path(localRepo.relativize(artifact.getFile().toPath()).toString())
                    .request()
                    .async()
                    .put(Entity.entity(artifact.getFile(), MediaType.APPLICATION_OCTET_STREAM_TYPE))
            );

            // if we have the sha1, upload that as well
            // also do the MD5?
            Path artifactPath = artifact.getFile().toPath();
            Path sha1Path = artifactPath.resolveSibling(artifactPath.getFileName().toString() + ".sha1");
            if (Files.exists(sha1Path)) {
                DefaultArtifact sha1Artifact = new DefaultArtifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getClassifier(),
                    artifact.getExtension() + ".sha1",
                    artifact.getVersion()
                );
                responses.put(sha1Artifact,
                    alpine.path(localRepo.relativize(sha1Path).toString())
                        .request()
                        .async()
                        .put(Entity.entity(sha1Path.toFile(), MediaType.APPLICATION_OCTET_STREAM_TYPE))
                );
            }
        }

        // ehhh this could be better but i don't care!
        for (Map.Entry<Artifact, Future<Response>> response : responses.entrySet()) {
            try {
                Response r = response.getValue().get();
                if (r.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                    System.out.format("%s upload completed successfully%n", response.getKey());
                } else if (r.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    System.out.format("%s already exists on the remote server, ignoring%n", response.getKey());
                } else {
                    status = 1; // mark us as having failed if things go wrong
                    System.out.format("received status %d while uploading artifact %s from file %s%n", r.getStatus(), response.getKey(), response.getKey().getFile());
                }
            } catch (Exception e) {
                status = 1; // mark us as having failed if things go wrong
                System.out.format("%s failed to upload%n", response.getKey());
                e.printStackTrace(System.out);
            }
        }

        System.out.format("Upload completed in %dms%s%n", (System.currentTimeMillis() - now), (status == 0 ? "" : " with errors"));

        return status;
    }

    private static final String usage =
        "artifact-upload [local repo] [remote repo] <artifact coordinates> <alpine repo URL>\n" +
        "the local repo can be any previously created maven2-formatted repository, or a new directory\n" +
        "   - this repo is used to cache the dependencies and probably is good at the default\n" +
        "   - the default is $HOME/.m2/repository\n" +
        "the remote repo can be the name 'central' or 'jcenter' or a fully qualified URI locating the repo\n" +
        "   - this repo is used to resolve missing dependencies\n" +
        "   - the default is maven central\n" +
        "   - the supported schemes are HTTP and file\n" +
        "the artifact coordinates are standard maven project coordinates for the primary artifact to install\n" +
        "the alpine repo URL is the endpoint from the target alpine installation\n";

    private static void usage() {
        System.out.println(usage);
        if (initException != null) {
            System.out.println("The following exception occurred while reading the command");
            initException.printStackTrace(System.out);
        }
    }

    private static Repo makeRepo(String repo) {
        if ("central".equals(repo) || "jcenter".equals(repo)) {
            return new Repo(repo);
        } else if (repo.startsWith("https://") || repo.startsWith("http://") || repo.startsWith("file://")) {
            return new Repo("upload-source", URI.create(repo));
        }
        return null;
    }
}
