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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

        USER_FACADE.getSpotifyAuth(username,code);
    }

    @Path("trackinfo")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response getTrackInfo(@HeaderParam("x-access-token") String token, String body) throws IOException {
        JWTdecoder decoder = new JWTdecoder(token);
        String username = decoder.getUserName();

        if(body != null) {
            JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
            String trackid = jsonBody.get("trackid").getAsString();

            JsonObject responseJSON = API_FACADE.getTrackInfo(username,trackid);
            return Response.ok(GSON.toJson(responseJSON)).build();
        }


        JsonObject responseJSON = API_FACADE.getTrackInfo(username);
        return Response.ok(GSON.toJson(responseJSON)).build();
    }

}
