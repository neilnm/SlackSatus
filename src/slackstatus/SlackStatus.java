package slackstatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

public class SlackStatus {
    static String token = "";
    public static void main(String[] args) throws Exception {
        
        //Token Input Dialog box
        String inputToken = (String)JOptionPane.showInputDialog(
                    null,
                    "Enter your Slack token",
                    "Token",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");

        token = inputToken;
        
        //Timer to rerun every x minutes/hours
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {
           @Override
           public void run() {
               try {
                   int id = getWeatherID();
                   String emoji = getEmoji(id);
                   updateSlack(emoji);
               } 
               catch (Exception ex) {
                   System.out.println(ex.getMessage());
               }
           }
        }, 0, 5, TimeUnit.MINUTES);
    }
    
    //get emoji by ID returned from weahter API
    static private String getEmoji(int id){
        String emoji = "";
        if(id >= 210 && id <= 221){return "lightning";}
        if(id >= 200 && id <= 202 || id >= 230 && id <= 232){return "thunder_cloud_and_rain";}
        if(id >= 520 && id <= 531){return "rain_cloud";}
        if(id >= 500 && id <= 504){return "partly_sunny_rain";}
        if(id == 804){return "cloud";}
        if(id == 803){return "barely_sunny";}
        if(id == 802){return "partly_sunny";}
        if(id == 801){return "mostly_sunny";}
        if(id == 800){return "sunnny";}
        if(id == 900){return "tornado";}
        if(id >= 600 && id <= 699){return "snow_cloud";}
        if(id >= 700 && id <= 799){return "fog";}
        if(id >= 956 && id <= 999 || id == 905){return "wind_blowing_face";}
        if(id >= 210 && id <= 221){return "lightning";}
        if(id == 901 || id == 902 || id == 962){return "cyclone";}
        return emoji;
    }
    
    
    //getting weather from weather api
    static private int getWeatherID() throws MalformedURLException, IOException, JSONException{
        int id;
        String url = "http://api.openweathermap.org/data/2.5/weather";
        String query = "q=Montreal,ca&appid=50ad33aeb6e06d5eb56df10390e05e47";
        String charset = "UTF-8";
        URL obj = new URL(url);

        //Opening cxn
        URLConnection conn = new URL(url + "?" + query).openConnection();
        conn.setRequestProperty("Accept-Charset", charset);
        InputStream stream = conn.getInputStream();

        //Parsing JSON weather from API to get condition ID
        try (Scanner scanner = new Scanner(stream)) {
            String result = scanner.useDelimiter("\\A").next();
            JSONObject jsonResult = new JSONObject(result);
            JSONArray a = jsonResult.getJSONArray("weather");
            JSONObject weather = ((JSONObject) a.get(0));

            id = weather.getInt("id");
            System.out.println(id);
            System.out.println("API returned weather ID :"+id);
            System.out.println("See: https://openweathermap.org/weather-conditions for weather codes");
            return id;
        }
    }
    
    // HTTP POST request to Slack
    static private void updateSlack(String emoji) throws Exception {
        final String USER_AGENT = "Mozilla/5.0";
        String url = "https://slack.com/api/users.profile.set?";
        URL urlObj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) urlObj.openConnection();
        
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        
        //SLACK TOKEN, Status Text and Status Emoji
        String urlParameters = "token="+token+"&profile=%7B%20%20%20%20%20%22"
                + "status_text%22%3A%20%22"+emoji+"%22%2C%20%20%20%20%20%22"
                + "status_emoji%22%3A%20%22%3A"+emoji+"%3A%22%20%7D&pretty=1";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        
        //Printing response back from Slack
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }
}