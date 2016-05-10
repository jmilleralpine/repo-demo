package com.alpine.plugins;


import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.DependencyResolutionException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.alpine.plugins.Responses.*;

@Path("resolve")
public class ResolveResource {

    private final ArtifactManagementService artifactManagementService;

    @Inject
    ResolveResource(ArtifactManagementService artifactManagementService) {
        this.artifactManagementService = artifactManagementService;
    }

    // used to verify that the installation worked successfully
    @GET
    @Path("/{path:.*}")
    public Response resolve(@PathParam("path") String path) throws Exception {
        try {
            java.nio.file.Path localRepo = artifactManagementService.localRepo();
            StringBuilder result = new StringBuilder(8192).append('{'); // 8k is a good guess i suppose
            for (Artifact artifact : artifactManagementService.resolve(new DefaultArtifact(path))) {
                result
                    .append('"').append(artifact.toString()).append('"')
                    .append('=')
                    .append('"').append(localRepo.relativize(artifact.getFile().toPath())).append('"')
                    .append(',');
            }
            result.replace(result.length() - 1, result.length(), "}");
            return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (DependencyResolutionException dre) {
            return NOT_FOUND;
        }
    }
}
