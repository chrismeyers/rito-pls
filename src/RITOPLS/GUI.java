package RITOPLS;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
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
    
    /**
     * Creates new form GUI
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();
        
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
        
        jLabel1.setText(services[0]);
        jLabel2.setText(services[1]);
        jLabel3.setText(services[2]);
        jLabel4.setText(services[3]);
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
        jLabel5.setText(POLLING_OFF_MSG);
        jLabel6.setText(POLLING_OFF_MSG);
        jLabel7.setText(POLLING_OFF_MSG);
        jLabel8.setText(POLLING_OFF_MSG);
        deColorize();
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
                    while(jToggleButton1.isSelected()){
                        //p.pollTest(i, getCurrentRegion());
                        //i++;
                        try {
                            // Set current status for each service.
                            statusInfo = p.getStatus(getCurrentRegion());
                            
                            if(getCurrentRegion().equals("na") || getCurrentRegion().equals("oce")) {
                                // NA and OCE use "Boards" service.
                                statusValues = statusInfo.get("Boards");
                            }
                            else{
                                // All other regions use "Forums" service.
                                statusValues = statusInfo.get("Forums");
                            }
                            jLabel5.setText(formatOutput(statusValues.keySet().toString()));
                            
                            statusValues = statusInfo.get("Game");
                            jLabel6.setText(formatOutput(statusValues.keySet().toString()));
                                                        
                            statusValues = statusInfo.get("Store");
                            jLabel7.setText(formatOutput(statusValues.keySet().toString()));
                            
                            statusValues = statusInfo.get("Website");
                            jLabel8.setText(formatOutput(statusValues.keySet().toString()));

                            // Change font color.
                            colorize();
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
            
            private String formatOutput(String raw) {
                // Remove brackets
                String formatted = raw.replaceAll("[\\[\\]]", "");

                return formatted;
            }
            /**
             * Change the color of service status labels based on current state
             * of server.
             */
            private void colorize() {
                //=================Boards service label=================
                if(jLabel5.getText().equals("Online")) {
                    jLabel5.setForeground(Color.green);
                }
                else if(jLabel5.getText().equals("Offline")){
                    jLabel5.setForeground(Color.red);
                }
                else if(jLabel5.getText().equals("Alert")){
                    jLabel5.setForeground(Color.yellow);
                }
                else if(jLabel5.getText().equals("Deploying")){
                    jLabel5.setForeground(Color.blue);
                }
                
                //=================Game service label=================
                if(jLabel6.getText().equals("Online")) {
                    jLabel6.setForeground(Color.green);
                }
                else if(jLabel6.getText().equals("Offline")){
                    jLabel6.setForeground(Color.red);
                }
                else if(jLabel6.getText().equals("Alert")){
                    jLabel6.setForeground(Color.yellow);
                }
                else if(jLabel6.getText().equals("Deploying")){
                    jLabel6.setForeground(Color.blue);
                }
                
                //=================Store service label=================
                if(jLabel7.getText().equals("Online")) {
                    jLabel7.setForeground(Color.green);
                }
                else if(jLabel7.getText().equals("Offline")){
                    jLabel7.setForeground(Color.red);
                }
                else if(jLabel7.getText().equals("Alert")){
                    jLabel7.setForeground(Color.yellow);
                }
                else if(jLabel7.getText().equals("Deploying")){
                    jLabel7.setForeground(Color.blue);
                }
                   
                //=================Website service label=================
                if(jLabel8.getText().equals("Online")) {
                    jLabel8.setForeground(Color.green);
                }
                else if(jLabel8.getText().equals("Offline")){
                    jLabel8.setForeground(Color.red);
                }
                else if(jLabel8.getText().equals("Alert")){
                    jLabel8.setForeground(Color.yellow);
                }
                else if(jLabel8.getText().equals("Deploying")){
                    jLabel8.setForeground(Color.blue);
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
    private void deColorize() {
        //=================Boards service label=================
        if(jLabel5.getText().equals(POLLING_OFF_MSG)){
            jLabel5.setForeground(Color.black);
        }
        
        //=================Game service label=================
        if(jLabel6.getText().equals(POLLING_OFF_MSG)){
            jLabel6.setForeground(Color.black);
        }
        
        //=================Store service label=================
        if(jLabel7.getText().equals(POLLING_OFF_MSG)){
            jLabel7.setForeground(Color.black);
        }
        
        //=================Website service label=================
        if(jLabel8.getText().equals(POLLING_OFF_MSG)){
            jLabel8.setForeground(Color.black);
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
                .addGap(55, 55, 55)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(jToggleButton1)))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8))
                .addContainerGap(73, Short.MAX_VALUE))
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
