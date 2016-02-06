package RITOPLS;

import java.util.ArrayList;

/**
 * This class stores the static values needed to populate fields in the GUI.
 * 
 * @author Chris Meyers
 */
public class StaticData {
    public static final String PROGRAM_TITLE          = "League of Legends Status Checker";
    public static final String DEBUG_TAG              = "[DEBUG]";
    public static final String MENU_FILE              = "File";
    public static final String MENU_HELP              = "Help";
    public static final String MENU_SET_REGION        = "Set Region";
    public static final String MENU_POLLING           = "Set Polling Rate";
    public static final String MENU_POLLING_ON        = "Start";
    public static final String MENU_POLLING_OFF       = "Stop";
    public static final String MENU_MINIMIZE          = "Minimize";
    public static final String MENU_MAXIMIZE          = "Maximize";
    public static final String MENU_EXIT              = "Exit";
    public static final String MENU_DEBUG             = "Debug";
    public static final String MENU_DEBUG_MODE        = "Debug Mode";
    public static final String MENU_DEBUG_FILE        = "Set Debug File";
    public static final String MENU_ABOUT             = "About";
    public static final String BUTTON_POLLING_ON      = "Checking...";
    public static final String BUTTON_POLLING_OFF     = "Click to check";
    public static final String POLLING_OFF_MSG        = "N/A";
    public static final String POLLING_WINDOW_MSG     = "How often should the server be checked\n(in seconds) ?";
    public static final String INCIDENTS_AVAILABLE    = "Incidents available for review.";
    public static final String NO_INCIDENTS_AVAILABLE = "No incidents to report!";
    public static final String NOT_POLLING_MSG        = "Not Currently Polling Server Status.";
    public static final String DEBUGGING_OFF_MSG      = "\u0000";
    public static final String DEBUGGING_ON_MSG       = "***Currently in Debug Mode***";
    public static final String SERVICE_ONLINE         = "Online";
    public static final String SERVICE_OFFLINE        = "Offline"; 
    public static final String SERVICE_ALERT          = "Alert"; 
    public static final String SERVICE_DEPLOYING      = "Deploying"; 
    public static final int DEFAULT_POLLING_RATE      = 10;
    
    public static final String INFO_STRING            = "Info";
    public static final String WARN_STRING            = "Warn";
    public static final String ALERT_STRING           = "Alert";
    public static final String ERROR_STRING           = "Error";
    
    public static final String INFO_SYMBOL            = "!";
    public static final String WARN_SYMBOL            = "!";
    public static final String ALERT_SYMBOL           = "! !";
    public static final String ERROR_SYMBOL           = "! ! !";
    public static final String WTF_SYMBOL             = "?";
    
    public static final String ABOUT_TITLE            = "About";
    public static final String ABOUT_ABOUT_MSG        = "Developed by: Chris Meyers || http://chrismeyers.info\n\n"
                                                        + "rito-pls is a java application that reports the current League\n"
                                                        + "of Legends service statuses for a specified region.  The\n"
                                                        + "application queries the League of Legends API periodically and\n"
                                                        + "presents the current status of several services (Client, Game,\n"
                                                        + "Store and Website).\n\n";
    public static final String ABOUT_LEGAL_MSG        = "riot-pls isn’t endorsed by Riot Games and doesn’t reflect the\n"
                                                        + "views or opinions of Riot Games or anyone officially involved\n"
                                                        + "in producing or managing League of Legends. League of Legends\n"
                                                        + "and Riot Games are trademarks or registered trademarks of Riot\n"
                                                        + "Games, Inc. League of Legends © Riot Games, Inc.";
    
    public static final String NETWORK_ERROR_MSG      = "A connection to the server was unable to be made.\n\n"
                                                        + "Either Riot's API servers are unresponsive or your network is "
                                                        + "experiencing issues.  Please check your connection and try "
                                                        + "again by toggling the \"Click to check\" button.";
    
    private String[] services                         = {"Client", "Game", "Store", "Website"};
    private String[] regions                          = {"NA", "EUW", "EUNE", "LAN", "LAS", "BR", "TR", "RU", "OCE", "JP"};
    
    private static final int INFO_PRECEDENCE          = 1;
    private static final int WARN_PRECEDENCE          = 1;
    private static final int ALERT_PRECEDENCE         = 2;
    private static final int ERROR_PRECEDENCE         = 3;
    
    private static final String[] POLLING_RATES       = {"1", "5", "10", "30", "45", "60"};

    public StaticData() {
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
     * Gets the correct array of services.
     * 
     * @param region The current region.
     * @return An array of service names. 
     */
    protected String[] getCurrentServiceNames(String region) {
        return services;
    }
    
    /**
     * Gets a service string from the correct array of services.
     * 
     * @param serv The service index
     * @return The correct service string.
     */
    protected String getCurrentServiceName(int serv) {
        return services[serv];
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
    
    /**
     * Gets an Array of the possible polling rates.
     * 
     * @return An Array of polling rates.
     */
    public String[] getPollingRatesArr() {
        return POLLING_RATES;
    }
    
    /**
     * Gets an ArrayList of the possible polling rates.
     * This method is used when ArrayList methods, such as indexOf(), are
     * needed.
     * 
     * @return An ArrayList of polling rates.
     */
    public ArrayList<String> getPollingRatesArrList() {
        ArrayList<String> temp = new ArrayList();
        for(String rate : POLLING_RATES) {
            temp.add(rate);
        }
        return temp;
    }
}
