package RITOPLS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
    private JLabel[] serviceLabels;
    private JLabel[] statusLabels;
    private JButton[] incidentButtons;
    
    /**
     * Creates new form GUI
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();
        serviceLabels = new JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4};
        statusLabels = new JLabel[]{jLabel5, jLabel6, jLabel7, jLabel8};
        incidentButtons = new JButton[]{jButton1, jButton2, jButton3, jButton4};
        
        setupMenus();
        setTextWhenOff(); // default state
        setPollingRate(5); // 5 seconds by default
        populateRegionComboBox(sdata.getRegions());
        
        //Initialize region to first item in ComboBox (NA)
        region = jComboBox1.getSelectedItem().toString().toLowerCase();
        
        populateServicesLabels();
  
        p = new Parser(region);
        
        // Listen for changes in region combo box state.
        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCurrentRegion(jComboBox1.getSelectedItem().toString());
                // Update labels in case naming convention in current region changed.
                populateServicesLabels();
            }        
        });
        
        // Listen for changes in check button state.
        jToggleButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jToggleButton1.isSelected()){
                    setTextWhenOn();
                }
                else{
                    setTextWhenOff();
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
     * Initializes menu items and adds ActionListeners to these items.
     */
    private void setupMenus() {
        this.setTitle("League of Legends Server Status Checker");
        this.setResizable(false);
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
                for(int invl = 5; invl <= 60;invl+=5) {
                    intervals.add(invl +  "");
                    
                    if(invl % 20 == 0){
                        invl += 10;
                    }
                                        
                    if(invl % 10 == 0){
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
     * 
     * @param services The available services to check.
     */
    private void populateServicesLabels() {
        String[] services;
        
        // NA and OCE use "Boards" service
        if(getCurrentRegion().equals("na") || getCurrentRegion().equals("oce")) {
            services = sdata.getServicesB();
        }
        // All others use "Forums" service
        else{
            services = sdata.getServicesF();
        }
        
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
    private void setTextWhenOff() {
        jToggleButton1.setText("Click to check");
        
        // Set label and button values
        for(int i = 0; i < statusLabels.length; i++){
            JLabel label = statusLabels[i];
            JButton button = incidentButtons[i];
            
            label.setText(POLLING_OFF_MSG);
            button.setEnabled(false);
            button.setText(POLLING_OFF_MSG);
            button.setBackground(null);
            decolorize(label);
        }
        
        // Set polling rate info label to blank when polling is off
        jLabel9.setText("Not Currently Polling Server Status.");
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Setup incident textbox.
        jTextArea1.setEditable(false);
        jTextArea1.setText("No incidents to report!");
        
    }
    
    /**
     * Sets the rate at which the program queries the API.
     * Default is every 10 seconds.
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
        jToggleButton1.setText("Checking...");
        
        // Creates a second thread to periodically check for a change in server
        // status.
        Runnable poll = new Runnable() {
            @Override
            public void run() {
                //synchronized(p) {
                    //int i = 0;
                    HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo = new HashMap();
                    HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>> statusValues = new HashMap();
                    ArrayList<HashMap<String, HashMap<String, String>>> services = new ArrayList<HashMap<String, HashMap<String, String>>>();
                    HashMap<String, HashMap<String, String>> incidents = new HashMap();
                    HashMap<String, String> content = new HashMap();
                    
                    while(jToggleButton1.isSelected()){
                        //p.pollTest(i, getCurrentRegion());
                        //i++;
                        try {
                            // Set current status for each service.
                            statusInfo = p.getStatus(getCurrentRegion());
                            setStatusStrings(statusInfo, statusValues, services, incidents, content);
                            
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        try {
                            // Refresh server status, default is 10 seconds
                            Thread.sleep(getPollingRate() * 1000);
                            p.pollTest(getPollingRate(), getCurrentRegion());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                //}
            }
            
            /**
             * Set status strings on GUI and handles incidents.
             * 
             * @param statusInfo All parsed information.
             * @param statusValues Contains service statuses.
             * @param services Contains data for all services.
             * @param incidents Contains the incidents.
             * @param content Contains severity, updatedTime and contentString.
             */
            private void setStatusStrings(
                    HashMap<String, HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>>> statusInfo,
                    HashMap<String, ArrayList<HashMap<String, HashMap<String, String>>>> statusValues,
                    ArrayList<HashMap<String, HashMap<String, String>>> services,
                    HashMap<String, HashMap<String, String>> incidents,
                    HashMap<String, String> content) {
                
                String serviceString, severity, updatedTime, contentString = "";
                
                // Set polling rate info label
                setPollingInfoLabel();
                
                for(int service = 0; service < statusLabels.length; service++) {
                    if(getCurrentRegion().equals("na") || getCurrentRegion().equals("oce")) {
                        // NA and OCE use "Boards" service.
                        serviceString = sdata.getServiceB(service);
                        statusValues = statusInfo.get(serviceString);
                    }
                    else {
                        // All other regions use "Forums" service.
                        serviceString = sdata.getServiceF(service);
                        statusValues = statusInfo.get(serviceString);
                    }
                    String status = statusValues.keySet().toString();
                
                    statusLabels[service].setText(formatOutput(status));
                    
                    colorize(statusLabels[service]);
                    
                    // Handle incidents.
                    services = statusValues.get(formatOutput(status));
                    
                    if(!services.isEmpty()) {
                        for(int serv = 0; serv < services.size(); serv++){
                            incidents = services.get(serv);
                            content = incidents.get(serviceString);

                            severity = content.get("severity");
                            updatedTime = content.get("updated_at");
                            contentString = content.get("content");

                            populateIncidentButton(service, serviceString, severity, updatedTime, contentString);
                            
                            System.out.println("[" +getCurrentRegion().toUpperCase() + " " + serviceString + "] :: " + severity + " :: " + updatedTime + " :: " + contentString);
                        }
                    }
                    services.clear();
                }
            }  
            
            /**
             * Updates incident buttons based on severity and provides a pop-up
             * box containing the incident(s).
             * 
             * @param service The current service that has an incident.
             * @param severity The severity of the current incident.
             * @param time The last updated time of the incident.
             * @param content What the incident is.
             */
            private void populateIncidentButton(int service, String serviceString,
                                                String severity, String time, String content) {
                
                final JButton button = incidentButtons[service];
                button.setEnabled(true);
                
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
                
                final String currentSeverity = severity;
                final String currentTime = time;
                final String currentContent = content;
                final String currentService = serviceString;
                final JTextArea textarea = jTextArea1;
                
                
                Runnable dialog = new Runnable() {
                    @Override
                    public void run() {
                        button.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                textarea.setText("");
                                textarea.append("[" + getCurrentRegion().toUpperCase() +
                                        " " + currentService + "] :: "  + currentSeverity +
                                        " :: "+ currentTime + " :: " + currentContent + "\n");
                            }
                        });
                    }
                };
                Thread dialogThread = new Thread(dialog);
                dialogThread.start();
            }
            
            /**
             * Sets the value of the polling info label (jLabel9) based on the 
             * current polling rate.
             */
            private void setPollingInfoLabel() {
                if(getPollingRate() == 1) {
                    jLabel9.setText("Refreshing every " + getPollingRate() + " second...");
                }
                else {
                    jLabel9.setText("Refreshing every " + getPollingRate() + " seconds...");
                }
                jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
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
             * Change the color of service status labels based on current state
             * of server.
             */
            private void colorize(JLabel label) {
                if(label.getText().equals("Online")) {
                    label.setForeground(Color.green);
                }
                else if(label.getText().equals("Offline")){
                    label.setForeground(Color.red);
                }
                else if(label.getText().equals("Alert")){
                    label.setForeground(Color.yellow);
                }
                else if(label.getText().equals("Deploying")){
                    label.setForeground(Color.blue);
                }
            }
        };
        
        Thread pollThread = new Thread(poll);
        pollThread.start();
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
