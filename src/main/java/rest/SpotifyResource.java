package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import utils.JWTdecoder;

//Todo Remove or change relevant parts before ACTUAL use
@Path("spotify")
public class SpotifyResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    private static final FacadeExample FACADE = FacadeExample.getFacadeExample(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Path("auth")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed("user")
    public String spotifyAuthUser(@HeaderParam("x-access-token") String token, String body) {
        JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
        String code = jsonBody.get("code").getAsString();
        JWTdecoder decoder = new JWTdecoder(token);
        String userName = decoder.getUserName();

        
        
        return "{\"msg\":\"Hello World\"}";
    }

}
