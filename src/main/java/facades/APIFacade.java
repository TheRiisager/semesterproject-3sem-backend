package facades;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.User;
import utils.HttpHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public void playSpotify(String username, boolean triedRefresh) {
        HttpHelper httpHelper = new HttpHelper();
        EntityManager em = emf.createEntityManager();
        Map<String, String> headers = new HashMap<>();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Content-Length", "0");
        headers.put("Host","87.50.6.250:8080");

        try {
            httpHelper.sendRequest("https://api.spotify.com/v1/me/player/play","PUT",headers,"");
        } catch (IOException e) {
            if(triedRefresh) {
                System.out.println("Could not alter playback of users spotify. User: " + user.getUserName() + ". Error: " + e.getLocalizedMessage());
            } else {
                userFacade.refreshTokens(username);
                playSpotify(username,true);
            }
        }
    }

    public void pauseSpotify(String username,boolean triedRefresh){
        HttpHelper httpHelper = new HttpHelper();
        EntityManager em = emf.createEntityManager();
        Map<String, String> headers = new HashMap<>();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Content-Length", "0");
        try {
            httpHelper.sendRequest("https://api.spotify.com/v1/me/player/pause","PUT",headers,"");
        } catch (IOException e) {
            if(triedRefresh) {
                System.out.println("Could not alter playback of users spotify. User: " + user.getUserName() + ". Error: " + e.getLocalizedMessage() + ". headers: " + headers.toString());
            } else {
                userFacade.refreshTokens(username);
                pauseSpotify(username,true);
            }
        }
    }

    public void nextSpotifyTrack(String username,boolean triedRefresh){
        HttpHelper httpHelper = new HttpHelper();
        EntityManager em = emf.createEntityManager();
        Map<String, String> headers = new HashMap<>();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Content-Length", "0");
        try {
            httpHelper.sendRequest("https://api.spotify.com/v1/me/player/next","POST",headers,"");
        } catch (IOException e) {
            if(triedRefresh) {
                System.out.println("Could not alter playback of users spotify. User: " + user.getUserName() + ". Error: " + e.getLocalizedMessage());
            } else {
                userFacade.refreshTokens(username);
                nextSpotifyTrack(username,true);
            }
        }
    }

    public void prevSpotifyTrack(String username,boolean triedRefresh){
        HttpHelper httpHelper = new HttpHelper();
        EntityManager em = emf.createEntityManager();
        Map<String, String> headers = new HashMap<>();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Content-Length", "0");
        try {
            httpHelper.sendRequest("https://api.spotify.com/v1/me/player/previous","POST",headers,"");
        } catch (IOException e) {
            if(triedRefresh) {
                System.out.println("Could not alter playback of users spotify. User: " + user.getUserName() + ". Error: " + e.getLocalizedMessage());
            } else {
                userFacade.refreshTokens(username);
                prevSpotifyTrack(username,true);
            }
        }
    }

    public JsonObject getTrackInfo(String username, boolean triedRefresh) throws IOException {
        EntityManager em = emf.createEntityManager();
        HttpHelper httpHelper = new HttpHelper();
        Map<String, String> headers = new HashMap<>();
        JsonObject responseJSON = new JsonObject();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Accept", "application/json");
        JsonObject spotifyJson;

        try {
            String spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing", "GET", headers, "");
            if (spotifyResponse.equals("")) {
                spotifyJson = new JsonObject();
                spotifyJson.addProperty("trackname", "");
                spotifyJson.addProperty("tracklength", "");
                spotifyJson.addProperty("trackpos", "");
                spotifyJson.addProperty("artistname", "");
                spotifyJson.addProperty("albumname", "");
                spotifyJson.addProperty("trackid", "");
                spotifyJson.addProperty("lyrics", "");
                return spotifyJson;
            }
            spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();

        } catch (IOException e) {
            spotifyJson = new JsonObject();
            if(triedRefresh){
                spotifyJson.addProperty("error", e.getLocalizedMessage());
                return spotifyJson;
            } else {
                userFacade.refreshTokens(username);
                return getTrackInfo(username,true);
            }
        }

        String trackname = spotifyJson.get("item").getAsJsonObject().get("name").getAsString();
        String tracklength = spotifyJson.get("item").getAsJsonObject().get("duration_ms").getAsString();
        String trackpos = spotifyJson.get("progress_ms").getAsString();
        String albumname = spotifyJson.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
        String trackid = spotifyJson.get("item").getAsJsonObject().get("id").getAsString();
        String artistname = spotifyJson.get("item").getAsJsonObject().get("artists").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();

        responseJSON.addProperty("trackname", trackname);
        responseJSON.addProperty("tracklength", tracklength);
        responseJSON.addProperty("trackpos", trackpos);
        responseJSON.addProperty("artistname", artistname.toString());
        responseJSON.addProperty("albumname", albumname);
        responseJSON.addProperty("trackid", trackid);
        responseJSON.addProperty("lyrics", getLyrics(responseJSON.get("artistname").getAsString(), responseJSON.get("trackname").getAsString()));

        return responseJSON;
    }

    public JsonObject getTrackInfo(String username, String trackid,boolean triedRefresh) throws IOException {
        EntityManager em = emf.createEntityManager();
        HttpHelper httpHelper = new HttpHelper();
        Map<String, String> headers = new HashMap<>();
        JsonObject responseJSON = new JsonObject();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        headers.put("Authorization", "Bearer " + user.getAccessToken());
        headers.put("Accept", "application/json");
        JsonObject spotifyJson;

        try {
            String spotifyResponse = httpHelper.sendRequest("https://api.spotify.com/v1/me/player/currently-playing", "GET", headers, "");
            if (spotifyResponse.equals("")) {

                spotifyJson = new JsonObject();

                spotifyJson.addProperty("trackname", "");
                spotifyJson.addProperty("tracklength", "");
                spotifyJson.addProperty("trackpos", "");
                spotifyJson.addProperty("artistname", "");
                spotifyJson.addProperty("albumname", "");
                spotifyJson.addProperty("trackid", "");
                spotifyJson.addProperty("lyrics", "");

                return spotifyJson;

            }
            spotifyJson = JsonParser.parseString(spotifyResponse).getAsJsonObject();

        } catch (IOException e) {
            spotifyJson = new JsonObject();
            if(triedRefresh) {
                spotifyJson.addProperty("error", e.getLocalizedMessage());
                return spotifyJson;
            } else {
                userFacade.refreshTokens(username);
                return getTrackInfo(username,trackid,true);
            }
        }

        String trackname = spotifyJson.get("item").getAsJsonObject().get("name").getAsString();
        String tracklength = spotifyJson.get("item").getAsJsonObject().get("duration_ms").getAsString();
        String trackpos = spotifyJson.get("progress_ms").getAsString();
        String albumname = spotifyJson.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
        String lyrics = "NO_UPDATE";
        


        String artistname = spotifyJson.get("item").getAsJsonObject().get("artists").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();

        responseJSON.addProperty("trackname", trackname);
        responseJSON.addProperty("tracklength", tracklength);
        responseJSON.addProperty("trackpos", trackpos);
        responseJSON.addProperty("artistname", artistname.toString());
        responseJSON.addProperty("albumname", albumname);

        if (!trackid.equals(spotifyJson.get("item").getAsJsonObject().get("id").getAsString())) {
            lyrics = getLyrics(responseJSON.get("artistname").getAsString(), responseJSON.get("trackname").getAsString());
            trackid = spotifyJson.get("item").getAsJsonObject().get("id").getAsString();
        }
        responseJSON.addProperty("trackid", trackid);
        responseJSON.addProperty("lyrics", lyrics);

        return responseJSON;
    }

    private String getLyrics(String artistName, String trackName) {
        HttpHelper httpHelper = new HttpHelper();
        Map<String, String> lyricHeaders = new HashMap<>();

        JsonObject lyricJson;
        lyricHeaders.put("Accept", "application/json");
        lyricHeaders.put("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:83.0) Gecko/20100101 Firefox/83.0");
        try {

            String lyricURL = "https://orion.apiseeds.com/api/music/lyric/" + artistName + "/" + trackName + "?apikey=dQR4afKVHwFNzc55VF63L8JtvRXffRl0fAleRmtiErnrWaWMu3gl46LS0lg9opr6";
            lyricJson = JsonParser.parseString(httpHelper.sendRequest(lyricURL, "GET", lyricHeaders, "")).getAsJsonObject();
            return lyricJson.get("result").getAsJsonObject().get("track").getAsJsonObject().get("text").getAsString();
        } catch (IOException e) {
            return "NO_DATA";
        }
    }
}
