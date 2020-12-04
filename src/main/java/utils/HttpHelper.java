
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
        con.setRequestMethod(method);
        con.setDoOutput(true);
        if(headers.containsKey("Content-Length")){
            if(headers.get("Content-Length").equals("0")){
                con.setFixedLengthStreamingMode(0);
                headers.remove("Content-Length");
            }
        }
        headers.forEach((key,value)-> con.setRequestProperty(key, value));


        if(!body.equals("")){
            con.setDoOutput(true);
            OutputStream output = con.getOutputStream();
            output.write(body.getBytes("UTF-8"));
            output.close();
        }

        Scanner scan = new Scanner(con.getInputStream());
        String jsonStr = "";
        while (scan.hasNext()) {
            jsonStr += scan.nextLine();
        }
        scan.close();
        System.out.println("content length:" +con.getHeaderField("Content-Length"));
        return jsonStr;
    } 
    
    
    
}
