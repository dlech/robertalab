package de.fhg.iais.roberta.javaServer.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * REST service for updating brick libraries and menu.<br>
 * Uses openRoberta.properties for file path references.
 * 
 * @author dpyka
 */
@Path("/update")
public class BrickUpdate {
    private static final Logger LOG = LoggerFactory.getLogger(BrickUpdate.class);

    private final String openRobertaRuntime;
    private final String openRobertaShared;
    private final String jsonLib;
    private final String ev3menu;

    @Inject
    public BrickUpdate(
        @Named("runtime.jar.dir.file") String runtime,
        @Named("shared.jar.dir.file") String shared,
        @Named("jsonlib.jar.dir.file") String json,
        @Named("ev3menu.jar.dir.file") String ev3menu) {
        this.openRobertaRuntime = runtime;
        this.openRobertaShared = shared;
        this.jsonLib = json;
        this.ev3menu = ev3menu;
    }

    @GET
    @Path("/runtime")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRuntime() throws FileNotFoundException {
        LOG.info("/update/runtime called");
        File runtimeJar = new File(this.openRobertaRuntime);
        ResponseBuilder response = Response.ok(new FileInputStream(runtimeJar));
        response.header("Content-Disposition", "attachment; filename=\"" + runtimeJar.getName() + "\"");
        return response.build();
    }

    @GET
    @Path("/shared")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getShared() throws FileNotFoundException {
        LOG.info("/update/shared called");
        File sharedJar = new File(this.openRobertaShared);
        ResponseBuilder response = Response.ok(new FileInputStream(sharedJar));
        response.header("Content-Disposition", "attachment; filename=\"" + sharedJar.getName() + "\"");
        return response.build();
    }

    @GET
    @Path("/jsonlib")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getJsonLib() throws FileNotFoundException {
        LOG.info("/update/jsonlib called");
        File jsonJar = new File(this.jsonLib);
        ResponseBuilder response = Response.ok(new FileInputStream(jsonJar));
        response.header("Content-Disposition", "attachment; filename=\"" + jsonJar.getName() + "\"");
        return response.build();
    }

    @GET
    @Path("/ev3menu")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getMenu() throws FileNotFoundException {
        LOG.info("/update/ev3menu called");
        File ev3menuJar = new File(this.ev3menu);
        ResponseBuilder response = Response.ok(new FileInputStream(ev3menuJar));
        response.header("Content-Disposition", "attachment; filename=\"" + ev3menuJar.getName() + "\"");
        return response.build();
    }

}
