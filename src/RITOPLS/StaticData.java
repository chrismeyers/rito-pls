package RITOPLS;

/**
 * This class stores the static values needed to populate fields in the GUI.
 * 
 * @author Chris Meyers
 */
public class StaticData {
    private String[] servicesB = {"Boards", "Game", "Store", "Website"};
    private String[] servicesF = {"Forums", "Game", "Store", "Website"};
    private String[] regions = {"NA", "EUW", "EUNE", "LAN", "LAS", "BR", "TR", "RU", "OCE"};

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
}
