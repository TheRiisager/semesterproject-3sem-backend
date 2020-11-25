
package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.json.Json;

/**
 *
 * @author Bendico
 */
public class JWTdecoder {

    private String header;
    private String body;
    private String signature;
    private String userName;



    public JWTdecoder(String token) {
        
      java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
      String[] parts = token.split("\\."); 
     
      this.header = new String (decoder.decode(parts[0]));  
      this.body = new String (decoder.decode(parts[1]));
      this.signature = new String (decoder.decode(parts[2]));
   
        JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
       this.userName = jsonBody.get("username").getAsString();
        
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public String getUserName() {
        return userName;
    }
    
    
    
    
    
    
    
    
    
}
