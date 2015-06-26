package RITOPLS;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This class parses the current League of Legends service status data for a 
 * region specified by the user.
 * 
 * @author Chris Meyers
 */
public class Parser {
    private String currentUrlData;
    private String baseUrl;
    private boolean networkOK;
    
    /**
     * Constructor for the Parser class.
     * 
     * @param region The region selected by the user.
     * @throws IOException 
     */
    public Parser(String region) throws IOException {
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
            reader = new BufferedReader(new InputStreamReader(statusUrlData.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[4096];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read); 
            }

            return buffer.toString();
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
            
            statusValues.put(status, (ArrayList)services.clone());
            services.clear(); // Make sure old incidents aren't copied if current service has no incidents.
            statusInfo.put(service, (HashMap)statusValues.clone());
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
        try {
            //getUrlData(buildUrl(region));
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
}
