package RITOPLS;

import java.util.ArrayList;

/**
 * This class stores the static values needed to populate fields in the GUI.
 * 
 * @author Chris Meyers
 */
public class StaticData {
    private String[] servicesB = {"Boards", "Game", "Store", "Website"};
    private String[] servicesF = {"Forums", "Game", "Store", "Website"};
    private String[] regions = {"NA", "EUW", "EUNE", "LAN", "LAS", "BR", "TR", "RU", "OCE"};
    
    public static final String INFO_STRING  = "Info";
    public static final String WARN_STRING  = "Warn";
    public static final String ALERT_STRING = "Alert";
    public static final String ERROR_STRING = "Error";
    
    private static final int INFO_PRECEDENCE  = 1;
    private static final int WARN_PRECEDENCE  = 1;
    private static final int ALERT_PRECEDENCE = 2;
    private static final int ERROR_PRECEDENCE = 3;

    public StaticData() {
    }
    
    /**
     * Gets the array of available services for regions that use "Boards".
     * 
     * @return An array of available services.
     */
    public String[] getServicesB() {
        return servicesB;
    }
    
    /**
     * Gets the array of available services for regions that use "Forums".
     * 
     * @return An array of available services.
     */
    public String[] getServicesF() {
        return servicesF;
    }
    
    /**
     * Gets the array of available regions.
     * 
     * @return An array of available regions.
     */
    public String[] getRegions() {
        return regions;
    }
    
    /**
     * Gets the the number of regions.
     * 
     * @return The size of the regions array.
     */
    public int getNumberRegions() {
        return regions.length;
    }
    
    /**
     * Returns a specific service string for regions that use "Forums".
     * 
     * @param index The index of the desired service string.
     * @return The desired service string.
     */
    public String getServiceF(int index) {
        return servicesF[index];
    }
    
    /**
     * Returns a specific service string  for regions that use "Boards".
     * 
     * @param index The index of the desired service string.
     * @return The desired service string.
     */
    public String getServiceB(int index) {
        return servicesB[index];
    }
    
    /**
     * Returns a specific region string.
     * 
     * @param index The index of the desired region string.
     * @return The desired region string.
     */
    public String getRegion(int index) {
        return regions[index];
    }

    /**
     * Determines the most severe incident in a given incident list.
     * 
     * @param severities List of severities for a specific service.
     * @return The highest severity in the list of severities.
     */
    public String determineMostSevere(ArrayList<String> severities) {
        int currentPrec = 0, mostSevere = 0;
        String currentSeverity = "", highestSeverity = "";

        for(int i = 0; i < severities.size(); i++){
            switch(severities.get(i)) {
                case INFO_STRING:
                    currentPrec = INFO_PRECEDENCE;
                    currentSeverity = INFO_STRING;
                    break;
                case WARN_STRING:
                    currentPrec = WARN_PRECEDENCE;
                    currentSeverity = WARN_STRING;
                    break;
                case ALERT_STRING:
                    currentPrec = ALERT_PRECEDENCE;
                    currentSeverity = ALERT_STRING;
                    break;
                case ERROR_STRING:
                    currentPrec = ERROR_PRECEDENCE;
                    currentSeverity = ERROR_STRING;
                    break;
                default:
                    break;
            }
            
            if(currentPrec > mostSevere) {
                mostSevere = currentPrec;
                highestSeverity = currentSeverity;
            }
            
        }
        return highestSeverity;
    }
}
