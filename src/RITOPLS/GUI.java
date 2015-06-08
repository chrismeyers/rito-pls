package RITOPLS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;

/**
 * This class provides a GUI interface that reflects the status of League of Legends 
 * services for the region specified by the user.
 * 
 * @author Chris Meyers
 */
public class GUI extends javax.swing.JFrame {
    private String region;
    private Parser p;
    private StaticData sdata;
    
    /**
     * Creates new form GUI
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();

        populateRegionComboBox(sdata.getRegions());
        populateServicesLabels(sdata.getServices());
        setTextWhenOff(); // default state

        //Initialize region to first item in ComboBox (NA)
        region = jComboBox1.getSelectedItem().toString().toLowerCase();   
        
        p = new Parser(region);
        
        // Listen for changes in region combo box state.
        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCurrentRegion(jComboBox1.getSelectedItem().toString());
            }        
        });
        
        // Listen for changes in check button state.
        jToggleButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!jToggleButton1.isSelected()){
                    setTextWhenOff();
                }
                else{
                    setTextWhenOn();
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
     * Populates jLabel1-4 with the available services.
     * 
     * @param services The available services to check.
     */
    private void populateServicesLabels(String[] services) {
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
        jLabel5.setText("N/A");
        jLabel6.setText("N/A");
        jLabel7.setText("N/A");
        jLabel8.setText("N/A");
        deColorize();
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
                int i = 0;
                while(jToggleButton1.isSelected()){
                    //p.pollTest(i, getCurrentRegion());
                    i++;
                    try {
                        // Set current status for each service.
                        jLabel5.setText(p.getStatus(getCurrentRegion(), sdata.getService(0)));
                        jLabel6.setText(p.getStatus(getCurrentRegion(), sdata.getService(1)));
                        jLabel7.setText(p.getStatus(getCurrentRegion(), sdata.getService(2)));
                        jLabel8.setText(p.getStatus(getCurrentRegion(), sdata.getService(3)));
                        
                        // Change font color.
                        colorize();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    
                    try {
                        // Refresh every 10 seconds.
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
                    jLabel5.setForeground(Color.red);
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
                    jLabel5.setForeground(Color.blue);
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
        if(jLabel5.getText().equals("N/A")){
            jLabel5.setForeground(Color.black);
        }
        
        //=================Game service label=================
        if(jLabel6.getText().equals("N/A")){
            jLabel6.setForeground(Color.black);
        }
        
        //=================Store service label=================
        if(jLabel7.getText().equals("N/A")){
            jLabel7.setForeground(Color.black);
        }
        
        //=================Website service label=================
        if(jLabel8.getText().equals("N/A")){
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
                .addContainerGap(94, Short.MAX_VALUE))
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
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables

}
