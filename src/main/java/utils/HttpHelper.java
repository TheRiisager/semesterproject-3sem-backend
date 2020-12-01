
package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Bendico
 */
public class HttpHelper {

    public HttpHelper() {
    }
    
    
    public String sendRequest(String _url, String method, Map<String, String> headers, String body) throws MalformedURLException, IOException {
        URL url = new URL(_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod(method);

        headers.forEach((key,value)-> con.setRequestProperty(key, value));

        if(!body.equals("")){
            OutputStream output = con.getOutputStream();
            output.write(body.getBytes("UTF-8"));
            output.close();
        }
        //con.setRequestProperty("Accept", "application/json");
        // con.setRequestProperty("User-Agent", "server"); //remember if you are using SWAPI

        Scanner scan = new Scanner(con.getInputStream());
        String jsonStr = null;
        if (scan.hasNext()) {
            jsonStr = scan.nextLine();
        }
        scan.close();
        return jsonStr;
    } 
    
    
    
}
