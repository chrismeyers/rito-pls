package RITOPLS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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
    
    /**
     * Creates new form GUI
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();
        serviceLabels = new JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4};
        statusLabels = new JLabel[]{jLabel5, jLabel6, jLabel7, jLabel8};
        
        setupMenus();
        setTextWhenOff(); // default state
        setPollingRate(10); // 10 seconds by default
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
        
        jMenuItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }        
        });
        
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
        
        for(int i = 0; i < statusLabels.length; i++){
            JLabel label = statusLabels[i];
            label.setText(POLLING_OFF_MSG);
            decolorize(label);
        }
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
        Runnable r = new Runnable() {
            @Override
            public void run() {
                synchronized(p) {
                    //int i = 0;
                    HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>> statusInfo = new HashMap();
                    HashMap<String, ArrayList<ArrayList<ArrayList<String>>>> statusValues = new HashMap();
                    ArrayList<ArrayList<ArrayList<String>>> services = new ArrayList<ArrayList<ArrayList<String>>>();
                    ArrayList<ArrayList<String>> incidents = new  ArrayList<ArrayList<String>>();
                    ArrayList<String> content = new ArrayList<String>();
                    
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
                }
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
                    HashMap<String, HashMap<String, ArrayList<ArrayList<ArrayList<String>>>>> statusInfo,
                    HashMap<String, ArrayList<ArrayList<ArrayList<String>>>> statusValues,
                    ArrayList<ArrayList<ArrayList<String>>> services,
                    ArrayList<ArrayList<String>> incidents,
                    ArrayList<String> content) {
                
                String serviceString, severity, updatedTime, contentString = "";
                
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
                    
                    for(int serv = 0; serv < services.size(); serv++){
                        incidents = services.get(serv);
                        content = incidents.get(0);

                        severity = content.get(0);
                        updatedTime = content.get(1);
                        contentString = content.get(2);
                        System.out.println("[" +getCurrentRegion().toUpperCase() + " " + serviceString + "] :: " + severity + " :: " + updatedTime + " :: " + contentString);
                    }
                }
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
        
        Thread checkThread = new Thread(r);
        checkThread.start();
    }
    
    /**
     * Reset the server status labels to black when the program is not checking
     * status.
     */
    private void decolorize(JLabel label) {
        //=================Boards service label=================
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
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                        .addComponent(jToggleButton1)))
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
                .addContainerGap(50, Short.MAX_VALUE))
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables

}
