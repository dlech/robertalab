package de.fhg.iais.roberta.javaServer.resources;

import java.io.File;
import java.nio.file.Files;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/download")
public class DownloadleJOSJar {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadleJOSJar.class);

    // TODO request + response header
    @POST
    //@Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response handle(String request) throws Exception {
        LOG.info("/download" + "," + "Code from Brick: " + request);
        File file = new File("c:\\temp\\HelloWorld2.jar");
        byte[] content = Files.readAllBytes(file.toPath());

        return Response.ok(content).build();
    }
}