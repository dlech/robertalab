package de.fhg.iais.roberta.javaServer.resources;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.util.Pair;

@Path("/download")
public class DownloadJar {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadJar.class);

    private final BrickCommunicator brickCommunicator;
    private final String pathToCrosscompilerBaseDir;

    @Inject
    public DownloadJar(BrickCommunicator brickCommunicator, @Named("crosscompiler.basedir") String pathToCrosscompilerBaseDir) {
        this.brickCommunicator = brickCommunicator;
        this.pathToCrosscompilerBaseDir = pathToCrosscompilerBaseDir;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response handle(JSONObject requestEntity) throws JSONException {
        String token = requestEntity.getString("token");
        LOG.info("/download - request for token " + token);

        Pair<String, String> jarDescription = this.brickCommunicator.iAmABrickAndWantToWaitForARunButtonPress(token);
        String fileName = jarDescription.getSecond() + ".jar";
        File jarFile = new File(this.pathToCrosscompilerBaseDir + jarDescription.getFirst() + "/target/" + fileName);
        ResponseBuilder response = Response.ok(jarFile);
        response.header("Content-Disposition", "attachment; filename=" + fileName);
        return response.build();
    }
}