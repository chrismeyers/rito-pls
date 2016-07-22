package RITOPLS;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * This class parses the current League of Legends service status data for a 
 * region specified by the user.
 * 
 * @author Chris Meyers
 */
public class StatusParser {
    private String currentUrlData;
    private final String baseUrl;
    private boolean networkOK;
    String ping;
    
    private static boolean DEBUG = false;
    private static String DEBUG_FILE = "F:\\Development\\League of Legends\\rito-pls\\JSON_examples\\offline.json";
    
    /**
     * Constructor for the Parser class.
     * 
     * @param region The region selected by the user.
     * @throws IOException 
     */
    public StatusParser(String region) throws IOException {
        baseUrl = "http://status.leagueoflegends.com/shards/";
        
        String currentUrlString = buildUrl(region);
        try {
            currentUrlData = getUrlData(currentUrlString);
            networkOK = true;
        }
        catch(UnknownHostException e) {
            currentUrlData = "";
            networkOK = false;
        }
        
        ping = "";
    }
    
    /**
     * Gets server status data in JSON format from the League of Legends API.
     * 
     * @param urlString The URL to get the server status data from.
     * @return The JSON obtained from the League of Legends API.
     * @throws IOException 
     */
    public final String getUrlData(String urlString) throws IOException {
        BufferedReader reader = null;
        try {
            URL statusUrlData = new URL(urlString);
            if(DEBUG) {
                InputStream fileStream = new FileInputStream(DEBUG_FILE);
                reader = new BufferedReader(new InputStreamReader(fileStream));
            }
            else {
                reader = new BufferedReader(new InputStreamReader(statusUrlData.openStream(), "UTF-8"));
            }
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[4096];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read); 
            }

            return buffer.toString().replace("\\r\\n", "");
        } 
        finally {
            if (reader != null) {
                reader.close();
            }    
        }        
    }
    
    /**
     * Builds a URL to reflect the region the user selected.
     * 
     * @param region The region selected by the user.
     * @return The API status url containing the current region.
     */
    private String buildUrl(String region) {
        return baseUrl + region;
    }

    /**
     * Used to test the functionality of the created thread that periodically polls
     * the League of Legends API.  
     * 
     * @param count The number of time the server was queried.
     * @param region The region selected by the user.
     */
    public void pollTest(int count, String region) {
        Date d = new Date();
        System.out.println(d.toString() + " // " + count + " second interval // " + region);
    }
    
    /**
     * Parses the JSON that was obtained from the League of Legends API.  Google's
     * Gson is used as the parsing utility.
     * 
     * @param region The region selected by the user.
     * @return The status of the specified service in the specified region.
     * @throws IOException 
     */
    public HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> 
                                        getStatus(String region) throws IOException {
        
        String currentData;

        try {
            currentData = getUrlData(buildUrl(region));
            networkOK = true;
        }
        catch(UnknownHostException e) {
            currentData = "";
            networkOK = false;
            throw e;
        }
        
        JsonElement jelem = new JsonParser().parse(currentData);
        JsonObject jobj = jelem.getAsJsonObject();
        JsonArray servicesArr = jobj.getAsJsonArray("services");
        JsonArray incidentsArr;
        JsonArray updatesArr;
        
        String service, status;
        HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo = new HashMap();
        HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>> statusValues = new HashMap();
        ArrayList<HashMap<String, HashMap<String, String>>> services = new ArrayList<HashMap<String, HashMap<String, String>>>();
        HashMap<String, HashMap<String, String>> incidents = new HashMap();
        HashMap<String, String> content = new HashMap();

        for(int i = 0; i < servicesArr.size(); i++) {
            jobj = servicesArr.get(i).getAsJsonObject();
            
            service = formatOutput(jobj.get("name").toString());
            status =  formatOutput(jobj.get("status").toString());       

            incidentsArr = jobj.getAsJsonArray("incidents");
            
            // If there are any incidents, store them.
            if(incidentsArr.size() > 0) {
                // Get each incident.
                for(int j = 0; j < incidentsArr.size(); j++) {
                    jobj = incidentsArr.get(j).getAsJsonObject();
                    updatesArr = jobj.getAsJsonArray("updates");
                    
                    // Get information for each update.
                    if(updatesArr.size() > 0) {
                        for(int k = 0; k < updatesArr.size(); k++) {
                            jobj = updatesArr.get(k).getAsJsonObject();

                            // Add id
                            content.put("id", formatOutput(jobj.get("id").toString()));
                            // Add severity
                            content.put("severity", formatOutput(jobj.get("severity").toString()));
                            // Add updated_at
                            content.put("updated_at", formatOutput(jobj.get("updated_at").toString()));
                            // Add content
                            content.put("content", formatOutput(jobj.get("content").toString()));
                            
                            incidents.put(service, (HashMap)content.clone());
                            services.add((HashMap)incidents.clone());
                        }
                    }
                }
            }
            
            Collections.reverse(services); // Flip incidents ArrayList to have the newest first.
            statusValues.put(status, (ArrayList)services.clone());
            services.clear(); // Make sure old incidents aren't copied if current service has no incidents.
            statusInfo.put(service, (HashMap)statusValues.clone());
            statusValues.clear(); // Make sure old statuses aren't copied if current service has no incidents.
        }
        
        return statusInfo;
    }
    
    /**
     * Formats the status obtained from the League of Legends API to remove 
     * surrounding quotations and begin with a capital letter.
     * 
     * @param raw The raw status string that was parsed.
     * @return A formatted status string.
     */
    private String formatOutput(String raw) {
        // Remove quotes
        String formatted = raw.replace("\"", "");
        //Capitalize first letter
        formatted = formatted.substring(0,1).toUpperCase() + formatted.substring(1);
        
        return formatted;
    }
    
    /**
     * Performs a network check by attempting to connect to the API.
     * 
     * @param region The current region.
     * @return  True if openStream() was successful, false otherwise.
     * @throws IOException 
     */
    public boolean networkCheck(String region) throws IOException {
        // Debug mode can be run offline.
        if(DEBUG) {
            return true;
        }
        
        try {
            URL statusUrlData = new URL(buildUrl(region));
            statusUrlData.openStream();
            networkOK = true;
            return true;
        }
        catch(UnknownHostException e) {
            networkOK = false;
            return false;
        }
    }
    
    /**
     * Gets the status of your connection to the status servers.
     * 
     * @return True if a connection was made, false otherwise.
     */
    public boolean isNetworkUp() {
        return networkOK;
    }
    
    /**
     * Toggles the state of debug mode.
     */
    public void toggleDebugMode() {
        DEBUG = !DEBUG;
    }
    
    /**
     * Gets the state of debug mode.
     * 
     * @return True if debug mode is enabled, false otherwise.
     */
    public boolean getDebugStatus() {
        return DEBUG;
    }
    
    /**
     * Sets the JSON file to be used in debug mode.
     * 
     * @param fileName An absolute path to a specified debug JSON file.
     */
    public void setDebugFile(String fileName) {
        DEBUG_FILE = fileName;
    }
    
    /**
     * Pings the IP of the current region.
     * 
     * @param ip the IP to check
     * @return the ping value.
     */
    public String determinePing(String ip) {
        if(ip.isEmpty()) {
            System.out.println("PING NOT AVAILABLE");
            return "Not Available";
        }
        
        String command = "ping " + ip;

        try{
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                if(line.length() > 0 && line.contains("time")) {
                    System.out.println(line);
                    input.close();
                    String timeString = line.substring(line.indexOf("time"));
                    String time = timeString.substring(timeString.indexOf("=") + 1);
                    ping = time;
                    return time;
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }

        return "SERVERS ON FIRE";
    }
    
    /**
     * Gets the last known ping value.
     * 
     * @return the last known ping value. 
     */
    public String getPing() {
        return ping;
    }
}
