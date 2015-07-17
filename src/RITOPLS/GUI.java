package RITOPLS;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

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
    private static final String POLLING_OFF_MSG = "N/A";
    private static final String INCIDENTS_AVAILABLE = "Incidents available for review.";
    private static final String NO_INCIDENTS_AVAILABLE = "No incidents to report!";
    private static final String NOT_POLLING_MSG = "Not Currently Polling Server Status.";
    private static final int DEFAULT_POLLING_RATE = 10;
    private final JLabel[] serviceLabels;
    private final JLabel[] statusLabels;
    private final JButton[] incidentButtons;
    private final HashMap<String, ArrayList<HashMap<String, String>>> allIncidents;
    private boolean regionChanged;
    Thread pollThread, counterThread;
    
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
        setPollingRate(DEFAULT_POLLING_RATE);
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
                
                // Clean array for new region.
                allIncidents.clear();
                
                // Keeps jToggleButton1's text from incorrectly changing to 
                //"Checking..."when the region is changed and jToggleButton is disabled.
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
        this.setTitle("League of Legends Server Status Checker");
        this.setResizable(false);
        setFormIcon();
        jMenuItem1.setText("Set Polling Rate");
        jMenuItem2.setText("Quit");
        jMenuItem3.setText("About");
        
        // Polling rate menu item listener
        jMenuItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> intervals = new ArrayList<String>();
                String pollingRate = "";
                int rate = 0;
                
                intervals.add("1");
                for(int invl = 5; invl <= 60; invl+=5) {
                    intervals.add(invl +  "");
                    
                    if(invl % 20 == 0) {
                        invl += 10;
                    }
                                        
                    if(invl % 10 == 0) {
                        invl += 5;
                    }
                }
                
                String startingValue = getPollingRate() + "";
                
                pollingRate = (String) JOptionPane.showInputDialog(new JFrame(), 
                    "How often should the server be checked\n(in seconds) ?",
                    "Polling rate",
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    intervals.toArray(), 
                    intervals.get(intervals.indexOf(startingValue)));
                    
                if(pollingRate == null) {   
                    // User Pressed cancel, get current rate.
                    pollingRate = startingValue;
                }

                rate = Integer.parseInt(pollingRate);
                
                setPollingRate(rate);
            }        
        });
        
        // Quit menu item listener
        jMenuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }        
        });
        
        // About menu item listener
        jMenuItem3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String about = 
                        "Developed by: Chris Meyers || http://chrismeyers.info\n\n"
                        + "rito-pls is a java application currently under development\n"
                        + "that reports the current League of Legends service statuses\n"
                        + "for a specified region. The applicationqueries the League of\n"
                        + "Legends API periodically and presents the current status of\n"
                        + "several services (Boards, Game, Store and Website).\n\n";
                String legal = 
                        "riot-pls isn’t endorsed by Riot Games and doesn’t reflect the\n"
                        + "views or opinions of Riot Games or anyone officially involved\n"
                        + "in producing or managing League of Legends. League of Legends\n"
                        + "and Riot Games are trademarks or registered trademarks of Riot\n"
                        + "Games, Inc. League of Legends © Riot Games, Inc.";
                JOptionPane.showMessageDialog(new JFrame(), 
                            about + legal,
                            "About", JOptionPane.INFORMATION_MESSAGE);
            }        
        });
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
            label.setText(POLLING_OFF_MSG);
            decolorize(label);
        }
        
        // Set polling rate info label to blank when polling is off
        jLabel9.setText(NOT_POLLING_MSG);
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /**
     * Sets all incident buttons to default "off" state
     */
    private void turnAllIncidentButtonsOff(){
        for (JButton button : incidentButtons) {
            button.setEnabled(false);
            button.setText(POLLING_OFF_MSG);
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
            return INCIDENTS_AVAILABLE;
        }       
        
        return NO_INCIDENTS_AVAILABLE;
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
                            // Set current status for each service.
                            try {
                                statusInfo = p.getStatus(getCurrentRegion());
                            }
                            catch(UnknownHostException e) {
                                setTextWhenOff();
                                break;
                            }
                            
                            setStatusStrings(statusInfo);
                            
                            if(regionChanged) {
                                jTextArea1.setText(setNewTextAreaMessage());
                                regionChanged = false;
                            }
                            
                            setFormIcon();
                            
                        } 
                        catch (IOException ex) {} 
                        catch (InterruptedException ex) {}

                        try {
                            Thread.sleep(getPollingRate() * 1000);
                            turnAllIncidentButtonsOff();
                            
                            if(p.networkCheck(getCurrentRegion())) {
                                // Throws network errors (IOException from not 
                                // being able to openStream()).
                            }
                                                    
                            p.pollTest(getPollingRate(), getCurrentRegion());
                        } catch (InterruptedException e) {
                            //Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, e);
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
                
                String incidentString, serviceString, status, severity, updatedTime, contentString, area = "";
                ArrayList<HashMap<String, String>> currentServiceIncList;
                HashMap<String, String> currentInc = new HashMap();
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
                            
                            populateIncidentButton(service, severity);
                            populateIncidentBox(service, serviceString);
                            
                            System.out.println(area + " :: " + severity + " :: "+ updatedTime + " :: " + contentString);
                        }
                    }
                }
            }  
            
            /**
             * Updates incident buttons based on severity.
             * 
             * @param service The current service that has an incident.
             * @param severity The severity of the current incident.
             */
            private void populateIncidentButton(int service, String severity) {
                
                final JButton button = incidentButtons[service];
                button.setEnabled(true);
                
                // TODO: set severity to prioritize the most severe incident in list.
                
                switch(severity) {
                    case "Warn":
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText("!");
                        break;
                        
                    case "Info":
                        button.setForeground(Color.white);
                        button.setBackground(Color.black);
                        button.setText("!");
                        break;
                        
                    case "Alert":
                        button.setForeground(Color.yellow);
                        button.setBackground(Color.black);
                        button.setText("!!");
                        break;
                        
                    case "Error":
                        button.setForeground(Color.red);
                        button.setBackground(Color.black);
                        button.setText("!!!");
                        break;
                        
                    default:
                        button.setForeground(Color.magenta);
                        button.setBackground(Color.black);
                        button.setText("?");
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
                final JTextArea textarea = jTextArea1;
                
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        textarea.setForeground(Color.black);
                        textarea.setText("");
                        for(int i = 0; i < allIncidents.get(currentService).size(); i++) {
                            textarea.append(allIncidents.get(currentService).get(i).get("area") + " :: ");
                            textarea.append(allIncidents.get(currentService).get(i).get("severity") + " :: ");
                            textarea.append(allIncidents.get(currentService).get(i).get("updatedTime") + " :: ");
                            textarea.append(allIncidents.get(currentService).get(i).get("contentString"));
                            
                            if(i != allIncidents.get(currentService).size()-1) {
                                textarea.append("\n\n");
                            }
                            
                            // "Scroll" to top of jTextBox1
                            textarea.setCaretPosition(0);
                        }
                    }
                });
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
                    case "Online":
                        label.setForeground(Color.green);
                        break;
                    case "Offline":
                        label.setForeground(Color.red);
                        break;
                    case "Alert":
                        label.setForeground(Color.yellow);
                        break;
                    case "Deploying":
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
    
    private void setFormIcon() {
        Image img;
        
        if(!jToggleButton1.isSelected()) {
            // Grey Icon - IDLE
            img = resources.ResourceLoader.getImage("iconIDLE.png");
        }
        else if(checkAllOnline() && jToggleButton1.isSelected()) {
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
            if(label.toString().equals("Offline")) {
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
        jToggleButton1.setText("Checking...");
    }
        
    /**
     * Set jToggleButton1's text when not selected.
     */
    private void checkButtonTextOff() {
        jToggleButton1.setText("Click to check");
    }
    
    /**
     * Reset the server status labels to black when the program is not checking
     * status.
     */
    private void decolorize(JLabel label) {
        if(label.getText().equals(POLLING_OFF_MSG)){
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

        String error = "A connection to the server was unable to be made.\n\n"
           + "Either Riot's API servers are unresponsive or your network is "
           + "experiencing issues.  Please check your connection and try "
           + "again by toggling the \"Click to check\" button.";

        //JOptionPane.showMessageDialog(new JFrame(), 
        //    error , "Network Error", JOptionPane.ERROR_MESSAGE);
        jTextArea1.setForeground(Color.red);
        jTextArea1.setText(error);
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
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

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

        jLabel5.setText("jLabel5");

        jLabel6.setText("jLabel6");

        jLabel7.setText("jLabel7");

        jLabel8.setText("jLabel8");

        jButton1.setText("jButton1");

        jButton2.setText("jButton1");

        jButton3.setText("jButton1");

        jButton4.setText("jButton1");

        jLabel9.setText("jLabel9");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jMenu1.setText("File");

        jMenuItem1.setText("jMenuItem1");
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("jMenuItem2");
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");

        jMenuItem3.setText("jMenuItem3");
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                        .addComponent(jToggleButton1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(50, 50, 50))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
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
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables

}
