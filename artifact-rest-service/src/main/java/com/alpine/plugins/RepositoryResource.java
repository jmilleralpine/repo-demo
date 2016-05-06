package com.alpine.plugins;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("repo")
public class RepositoryResource {

    private static final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();
    private static final Response CONFLICT = Response.status(Response.Status.CONFLICT).build();
    private static final Response ACCEPTED = Response.accepted().build();

    private final PathParser pathParser;
    private final ArtifactManagementService artifactManagementService;
    private final MimetypesFileTypeMap mimeHelper = new MimetypesFileTypeMap();

    @Inject
    RepositoryResource(PathParser pathParser, ArtifactManagementService artifactManagementService) {
        this.pathParser = pathParser;
        this.artifactManagementService = artifactManagementService;
    }

    private Response serveIfFound(File file) throws Exception {
        if (file != null) {
            System.out.println("responding with " + file.getAbsolutePath());
            return Response.ok(file, mimeHelper.getContentType(file)).build();
        }
        return NOT_FOUND;
    }

    private Response getArtifact(Artifact artifact) throws Exception {
        return serveIfFound(artifactManagementService.find(artifact).getFile());
    }

    private Response getMetadata(Metadata metadata) throws Exception {
        return serveIfFound(artifactManagementService.find(metadata).getFile());
    }

    @GET
    @Path("/{path:.*}")
    public Response get(@PathParam("path") String path) throws Exception {
        Artifact artifact = pathParser.parseArtifact(path);
        Metadata metadata = pathParser.parseMetadata(path);
        if (artifact != null) {
            return getArtifact(artifact);
        } else if (metadata != null) {
            return getMetadata(metadata);
        }

        return NOT_FOUND;
    }

    @PUT
    @Path("/{path:.*}")
    public Response put(@PathParam("path") String path, File input) throws Exception {
        try {
            Artifact artifact = pathParser.parseArtifact(path);

            if (artifact != null) {

                if (artifactManagementService.find(artifact).getFile() != null && !artifact.isSnapshot()) {
                    // do not allow overwriting releases
                    return CONFLICT;
                }

                artifactManagementService.install(artifact.setFile(input));
                return ACCEPTED;
            }

            if (pathParser.parseMetadata(path) != null) {
                // we indicate we're cool with whatever metadata
                // because it appears the clients expect that
                // but we maintain our own, thanks
                return ACCEPTED;
            }

            return NOT_FOUND;
        } finally {
            //noinspection ResultOfMethodCallIgnored
            input.delete(); // quietly
        }
    }
}
