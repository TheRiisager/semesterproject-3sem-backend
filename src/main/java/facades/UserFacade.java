package facades;

import dtos.UserDTO;
import entities.User;
import java.io.IOException;
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
    public void refreshTokens(String username){}
    
     public void getSpotifyAuth(String userName, String code) throws IOException {
         EntityManager em = emf.createEntityManager();
         User user;
         
         Map<String, String> headers = new HashMap<>();
         headers.put("content-Type", "application/x-www-form-urlencoded");
         headers.put("Accept", "application/json");
         HttpHelper httpHelper = new HttpHelper();
         String requestBody = "grant_type=authorization_code&code=" + code + "&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2F&client_id=f382ba93a1794be4b700ddcbf6bfe068&client_secret=b2936ccce2534ec694a135eb4d42444c";
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
