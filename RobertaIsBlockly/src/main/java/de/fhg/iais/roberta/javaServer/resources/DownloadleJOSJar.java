package de.fhg.iais.roberta.javaServer.resources;

import java.io.File;
import java.nio.file.Files;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/download")
public class DownloadleJOSJar {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadleJOSJar.class);

    @POST
    @Produces("application/x-java-serialized-object")
    public Response handle() throws Exception {
        LOG.info("/download");
        File file = new File("c:\\HelloWorld.jar");
        byte[] content = Files.readAllBytes(file.toPath());
        return Response.ok(content).build();
    }
}