package facades;

import dtos.UserDTO;
import entities.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import security.errorhandling.AuthenticationException;
import utils.HttpHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author lam@cphbusiness.dk
 */
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    /**
     *
     * @param _emf
     * @return the instance of this facade.
     */
    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    public User getVeryfiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid user name or password");
            }
        } finally {
            em.close();
        }
        return user;
    }

    public UserDTO getUserDTO(String username) {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
        } finally {
            em.close();
        }
        return new UserDTO(user.getUserName(), user.getRolesAsStrings().toString());
    }
    //TODO implement this:
    public void refreshTokens(String username){
        Base64.Encoder encoder = Base64.getEncoder();
        EntityManager em = emf.createEntityManager();
        Map<String, String> headers = new HashMap<>();
        headers.put("content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        String authString = "f382ba93a1794be4b700ddcbf6bfe068:b2936ccce2534ec694a135eb4d42444c";
        headers.put("Authorization","Basic " + encoder.encodeToString(authString.getBytes(StandardCharsets.UTF_8)));
        HttpHelper httpHelper = new HttpHelper();
        User user;
        try {
            user = em.find(User.class, username);

            String requestBody = "grant_type=refresh_token&refresh_token=" + user.getRefreshToken();
            try {
                String response = httpHelper.sendRequest("https://accounts.spotify.com/api/token","GET",headers,requestBody);
                JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
                em.getTransaction().begin();
                user.setAccessToken(responseJson.get("access_token").getAsString());
                if(responseJson.has("refresh_token")){
                    user.setRefreshToken(responseJson.get("refresh_token").getAsString());
                }
                em.merge(user);
                em.getTransaction().commit();

            } catch (IOException e){
                System.out.println("Could not refresh token for user: " + user.getUserName());
            }

        } finally {
            em.close();
        }

    }
    
     public void getSpotifyAuth(String userName, String code,boolean fromAndroid) throws IOException {
         EntityManager em = emf.createEntityManager();
         User user;
         String redirect;
         if(fromAndroid){
             redirect = "http%3A%2F%2Flocalhost%3A8888%2Fcallback";
         } else {
             redirect = "http%3A%2F%2Flocalhost%3A3000%2F";
         }
         
         Map<String, String> headers = new HashMap<>();
         headers.put("content-Type", "application/x-www-form-urlencoded");
         headers.put("Accept", "application/json");
         HttpHelper httpHelper = new HttpHelper();
         String requestBody = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirect +"&client_id=f382ba93a1794be4b700ddcbf6bfe068&client_secret=b2936ccce2534ec694a135eb4d42444c";
         String result = httpHelper.sendRequest("https://accounts.spotify.com/api/token", "POST", headers, requestBody);

         JsonObject responseJson = JsonParser.parseString(result).getAsJsonObject();
         
         String accessToken = responseJson.get("access_token").getAsString();
         String refreshToken = responseJson.get("refresh_token").getAsString();
         
         try {
            user = em.find(User.class, userName);
            em.getTransaction().begin();
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            em.merge(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

}
    
    
    

}
