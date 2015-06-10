package RITOPLS;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class parses the current League of Legends service status data for a 
 * region specified by the user.
 * 
 * @author Chris Meyers
 */
public class Parser {
    private String currentUrlData;
    private String baseUrl;
    
    /**
     * Constructor for the Parser class.
     * 
     * @param region The region selected by the user.
     * @throws IOException 
     */
    public Parser(String region) throws IOException {
        baseUrl = "http://status.leagueoflegends.com/shards/";
        String currentUrlString = buildUrl(region);
        currentUrlData = getUrlData(currentUrlString);
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
            StringBuffer buffer = new StringBuffer();
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
     * @return 
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
     * @param service The service to parse.
     * @return The status of the specified service in the specified region.
     * @throws IOException 
     */
    public HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>> 
                                        getStatus(String region) throws IOException {//ArrayList<String> getStatus(String region) throws IOException {
        JsonElement jelem = new JsonParser().parse(getUrlData(buildUrl(region)));
        JsonObject jobj = jelem.getAsJsonObject();
        JsonArray servicesArr = jobj.getAsJsonArray("services");
        JsonArray incidentsArr;
        JsonArray updatesArr;
        
        String service, status;
        HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>> statusInfo = new HashMap();
        
        HashMap<String, ArrayList<ArrayList<ArrayList<String>>>> statusValues = new HashMap();
        ArrayList<ArrayList<ArrayList<String>>> incidents = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> updates = new ArrayList<ArrayList<String>>();
        ArrayList<String> updateInfo = new ArrayList<String>();

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
                    for(int k = 0; k < updatesArr.size(); k++) {
                        jobj = updatesArr.get(k).getAsJsonObject();
                        
                        // Add updated_at
                        updateInfo.add(formatOutput(jobj.get("severity").toString()));
                        // Add updated_at
                        updateInfo.add(formatOutput(jobj.get("updated_at").toString()));
                        // Add content
                        updateInfo.add(formatOutput(jobj.get("content").toString()));
                        
                        updates.add((ArrayList)updateInfo.clone());
                        updateInfo.clear();
                    }
                    
                    //updateInfo.add(formatOutput(jobj.get("content").toString()));
                    //updates.add(formatOutput(jobj.get("updates").toString()));
                    incidents.add((ArrayList)updates.clone());
                    updates.clear();
                }
            }
            statusValues.put(status, (ArrayList)incidents.clone());
            statusInfo.put(service, (HashMap)statusValues.clone());
            incidents.clear();
            statusValues.clear();
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
}
