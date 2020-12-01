package facades;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.User;
import utils.HttpHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class APIFacade {

    private static EntityManagerFactory emf;
    private static APIFacade instance;
    private static UserFacade userFacade;

    private APIFacade() {
    }

    public static APIFacade getAPIFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new APIFacade();
            userFacade = UserFacade.getUserFacade(emf);
        }
        return instance;
    }

    public JsonObject getTrackInfo(String username) throws IOException {
        EntityManager em = emf.createEntityManager();
        HttpHelper httpHelper = new HttpHelper();
        Map<String,String> headers = new HashMap<>();
        JsonObject responseJSON = new JsonObject();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Accept", "application/json");

        String spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing","GET",headers,"");
        JsonObject spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();

        if(spotifyJson.has("status")){
            userFacade.refreshTokens(username);
            spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing","GET",headers,"");
            spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();
        }
        String trackname = spotifyJson.get("item").getAsJsonObject().get("name").getAsString();
        String tracklength = spotifyJson.get("item").getAsJsonObject().get("duration_ms").getAsString();
        String trackpos = spotifyJson.get("progress_ms").getAsString();
        String albumname = spotifyJson.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
        String trackid = spotifyJson.get("item").getAsJsonObject().get("id").getAsString();
        String artistname = spotifyJson.get("item").getAsJsonObject().get("artists").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();


        responseJSON.addProperty("trackname",trackname);
        responseJSON.addProperty("tracklength",tracklength);
        responseJSON.addProperty("trackpos",trackpos);
        responseJSON.addProperty("artistname",artistname.toString());
        responseJSON.addProperty("albumname",albumname);
        responseJSON.addProperty("trackid",trackid);

        Map<String,String> lyricHeaders = new HashMap<>();
        lyricHeaders.put("Accept", "application/json");
        String lyricURL = "https://orion.apiseeds.com/api/music/lyric/" + responseJSON.get("artistname").getAsString() + "/" + responseJSON.get("trackname").getAsString() + "?apikey=dQR4afKVHwFNzc55VF63L8JtvRXffRl0fAleRmtiErnrWaWMu3gl46LS0lg9opr6";
        JsonObject lyricJson = JsonParser.parseString( httpHelper.sendRequest(lyricURL,"GET",lyricHeaders,"") ).getAsJsonObject();
        responseJSON.addProperty("lyrics", lyricJson.get("track").getAsJsonObject().get("text").getAsString() );

        return responseJSON;
    }

    public JsonObject getTrackInfo(String username, String trackid) throws IOException {
        EntityManager em = emf.createEntityManager();
        HttpHelper httpHelper = new HttpHelper();
        Map<String,String> headers = new HashMap<>();
        JsonObject responseJSON = new JsonObject();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization",user.getAccessToken());

        String spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing","GET",headers,"");
        JsonObject spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();

        if(spotifyJson.has("status")){
            userFacade.refreshTokens(username);
            spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing","GET",headers,"");
            spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();
        }

        String trackname = spotifyJson.get("item").getAsJsonObject().get("name").getAsString();
        String tracklength = spotifyJson.get("item").getAsJsonObject().get("duration_ms").getAsString();
        String trackpos = spotifyJson.get("progress_ms").getAsString();
        String albumname = spotifyJson.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
        String artistname = spotifyJson.get("item").getAsJsonObject().get("artists").getAsJsonArray().get(0).getAsString();


        responseJSON.addProperty("trackname",trackname);
        responseJSON.addProperty("tracklength",tracklength);
        responseJSON.addProperty("trackpos",trackpos);
        responseJSON.addProperty("artistname",artistname.toString());
        responseJSON.addProperty("albumname",albumname);

        String newtrackid = spotifyJson.get("item").getAsJsonObject().get("id").getAsString();
        if(!newtrackid.equals(trackid)){
            String lyricURL = "https://orion.apiseeds.com/api/music/lyric/" + responseJSON.get("artistname").getAsString() + "/" + responseJSON.get("trackname").getAsString() + "?apikey=dQR4afKVHwFNzc55VF63L8JtvRXffRl0fAleRmtiErnrWaWMu3gl46LS0lg9opr6";
            JsonObject lyricJson = JsonParser.parseString( httpHelper.sendRequest(lyricURL,"GET",new HashMap<>(),"") ).getAsJsonObject();
            responseJSON.addProperty("lyrics", lyricJson.get("track").getAsJsonObject().get("text").getAsString() );
            responseJSON.addProperty("trackid",newtrackid);
        }
        responseJSON.addProperty("trackid",trackid);

        return responseJSON;
    }
}
