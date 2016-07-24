package ritopls;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Handles populating the GUI based on the parsed status data for the current region.
 * 
 * @author Chris Meyers
 */
public class StatusHandler {
    private final GUI gui;
    private Thread pollThread, counterThread;
    private final HashMap<String, ArrayList<HashMap<String, String>>> allIncidents;
    
    /**
     * StatusHandler constructor.
     * 
     * @param g The current GUI instance.
     * @throws IOException 
     */
    public StatusHandler(GUI g) throws IOException {
        gui = g;
        allIncidents = new HashMap();
    }
    
    /**
     * Adjust values of server status labels when checking is enabled.
     */
    public void setTextWhenOn() {
        gui.checkButtonTextOn();
        
        // Creates a second thread to periodically check for a change in server
        // status.
        Runnable poll = new Runnable() {
            @Override
            public void run() {
                synchronized(gui.getParser()) {
                    HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo = new HashMap();

                    while(gui.getJToggleButton(1).isSelected()) {
                        try {
                            gui.getParser().pollTest(gui.getPollingRate(), gui.getCurrentRegion());
                            
                            allIncidents.clear();
                            
                            // Set current status for each service.
                            try {
                                statusInfo = gui.getParser().getStatus(gui.getCurrentRegion());
                            }
                            catch(UnknownHostException e) {
                                gui.setTextWhenOff();
                                break;
                            }
                            
                            setStatusStrings(statusInfo);
                            
                            /*
                             * Case 1:
                             *   - Set default text if incidents go away on refresh.
                             * 
                             * Case 2:
                             *   - Set default text if incidents exist and jToggleButton1
                             *     was toggled.
                             * 
                             * Case 3:
                             *   - Set default text if incidents go away on refresh
                             *     while the incidents for a selected service were being
                             *     displayed.
                             * 
                             */
                            if ((allIncidents.isEmpty()) ||                                            // Case 1
                                (!allIncidents.isEmpty() && gui.getLastClicked() == null) ||           // Case 2
                                (gui.getLastClicked() != null && !gui.getLastClicked().isEnabled())) { // Case 3
                                
                                gui.getJTextArea(1).setText(gui.setNewTextAreaMessage());
                                gui.setLastClicked(null);
                            }
                            
                            if(gui.hasRegionChanged()) {
                                gui.getJTextArea(1).setText(gui.setNewTextAreaMessage());
                                gui.setRegionChanged(false);
                            }
                            
                            gui.setFormIcon();
                            gui.setPingValue();
                            if(gui.getNotifTray() != null) {
                                gui.getNotifTray().setVariableMenuItems(-1);
                            }
                        } 
                        catch (IOException | InterruptedException ex) {}

                        System.out.println();
                                                    
                        try {
                            Thread.sleep(gui.getPollingRate() * 1000);
                            allIncidents.clear();
                            gui.turnAllIncidentButtonsOff();
                            
                            if(gui.getParser().networkCheck(gui.getCurrentRegion())) {
                                // Throws network errors (IOException from not 
                                // being able to openStream()).
                            }
                        } catch (InterruptedException e) {
                            System.out.println("**************THREAD \"" + Thread.currentThread().getName() + "\" HAS BEEN INTERRUPTED**************");
                            try {
                                gui.setTextWhenOff();
                            } catch (IOException ex) {}
                            
                            if(gui.getJToggleButton(1).isSelected()) {
                                gui.checkButtonTextOn();
                            }
                            gui.setFormIcon();
                        } catch (IOException ex) {
                            // Catches network errors from not being able to openStream().
                            System.out.println(ex);
                            gui.networkErrorFound();
                        }
                       
                    }
                }
            }        
        };

        pollThread = new Thread(poll, "Poll Thread");
        pollThread.start();
    }

    /**
    * Set status strings on GUI and handles incidents.
    * 
    * @param statusInfo All parsed information.
    * @throws InterruptedException
    */
    private void setStatusStrings(HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo) throws InterruptedException {
        String serviceString = "", status = "", id = "", severity = "", updatedTime = "", contentString = "", area = "";
        ArrayList<HashMap<String, String>> currentServiceIncList;
        HashMap<String, String> currentInc = new HashMap();
        ArrayList<String> severities = new ArrayList();
        boolean newIncidentFound;

        // Set polling rate info label
        setPollingInfoLabel();

        // Set status labels, color these labels and handle incidents.
        for(int service = 0; service < gui.getStatusLabels().length; service++) {
            serviceString = StaticData.getCurrentServiceName(service);
            status = statusInfo.get(serviceString).keySet().toString();

            gui.getStatusLabels()[service].setText(formatOutput(status));
            gui.colorize(gui.getStatusLabels()[service]);

            // Handle incidents.
            if(!statusInfo.get(serviceString).get(formatOutput(status)).isEmpty()) {
                for (int i = 0; i < statusInfo.get(serviceString).get(formatOutput(status)).size(); i++) {
                    newIncidentFound = true;
                    id = statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("id");
                    severity = statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("severity");
                    updatedTime = formatTime(statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("updated_at"));
                    contentString = statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("content");
                    area = "[" + gui.getCurrentRegion().toUpperCase() + " " + serviceString + "]";

                    /*
                     * If the incident list doesn't have an entry for the
                     * current service, make a new ArrayList and add the
                     * incident to that.  Otherwise, use the ArrayList
                     * that already exists and append the new incident to
                     * it.  Add the ArrayList to the incident HashMap.
                     */
                    if(allIncidents.get(serviceString) == null) {
                        currentServiceIncList = new ArrayList();
                    }
                    else {
                        currentServiceIncList = allIncidents.get(serviceString);

                        for(HashMap<String, String> c : allIncidents.get(serviceString)) {
                            // Filter new incidents by "id" field.
                            if(c.get("id").equals(id)) {
                                // The "id" already exists in the HashMap.
                                newIncidentFound = false;
                            }
                        }
                    }

                    if(newIncidentFound) {
                        currentInc.put("id", id);
                        currentInc.put("area", area);
                        currentInc.put("severity", severity);
                        currentInc.put("updatedTime", updatedTime);
                        currentInc.put("contentString", contentString);

                        currentServiceIncList.add((HashMap)currentInc.clone());
                        currentInc.clear();
                        allIncidents.put(serviceString, currentServiceIncList);
                    }

                    severities.add(severity);
                    populateIncidentBox(service, serviceString);

                    System.out.println(area + " :: " + severity + " :: "+ updatedTime + " :: " + contentString);
                }
                populateIncidentButton(service, severities);
                severities.clear();
            }
        }
    }  

    /**
     * Updates incident buttons based on severity.
     * 
     * @param service The current service that has an incident.
     * @param severities The severities of the current service's incidents.
     */
    private void populateIncidentButton(int service, ArrayList<String> severities) {
        final JButton button = gui.getIncidentButtons()[service];
        button.setEnabled(true);

        String severity = StaticData.determineMostSevere(severities);

        switch(severity) {
            case StaticData.INFO_STRING:
                button.setForeground(Color.black);
                button.setText(StaticData.INFO_SYMBOL);
                break;

            case StaticData.WARN_STRING:
                button.setForeground(Color.black);
                button.setText(StaticData.WARN_SYMBOL);
                break;

            case StaticData.ALERT_STRING:
                button.setForeground(Color.black);
                button.setText(StaticData.ALERT_SYMBOL);
                break;

            case StaticData.ERROR_STRING:
                button.setForeground(Color.black);
                button.setText(StaticData.ERROR_SYMBOL);
                break;

            default:
                button.setForeground(Color.black);
                button.setText(StaticData.WTF_SYMBOL);
                break;
        }
    }

    /**
     * Updates the incident output box (jTextArea1).
     * 
     * @param service The current service that has an incident.
     * @param serviceString A string of the incident.
     */
    private void populateIncidentBox(int service, String serviceString) {
        final JButton button = gui.getIncidentButtons()[service];
        button.setEnabled(true);

        final String currentService = serviceString;

        if(gui.getLastClicked() == button) {
            handleTextArea(currentService);
        }

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTextArea(currentService);
                gui.setLastClicked(button);
            }
        });
    }

    /**
     * Prints data to jTextArea1.
     * 
     * @param currentService The current service to output.
     */
    private void handleTextArea(String currentService) {
        gui.getJTextArea(1).setForeground(Color.black);
        gui.getJTextArea(1).setText("");
        for(int i = 0; i < allIncidents.get(currentService).size(); i++) {
            gui.getJTextArea(1).append(allIncidents.get(currentService).get(i).get("area") + " :: ");
            gui.getJTextArea(1).append(allIncidents.get(currentService).get(i).get("severity") + " :: ");
            gui.getJTextArea(1).append(allIncidents.get(currentService).get(i).get("updatedTime") + " :: ");
            gui.getJTextArea(1).append(allIncidents.get(currentService).get(i).get("contentString"));

            if(i != allIncidents.get(currentService).size()-1) {
                gui.getJTextArea(1).append("\n\n");
            }

            // "Scroll" to top of jTextBox1
            gui.getJTextArea(1).setCaretPosition(0);
        }
    }

    /**
     * Sets the value of the polling info label (jLabel9) based on the 
     * current polling rate.
     * 
     * @throws InterruptedException
     */
    private void setPollingInfoLabel() throws InterruptedException {
        Runnable countdown = new Runnable() {
            @Override
            public void run() {
                for(int i = gui.getPollingRate(); i > -1; i--) {
                    if(gui.getJToggleButton(1).isSelected()) {
                        if(i == 1) {
                            gui.getJLabel(9).setText("Refreshing " + gui.getCurrentRegion().toUpperCase() + " in " + i + " second...");
                        }
                        else {
                            gui.getJLabel(9).setText("Refreshing " + gui.getCurrentRegion().toUpperCase() + " in " + i + " seconds...");
                        }
                        gui.getJLabel(9).setHorizontalAlignment(SwingConstants.CENTER);
                        
                        if(gui.getNotifTray() != null) {
                            gui.getNotifTray().setVariableMenuItems(i);
                        }
 
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            System.out.println("**************THREAD \"" + Thread.currentThread().getName() + "\" HAS BEEN INTERRUPTED**************");
                            break;
                        }
                    }
                }
            }
        };
 
        counterThread = new Thread(countdown, "Counter Thread");
        counterThread.start();
    }

    /**
     * Removes brackets from the status obtained in the HashMap keySet.
     * 
     * @param raw The raw status string that was parsed.
     * @return A formatted status string.
     */
    private String formatOutput(String raw) {
        // Remove brackets
        String formatted = raw.replaceAll("[\\[\\]]", "");

        return formatted;
    }

    /**
     * Converts an incident's updated time to a more readable form.
     * 
     * @param raw The raw updated time as received from the API.
     * @return A human readable timestamp.
     */
    private String formatTime(String raw) {
        String date = raw.substring(0, raw.indexOf("T"));
        String time = raw.substring(raw.indexOf("T") + 1, raw.indexOf(".")) + " GMT";

        return date + " @ " + time; 
    }
            
    /**
     * Interrupt all non-main threads.
     */
    protected void interruptThreads() {
        if(pollThread != null) {
            pollThread.interrupt();
        }
        
        if(counterThread != null) {
            counterThread.interrupt();
        }
    }
    
    /**
     * Gets an incident map for the current region.
     * 
     * @return HashMap of incidents for current region.
     */
    protected HashMap<String, ArrayList<HashMap<String, String>>> getAllIncidents() {
        return allIncidents;
    }
    
}
