package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import facades.APIFacade;
import facades.UserFacade;
import utils.EMF_Creator;
import facades.FacadeExample;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import utils.JWTdecoder;

import java.io.IOException;

//Todo Remove or change relevant parts before ACTUAL use
@Path("spotify")
public class SpotifyResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    private static final UserFacade USER_FACADE = UserFacade.getUserFacade(EMF);
    private static final APIFacade API_FACADE = APIFacade.getAPIFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Path("auth")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed("user")
    public void spotifyAuthUser(@HeaderParam("x-access-token") String token, String body) throws IOException {
        System.out.println("Received request. Token: " + token + ". Request body: " + body);
        JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
        String code = jsonBody.get("code").getAsString();
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();

        if(jsonBody.has("Identifier")){
            if(jsonBody.get("Identifier").getAsString().equals("ANDROID")){
                USER_FACADE.getSpotifyAuth(username, code,true);
            } else {
                USER_FACADE.getSpotifyAuth(username, code,false);
            }
        } else {
            USER_FACADE.getSpotifyAuth(username, code,false);
        }
    }

    @Path("trackinfo")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response getTrackInfo(@HeaderParam("x-access-token") String token, String body) throws IOException {
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();

        if(!body.equals("")) {
            JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
            String trackid = jsonBody.get("trackid").getAsString();

            JsonObject responseJSON = API_FACADE.getTrackInfo(username,trackid,false);
            System.out.println(responseJSON.toString());
            return Response.ok(GSON.toJson(responseJSON)).build();
        }


        JsonObject responseJSON = API_FACADE.getTrackInfo(username, false);
        return Response.ok(GSON.toJson(responseJSON)).build();
    }

    @Path("play")
    @PUT
    @RolesAllowed("user")
    public void playSpotify(@HeaderParam("x-access-token") String token){
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();
        API_FACADE.playSpotify(username,false);
    }

    @Path("pause")
    @PUT
    @RolesAllowed("user")
    public void pauseSpotify(@HeaderParam("x-access-token") String token){
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();
        API_FACADE.pauseSpotify(username,false);
    }

    @Path("next")
    @POST
    @RolesAllowed("user")
    public void nextSpotifyTrack(@HeaderParam("x-access-token") String token){
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();
        API_FACADE.nextSpotifyTrack(username,false);
    }

    @Path("previous")
    @POST
    @RolesAllowed("user")
    public void previousSpotifyTrack(@HeaderParam("x-access-token") String token){
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();
        API_FACADE.prevSpotifyTrack(username,false);
    }

}
