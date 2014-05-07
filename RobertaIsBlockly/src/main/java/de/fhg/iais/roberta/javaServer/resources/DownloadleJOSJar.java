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

@Path("/download")
public class DownloadleJOSJar {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadleJOSJar.class);

    @POST
    //@Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response handle(String token) {
        LOG.info("/download" + ", " + "Code from Brick: " + token);
        ResponseBuilder builder = Response.status(Status.OK);
        String fileName = tokenHandler(token); // this will be database access later, get fileName associated with token
        try {
            File file = new File("c:\\temp", fileName); // thows nullpointer e with fileName=null (no token @EV3), TODO need to be handled -> infomessage to user
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for ( int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
            }
            builder.header("fileName", fileName);
            builder.entity(bos.toByteArray());
            fis.close(); // + finally?!

        } catch ( IOException ex ) {
            LOG.info("Error @FileInputStream(file)");
        }
        return builder.build();
    }

    private String tokenHandler(String token) {
        if ( token.equals("ZXCV") ) {
            return "blurp.jar";
        } else {
            return null;
        }
    }
}