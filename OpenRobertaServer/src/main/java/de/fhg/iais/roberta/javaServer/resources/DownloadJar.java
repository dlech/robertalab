package de.fhg.iais.roberta.javaServer.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.InjectParam;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.util.Pair;

@Path("/download")
public class DownloadJar {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadJar.class);

    private final BrickCommunicator brickCommunicator;

    @Inject
    public DownloadJar(@InjectParam BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response handle() {
        String token = "1Q2W3E4R";
        LOG.info("/download - token from brick: " + token);
        Pair<String, String> jarDescription = this.brickCommunicator.iAmABrickAndWantToWaitForARunButtonPress(token);
        ResponseBuilder builder = Response.status(Status.OK);
        String fileName = jarDescription.getSecond() + ".jar";
        try {
            File file = new File(CompilerWorkflow.BASE_DIR + jarDescription.getFirst() + "/target/" + fileName);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for ( int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
            }
            builder.header("fileName", fileName);
            builder.entity(bos.toByteArray());
            fis.close();
        } catch ( IOException e ) {
            LOG.info("Error @FileInputStream(file)", e);
        }
        return builder.build();
    }
}