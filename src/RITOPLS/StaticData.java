package RITOPLS;

/**
 * This class stores the static values needed to populate fields in the GUI.
 * 
 * @author Chris Meyers
 */
public class StaticData {
    private String[] services = {"Game", "Store", "Boards", "Website"};
    private String[] regions = {"NA", "EUW", "EUNE", "LAN", "LAS", "BR", "TR", "RU", "OCE"};

    public StaticData() {
    }
    
    /**
     * Gets the array of available services.
     * 
     * @return An array of available services.
     */
    public String[] getServices() {
        return services;
    }
    
    /**
     * Gets the the number of services.
     * 
     * @return The size of the services array.
     */
    public int getNumberServices() {
        return services.length;
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
     * Returns a specific service string.
     * 
     * @param index The index of the desired service string.
     * @return The desired service string.
     */
    public String getService(int index) {
        return services[index];
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
