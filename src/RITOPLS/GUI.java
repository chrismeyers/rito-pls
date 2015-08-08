package RITOPLS;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This class provides a GUI interface that reflects the status of League of Legends 
 * services for the region specified by the user.
 * 
 * @author Chris Meyers
 */
public class GUI extends javax.swing.JFrame {
    private String region;
    private final Parser p;
    private final StaticData sdata;
    private int pollingRate;
    private final JLabel[] serviceLabels;
    private final JLabel[] statusLabels;
    private final JButton[] incidentButtons;
    private JButton lastClicked;
    private final HashMap<String, ArrayList<HashMap<String, String>>> allIncidents;
    private boolean regionChanged;
    Thread pollThread, counterThread;
    TrayIcon trayIcon;
    MenuItem info;
    MenuItem togglePolling;
    
    /**
     * Creates new form GUI
     * @throws java.io.IOException
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();
        allIncidents = new HashMap();
        regionChanged = true;
        serviceLabels = new JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4};
        statusLabels = new JLabel[]{jLabel5, jLabel6, jLabel7, jLabel8};
        incidentButtons = new JButton[]{jButton1, jButton2, jButton3, jButton4};
        
        setupMenus();
        setPollingRate(StaticData.DEFAULT_POLLING_RATE);
        populateRegionComboBox(sdata.getRegions());
        
        //Initialize region to first item in ComboBox (NA)
        region = jComboBox1.getSelectedItem().toString().toLowerCase();
        
        populateServicesLabels();
        
        p = new Parser(region);
        setTextWhenOff(); // default state
        
        // Listen for changes in region combo box state.
        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCurrentRegion(jComboBox1.getSelectedItem().toString());
                
                // Update labels in case naming convention in current region changed.
                populateServicesLabels();
                
                // Keeps jToggleButton1's text from incorrectly changing to 
                //"Checking..." when the region is changed and jToggleButton is disabled.
                if(jToggleButton1.isSelected()) { 
                    regionChanged = true;
                    interruptThreads();
                }
            }        
        });
        
        // Listen for changes in check button state.
        jToggleButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jToggleButton1.isSelected()) {
                    try {
                        if(p.networkCheck(getCurrentRegion())) {
                            // Throws network errors (IOException from not 
                            // being able to openStream()).
                            setTextWhenOn();
                            jTextArea1.setText(setNewTextAreaMessage());
                        }
                    } catch (IOException ex) {
                        // Catches network errors from not being able to openStream().
                        System.out.println(ex);
                        networkErrorFound();
                    }
                }
                else {
                    interruptThreads();
                    lastClicked = null;
                }
            }        
        });  
    }
    
    /**
     * Populates jComboBox1 with the available regions.
     * 
     * @param regions The available regions to check.
     */
    private void populateRegionComboBox(String[] regions) {
        jComboBox1.setModel(new DefaultComboBoxModel(regions));
    }
    
    /**
     * Interrupt all non-main threads.
     */
    private void interruptThreads() {
        if(pollThread != null) {
            pollThread.interrupt();
        }
        
        if(counterThread != null) {
            counterThread.interrupt();
        }
    }
    
    /**
     * Initializes menu items and adds ActionListeners to these items.
     */
    private void setupMenus() {
        this.setTitle(StaticData.PROGRAM_TITLE);
        this.setResizable(false);
        setFormIcon();
        
        jMenu1.setText(StaticData.MENU_FILE);
        jMenuItem1.setText(StaticData.MENU_POLLING);
        jMenuItem2.setText(StaticData.MENU_MINIMIZE);
        jMenuItem3.setText(StaticData.MENU_EXIT);
        jMenu2.setText(StaticData.MENU_HELP);
        jMenu3.setText(StaticData.MENU_DEBUG);
        jCheckBoxMenuItem1.setText(StaticData.MENU_DEBUG_MODE);
        jMenuItem4.setText(StaticData.MENU_ABOUT);
        jMenuItem5.setText(StaticData.MENU_DEBUG_FILE);
        
        jCheckBoxMenuItem1.setSelected(false);
        
        jLabel10.setText(StaticData.DEBUGGING_OFF_MSG);
        
        // Polling rate menu item listener
        jMenuItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayPollingRateWindow();
            }        
        });
        
        // Minimize to system tray.
        jMenuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                minimizeToTray();
            }        
        });
        
        // Quit menu item listener
        jMenuItem3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }        
        });
        
        // Toggle Debug Mode
        jCheckBoxMenuItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnAllIncidentButtonsOff();
                p.toggleDebugMode();
                if(p.getDebugStatus()) {
                    jLabel10.setText(StaticData.DEBUGGING_ON_MSG);
                    setTitle(false);
                }
                else {
                    jLabel10.setText(StaticData.DEBUGGING_OFF_MSG);
                    setTitle(true);
                }

                toggleCheckButton(); // Reset polling after debug toggle.
            }
        });
        
        // Set debug file.
        jMenuItem5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON", "JSON");
                fileChooser.addChoosableFileFilter(filter);
                int file = fileChooser.showOpenDialog(null);
                if(file == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    p.setDebugFile(selectedFile.getAbsolutePath());
                    toggleCheckButton(); // Reset polling for new debug file.
                }
            }
        });
        
        // About menu item listener
        jMenuItem4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAboutWindow();
            }
        });
    }
    
    /**
     * Allows access to setTitle() from within the debug menuitem actionListener.
     * 
     * @param debugMode The current state of debugging.
     */
    private void setTitle(boolean debugMode) {
        if(debugMode) {
            this.setTitle(StaticData.PROGRAM_TITLE);
        }
        else {
            this.setTitle(StaticData.DEBUG_TAG + " " + StaticData.PROGRAM_TITLE);
        }
    }
    
    /**
     * Raises a window showing info about the program.
     */
    private void displayAboutWindow() {
        JOptionPane.showMessageDialog(new JFrame(), 
            StaticData.ABOUT_ABOUT_MSG + StaticData.ABOUT_LEGAL_MSG,
            StaticData.ABOUT_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Populates and raises a window used to specify the polling rate for the
     * program.
     */
    private void displayPollingRateWindow() {
        // An ArrayList is used here instead of an Array for the indexOf() method
        // in order to set the starting value in the 7th param of the JOptionPane.
        ArrayList<String> intervals = sdata.getPollingRatesArrList();
        String pollRate = "";
        int rate = 0;

        String startingValue = getPollingRate() + "";

        pollRate = (String) JOptionPane.showInputDialog(new JFrame(), 
            StaticData.POLLING_WINDOW_MSG,
            StaticData.MENU_POLLING,
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            intervals.toArray(), 
            intervals.get(intervals.indexOf(startingValue)));

        if(pollRate == null) {   
            // User pressed cancel, get current rate.
            pollRate = startingValue;
        }

        rate = Integer.parseInt(pollRate);

        setPollingRate(rate);
    }
    
    /**
     * Populates jLabel1-4 with the available services.
     */
    private void populateServicesLabels() {
        String[] services = getCurrentServiceNames();
 
        for(int i = 0; i < serviceLabels.length; i++) {
            JLabel label = serviceLabels[i];
            label.setText(services[i]);
        }
    }
    
    /**
     * Return the region that is currently selected in jComboBox1;
     * 
     * @return The current region.
     */
    public String getCurrentRegion() {
        return region;
    }
    
    /**
     * Sets the region to reflect what is currently selected in jComboBox1.
     * 
     * @param newRegion The value currently selected in jComboBox1.
     */
    private void setCurrentRegion(String newRegion) {
        region = newRegion.toLowerCase();
    }
    
    /**
     * Set server status labels when not checking the server status.
     */
    private void setTextWhenOff() throws IOException {
        if(p.networkCheck(getCurrentRegion())) {
            jTextArea1.setText(setNewTextAreaMessage());
        }
        else {
            networkErrorFound();
        }

        checkButtonTextOff();
        resetStatusLabels();
        turnAllIncidentButtonsOff();

        // Setup incident textbox.
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);  
    }
    
    /**
     * Sets the default status label values.
     */
    private void resetStatusLabels(){
        for (JLabel label : statusLabels) {
            label.setText(StaticData.POLLING_OFF_MSG);
            decolorize(label);
        }
        
        // Set polling rate info label to blank when polling is off
        jLabel9.setText(StaticData.NOT_POLLING_MSG);
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /**
     * Sets all incident buttons to default "off" state
     */
    private void turnAllIncidentButtonsOff(){
        for (JButton button : incidentButtons) {
            button.setEnabled(false);
            button.setText(StaticData.POLLING_OFF_MSG);
            button.setBackground(null);
        }
    }
    
    /**
     * Returns a string to be used when setting jTextArea1 based on if there
     * are currently any incidents.
     * 
     * @return A string to be used to populate the default jTextArea1.
     */
    private String setNewTextAreaMessage() {
        jTextArea1.setForeground(Color.black);

        if(jToggleButton1.isSelected() && !allIncidents.isEmpty()) {
            return StaticData.INCIDENTS_AVAILABLE;
        }       
        
        return StaticData.NO_INCIDENTS_AVAILABLE;
    }
    
    /**
     * Sets the rate at which the program queries the API.
     * 
     * @param rate The rate of checking servers (in seconds)
     */
    private void setPollingRate(int rate) {
        pollingRate = rate;
    }
    
    /**
     * Gets the rate at which the program queries the API.
     * 
     * @return The rate at which the program checks the servers.
     */
    public int getPollingRate() {
        return pollingRate;
    }
    
    /**
     * Adjust values of server status labels when checking is enabled.
     */
    private void setTextWhenOn() {
        checkButtonTextOn();
        
        // Creates a second thread to periodically check for a change in server
        // status.
        Runnable poll = new Runnable() {
            @Override
            public void run() {
                synchronized(p) {
                    HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo = new HashMap();

                    while(jToggleButton1.isSelected()) {
                        try {
                            allIncidents.clear();
                            
                            // Set current status for each service.
                            try {
                                statusInfo = p.getStatus(getCurrentRegion());
                            }
                            catch(UnknownHostException e) {
                                setTextWhenOff();
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
                            if ((allIncidents.isEmpty())                                 // Case 1
                                    || (!allIncidents.isEmpty() && lastClicked == null)  // Case 2
                                    || (lastClicked != null && !lastClicked.isEnabled()) // Case 3
                                    ) {
                                jTextArea1.setText(setNewTextAreaMessage());
                                lastClicked = null;
                            }
                            
                            if(regionChanged) {
                                jTextArea1.setText(setNewTextAreaMessage());
                                regionChanged = false;
                            }
                            setFormIcon();
                        } 
                        catch (IOException | InterruptedException ex) {}

                        try {
                            Thread.sleep(getPollingRate() * 1000);
                            allIncidents.clear();
                            turnAllIncidentButtonsOff();
                            
                            if(p.networkCheck(getCurrentRegion())) {
                                // Throws network errors (IOException from not 
                                // being able to openStream()).
                            }
                                                    
                            p.pollTest(getPollingRate(), getCurrentRegion());
                        } catch (InterruptedException e) {
                            System.out.println("**************THREAD \"" + Thread.currentThread().getName() + "\" HAS BEEN INTERRUPTED**************");
                            try {
                                setTextWhenOff();
                            } catch (IOException ex) {}
                            
                            if(jToggleButton1.isSelected()) {
                                checkButtonTextOn();
                            }
                            setFormIcon();
                        } catch (IOException ex) {
                            // Catches network errors from not being able to openStream().
                            System.out.println(ex);
                            networkErrorFound();
                        }
                    }
                }
            }
            
            /**
             * Set status strings on GUI and handles incidents.
             * 
             * @param statusInfo All parsed information.
             */
            private void setStatusStrings(HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo) 
                                                    throws InterruptedException {
                
                String serviceString = "", status = "", severity = "", updatedTime = "", contentString = "", area = "";
                ArrayList<HashMap<String, String>> currentServiceIncList;
                HashMap<String, String> currentInc = new HashMap();
                ArrayList<String> severities = new ArrayList<String>();
                boolean incidentFound;
                           
                
                // Set polling rate info label
                setPollingInfoLabel();
                
                // Set status labels, color these labels and handle incidents.
                for(int service = 0; service < statusLabels.length; service++) {
                    serviceString = getCurrentServiceName(service);
                    status = statusInfo.get(serviceString).keySet().toString();
                
                    statusLabels[service].setText(formatOutput(status));
                    colorize(statusLabels[service]);
                    
                    // Handle incidents.
                    if(!statusInfo.get(serviceString).get(formatOutput(status)).isEmpty()) {
                        for (int i = 0; i < statusInfo.get(serviceString).get(formatOutput(status)).size(); i++) {
                            incidentFound = false;
                            severity = statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("severity");
                            updatedTime = formatTime(statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("updated_at"));
                            contentString = statusInfo.get(serviceString).get(formatOutput(status)).get(i).get(serviceString).get("content");
                            area = "[" +getCurrentRegion().toUpperCase() + " " + serviceString + "]";

                            /*
                             * If the incident list doesn't have an entry for the
                             * current service, make a new ArrayList and add the
                             * incident to that.  Otherwise, use the ArrayList
                             * that already exists and append the new incident to
                             * it.  Add the ArrayList to the incident HashMap.
                             */
                            if(allIncidents.get(serviceString) == null) {
                                currentServiceIncList = new ArrayList<HashMap<String, String>>();
                            }
                            else {
                                currentServiceIncList = allIncidents.get(serviceString);
                                
                                for (HashMap<String, String> c : allIncidents.get(serviceString)) {
                                    if(c.get("contentString").equals(contentString)) {
                                        incidentFound = true;
                                    }
                                }
                            }
                            
                            if(!incidentFound) {
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
             * @param severity The severity of the current incident.
             */
            private void populateIncidentButton(int service, ArrayList<String> severities) {
                
                final JButton button = incidentButtons[service];
                button.setEnabled(true);

                String severity = sdata.determineMostSevere(severities);
                
                switch(severity) {
                    case StaticData.INFO_STRING:
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText(StaticData.INFO_SYMBOL);
                        break;
                        
                    case StaticData.WARN_STRING:
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText(StaticData.WARN_SYMBOL);
                        break;
                        
                    case StaticData.ALERT_STRING:
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText(StaticData.ALERT_SYMBOL);
                        break;
                        
                    case StaticData.ERROR_STRING:
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText(StaticData.ERROR_SYMBOL);
                        break;
                        
                    default:
                        button.setForeground(Color.magenta);
                        button.setBackground(Color.black);
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
                final JButton button = incidentButtons[service];
                button.setEnabled(true);
                
                final String currentService = serviceString;
                
                if(lastClicked == button) {
                    handleTextArea(currentService);
                }
                
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleTextArea(currentService);
                        lastClicked = button;
                    }
                });
            }
            
            /**
             * Prints data to jTextArea1.
             * 
             * @param currentService The current service to output.
             */
            private void handleTextArea(String currentService) {
                jTextArea1.setForeground(Color.black);
                jTextArea1.setText("");
                for(int i = 0; i < allIncidents.get(currentService).size(); i++) {
                    jTextArea1.append(allIncidents.get(currentService).get(i).get("area") + " :: ");
                    jTextArea1.append(allIncidents.get(currentService).get(i).get("severity") + " :: ");
                    jTextArea1.append(allIncidents.get(currentService).get(i).get("updatedTime") + " :: ");
                    jTextArea1.append(allIncidents.get(currentService).get(i).get("contentString"));

                    if(i != allIncidents.get(currentService).size()-1) {
                        jTextArea1.append("\n\n");
                    }

                    // "Scroll" to top of jTextBox1
                    jTextArea1.setCaretPosition(0);
                }
            }

            /**
             * Sets the value of the polling info label (jLabel9) based on the 
             * current polling rate.
             */
            private void setPollingInfoLabel() throws InterruptedException {
                Runnable countdown = new Runnable() {
                    @Override
                    public void run() {
                        for(int i = getPollingRate(); i > -1; i--) {
                            if(jToggleButton1.isSelected()) {
                                if(i == 1) {
                                    jLabel9.setText("Refreshing " + getCurrentRegion().toUpperCase() + " in " + i + " second...");
                                }
                                else {
                                    jLabel9.setText("Refreshing " + getCurrentRegion().toUpperCase() + " in " + i + " seconds...");
                                }
                                jLabel9.setHorizontalAlignment(SwingConstants.CENTER);

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
             * @return A more readable timestamp.
             */
            private String formatTime(String raw) {
                String date = raw.substring(0, raw.indexOf("T"));
                String time = raw.substring(raw.indexOf("T") + 1, raw.indexOf(".")) + " GMT";
                
                return date + " @ " + time; 
            }
            
            /**
             * Change the color of service status labels based on current state
             * of server.
             */
            private void colorize(JLabel label) {
                switch (label.getText()) {
                    case StaticData.SERVICE_ONLINE:
                        label.setForeground(Color.green);
                        break;
                    case StaticData.SERVICE_OFFLINE:
                        label.setForeground(Color.red);
                        break;
                    case StaticData.SERVICE_ALERT:
                        label.setForeground(Color.yellow);
                        break;
                    case StaticData.SERVICE_DEPLOYING:
                        label.setForeground(Color.blue);
                        break;
                    default:
                        label.setForeground(Color.magenta);
                        break;
                }
            }
        };

        pollThread = new Thread(poll, "Poll Thread");
        pollThread.start();
    }
    
    /**
     * Sets the GUI form icon based on the status of services for the current
     * region.
     */
    private void setFormIcon() {
        Image img;
        
        if(!jToggleButton1.isSelected()) {
            // Grey Icon - IDLE
            img = resources.ResourceLoader.getImage("iconIDLE.png");
        }
        else if(checkAllOnline()) {
            if(checkForAnIncident()) {
                // Yellow Icon - INCDENTS EXIST
                img = resources.ResourceLoader.getImage("iconINCIDENT.png");
            }
            else {
                // Green Icon - ALL SERVICES ONLINE, NO INCIDENTS
                img = resources.ResourceLoader.getImage("iconONLINE.png");
            }
        }
        else {
            // Red Icon - AT LEAST ONE SERVICE OFFLINE
            img = resources.ResourceLoader.getImage("iconOFFLINE.png");
        }
        
        this.setIconImage(img);
        
        if(trayIcon != null) {
            trayIcon.setImage(img);
        }
    }
    
    /**
     * Gets the correct array of services.
     * 
     * @return An array of service names. 
     */
    private String[] getCurrentServiceNames() {
        String[] services;
        // NA and OCE use "Boards" service
        if(getCurrentRegion().equals("na") || getCurrentRegion().equals("oce")) {
            services = sdata.getServicesB();
        }
        // All others use "Forums" service
        else {
            services = sdata.getServicesF();
        }

        return services;
    }
    
    /**
     * Gets a service string from the correct array of services.
     * 
     * @param serv The service index
     * @return The correct service string.
     */
    private String getCurrentServiceName(int serv) {
        String service;
        // NA and OCE use "Boards" service
        if(getCurrentRegion().equals("na") || getCurrentRegion().equals("oce")) {
            service = sdata.getServiceB(serv);
        }
        // All others use "Forums" service
        else {
            service = sdata.getServiceF(serv);
        }

        return service;
    }
    
    /**
     * Checks to see if all services are online.
     * 
     * @return False if one at least one service is offline. 
     */
    private boolean checkAllOnline() {
        for(JLabel label : statusLabels) {
            if(!label.getText().equals(StaticData.SERVICE_ONLINE) && !label.getText().equals(StaticData.POLLING_OFF_MSG)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if all services are online.
     * 
     * @return True if there is at least one incident. 
     */
    private boolean checkForAnIncident() {
        if(!allIncidents.isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Set jToggleButton1's text when selected.
     */
    private void checkButtonTextOn() {
        jToggleButton1.setText(StaticData.BUTTON_POLLING_ON);
    }
        
    /**
     * Set jToggleButton1's text when not selected.
     */
    private void checkButtonTextOff() {
        jToggleButton1.setText(StaticData.BUTTON_POLLING_OFF);
    }
    
    /**
     * Toggles check button when changing to and from debug mode, and vice-versa.
     */
    private void toggleCheckButton() {
        if(jToggleButton1.isSelected()) {
            jToggleButton1.doClick(); // Toggle off
            jToggleButton1.doClick(); // Toggle on
        }
    }
    
    /**
     * Reset the server status labels to black when the program is not checking
     * status.
     */
    private void decolorize(JLabel label) {
        if(label.getText().equals(StaticData.POLLING_OFF_MSG)){
            label.setForeground(Color.black);
        }
    }
    
    /**
     * Displays a message in jTextBox1 if there is a network error.
     */
    private void networkErrorFound() {
        jToggleButton1.setSelected(false);
        checkButtonTextOff();
        resetStatusLabels();

        //JOptionPane.showMessageDialog(new JFrame(), 
        //    StaticData.NETWORK_ERROR_MSG , "Network Error", JOptionPane.ERROR_MESSAGE);
        
        jTextArea1.setForeground(Color.red);
        jTextArea1.setText(StaticData.NETWORK_ERROR_MSG);
    }
    
    /**
     * Minimizes the GUI to an icon on the notification area.
     */
    private void minimizeToTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            JOptionPane.showMessageDialog(new JFrame(), 
                "SystemTray is not supported" , "Minimize Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.setVisible(false);
        
        PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(this.getIconImage());
        final SystemTray tray = SystemTray.getSystemTray();
        
        // Create popup menu components
        info = new MenuItem();
        togglePolling = new MenuItem();
        setVariableMenuItems();
        info.setEnabled(false);
        MenuItem about = new MenuItem(StaticData.MENU_ABOUT);
        Menu setRegion = new Menu(StaticData.MENU_SET_REGION);
        setupRegionTrayMenu(setRegion);
        Menu setPolling = new Menu(StaticData.MENU_POLLING);
        setupPollingRateTrayMenu(setPolling);
        MenuItem maximize = new MenuItem(StaticData.MENU_MAXIMIZE);
        MenuItem quit = new MenuItem(StaticData.MENU_EXIT);
  
        //Add components to popup menu
        popup.add(info);
        popup.add(about);
        popup.addSeparator(); //=============
        popup.add(togglePolling);
        popup.addSeparator(); //=============
        popup.add(setRegion);
        popup.add(setPolling);
        popup.addSeparator(); //=============
        popup.add(maximize);
        popup.add(quit);
         
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
       
        about.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                displayAboutWindow();
            }        
        });    
               
        togglePolling.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                jToggleButton1.doClick();
                setVariableMenuItems();
            }        
        });
        
        setPolling.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                displayPollingRateWindow();
                setVariableMenuItems();
            }        
        });
        
        maximize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maximizeFromTray(tray, trayIcon);
            }        
        });
        
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }        
        });
    }
    
    /**
     * Restores the GUI from the notification area and cleans up icon.
     * 
     * @param tray The current system tray icon.
     * @param trayIcon The current system tray icon.
     */
    private void maximizeFromTray(SystemTray tray, TrayIcon trayIcon) {
        this.setVisible(true);
        tray.remove(trayIcon);
    }
    
    /**
     * Populates the system tray region menu.
     * 
     * @param setRegion The tray menu that contains the regions.
     */
    private void setupRegionTrayMenu(final Menu setRegion) {
        String[] regions = sdata.getRegions();
        final MenuItem[] regionMenuItems = new MenuItem[regions.length];
        int i = 0;

        for(final String r : regions) {
            final MenuItem currentRegion = new MenuItem(r);
            regionMenuItems[i] = currentRegion;
            setRegion.add(currentRegion);
            if(r.toLowerCase().equals(getCurrentRegion())) {
                currentRegion.setFont(new Font("default", Font.BOLD, 12));
            }
            i++;
        }

        // Add action listeners for all region menu items
        for(int j = 0; j < regionMenuItems.length; j++) {
            final int index = j;
            regionMenuItems[j].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(MenuItem mi : regionMenuItems) {
                        mi.setFont(new Font("default", Font.PLAIN, 12));
                    }
                    setCurrentRegion(regionMenuItems[index].getLabel());
                    regionMenuItems[index].setFont(new Font("default", Font.BOLD, 12));
                    jComboBox1.setSelectedIndex(index);
                    setVariableMenuItems();
                }        
            });
        }
    }
    
    /**
     * Populates the system tray polling rate menu.
     * 
     * @param setRegion The tray menu that contains the polling rates. 
     */
    private void setupPollingRateTrayMenu(final Menu setPolling) {
        String[] rates = sdata.getPollingRatesArr();
        final MenuItem[] pollingRateMenuItems = new MenuItem[rates.length];
        int i = 0;

        for(final String r : rates) {
            final MenuItem currentRate = new MenuItem(r);
            pollingRateMenuItems[i] = currentRate;
            setPolling.add(currentRate);
            if(Integer.parseInt(r) == getPollingRate()) {
                currentRate.setFont(new Font("default", Font.BOLD, 12));
            }
            i++;
        }

        // Add action listeners for all region menu items
        for(int j = 0; j < pollingRateMenuItems.length; j++) {
            final int index = j;
            pollingRateMenuItems[j].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(MenuItem mi : pollingRateMenuItems) {
                        mi.setFont(new Font("default", Font.PLAIN, 12));
                    }
                    setPollingRate(Integer.parseInt(pollingRateMenuItems[index].getLabel()));
                    pollingRateMenuItems[index].setFont(new Font("default", Font.BOLD, 12));
                    setVariableMenuItems();
                }        
            });
        }
    }
    
    /**
     * Populates tray menu items that periodically change.
     */
    private void setVariableMenuItems() {
        if(jToggleButton1.isSelected()) {
            info.setLabel("[" + getCurrentRegion().toUpperCase() + "] :: " + "Refreshing every " + getPollingRate() + "s");
            togglePolling.setLabel(StaticData.MENU_POLLING_OFF);
        }
        else {
            info.setLabel("[" + getCurrentRegion().toUpperCase() + "] :: " + "Not currently polling");
            togglePolling.setLabel(StaticData.MENU_POLLING_ON);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel10 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToggleButton1.setText("jToggleButton1");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel1.setText("jLabel1");

        jLabel2.setText("jLabel2");

        jLabel3.setText("jLabel3");

        jLabel4.setText("jLabel4");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("jLabel5");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("jLabel6");
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("jLabel7");
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("jLabel8");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jButton1.setText("jButton1");

        jButton2.setText("jButton1");

        jButton3.setText("jButton1");

        jButton4.setText("jButton1");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("jLabel9");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("jLabel10");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jMenu1.setText("jMenu1");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("jMenuItem1");
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("jMenuItem2");
        jMenu1.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("jMenuItem3");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("jMenu2");

        jMenu3.setText("jMenu3");

        jCheckBoxMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");
        jMenu3.add(jCheckBoxMenuItem1);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("jMenuItem5");
        jMenu3.add(jMenuItem5);

        jMenu2.add(jMenu3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("jMenuItem4");
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                        .addComponent(jToggleButton1)))
                .addGap(50, 50, 50))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(jLabel9))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(jLabel10)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6)
                    .addComponent(jButton2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8)
                    .addComponent(jButton4))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables

}
