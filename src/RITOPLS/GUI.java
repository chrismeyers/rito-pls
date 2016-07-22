package RITOPLS;

import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
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
    private final StatusParser parser;
    private StatusHandler handler;
    private NotificationTray notif;
    private final StaticData sdata;
    private int pollingRate;
    private final JLabel[] serviceLabels;
    private final JLabel[] statusLabels;
    private final JButton[] incidentButtons;
    private JButton lastClicked;
    private boolean regionChanged;
    private TrayIcon trayIcon;
    private MenuItem info;
    private MenuItem notifPing;
    private MenuItem togglePolling;
    private final SystemTray tray;
    
    /**
     * Creates new form GUI
     * 
     * @throws java.io.IOException
     */
    public GUI() throws IOException {
        initComponents();
        sdata = new StaticData();
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
        
        parser = new StatusParser(region);
        handler = null;
        setTextWhenOff(); // default state
        tray = SystemTray.getSystemTray();
        
        clearPingLabels();
        
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
                    handler.interruptThreads();
                }
            }        
        });
        
        // Listen for changes in check button state.
        jToggleButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jToggleButton1.isSelected()) {
                    try {
                        if(parser.networkCheck(getCurrentRegion())) {
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
                    handler.interruptThreads();
                    lastClicked = null;
                }
            }        
        });  
    }
    
    //========================== GLOBAL GETTERS ============================
    protected JToggleButton getJToggleButton(int which) {
        switch(which) {
            case 1:
                return jToggleButton1;
            default:
                return null;
        }
    }
    
    protected JTextArea getJTextArea(int which) {
        switch(which) {
            case 1:
                return jTextArea1;
            default:
                return null;
        }
    }
    
    protected JComboBox getJComboBox(int which) {
        switch(which) {
            case 1:
                return jComboBox1;
            default:
                return null;
        }
    }
    
    protected JLabel getJLabel(int which) {
        switch(which) {
            case 9:
                return jLabel9;
            case 12:
                return jLabel12;
            default:
                return null;
        }
    }
    
    protected JLabel[] getStatusLabels() {
        return statusLabels;
    }
    
    protected JButton getLastClicked() {
        return lastClicked;
    }
    
    protected JButton[] getIncidentButtons() {
        return incidentButtons;
    }
    
    protected StatusParser getParser() {
        return parser;
    }
    
    protected StaticData getStaticData() {
        return sdata;
    }
    
    protected NotificationTray getNotifTray() {
        return notif;
    }
    
    protected boolean hasRegionChanged() {
        return regionChanged;
    }
    
    protected MenuItem getInfoTrayMenuItem() {
        return info;
    }
    
    protected MenuItem getPingTrayMenuItem() {
        return notifPing;
    }
    
    protected MenuItem getTogglePollingTrayMenuItem() {
        return togglePolling;
    }
        
    protected TrayIcon getTrayIcon() {
        return trayIcon;
    }
    
    protected SystemTray getSystemTray() {
        return tray;
    }

    //========================== GLOBAL SETTERS ============================
    protected void setLastClicked(JButton last) {
        lastClicked = last;
    }
    
    protected void setRegionChanged(boolean change) {
        regionChanged = change;
    }
    
    protected void setTrayIcon(Image icon) {
        trayIcon = new TrayIcon(icon); 
    }
    
    protected void setInfoTrayMenuItem() {
        info = new MenuItem();
    }
    
    protected void setPingTrayMenuItem() {
        notifPing = new MenuItem();
    }
        
    protected void setTogglePollingTrayMenuItem() {
        togglePolling = new MenuItem();
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
                try {
                    minimizeToTray();
                } catch (IOException ex) {}
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
                parser.toggleDebugMode();
                if(parser.getDebugStatus()) {
                    jLabel10.setText(StaticData.DEBUGGING_ON_MSG);
                    setTitleInDebug(true);
                }
                else {
                    jLabel10.setText(StaticData.DEBUGGING_OFF_MSG);
                    setTitleInDebug(false);
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
                    parser.setDebugFile(selectedFile.getAbsolutePath());
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
     * Allows access to setTitle() from within actionListeners.
     * 
     * @param debugMode The current state of debugging.
     */
    private void setTitleInDebug(boolean debugMode) {
        if(debugMode) {
            this.setTitle(StaticData.DEBUG_TAG + " " + StaticData.PROGRAM_TITLE);
        }
        else {
            this.setTitle(StaticData.PROGRAM_TITLE);
        }
    }
    
    /**
     * Raises a window showing info about the program.
     */
    protected void displayAboutWindow() {
        JOptionPane.showMessageDialog(new JFrame(), 
            StaticData.ABOUT_ABOUT_MSG + StaticData.ABOUT_LEGAL_MSG,
            StaticData.ABOUT_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Populates and raises a window used to specify the polling rate for the
     * program.
     */
    protected void displayPollingRateWindow() {
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
        String[] services = sdata.getCurrentServiceNames(getCurrentRegion());
 
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
    protected void setCurrentRegion(String newRegion) {
        region = newRegion.toLowerCase();
    }
    
    /**
     * Sets the default status label values.
     */
    private void resetStatusLabels() {
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
    protected void turnAllIncidentButtonsOff() {
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
    protected String setNewTextAreaMessage() {
        jTextArea1.setForeground(Color.black);

        if(jToggleButton1.isSelected() && !handler.getAllIncidents().isEmpty()) {
            return StaticData.INCIDENTS_AVAILABLE;
        }       
        
        return StaticData.NO_INCIDENTS_AVAILABLE;
    }
    
    /**
     * Sets the rate at which the program queries the API.
     * 
     * @param rate The rate of checking servers (in seconds)
     */
    protected void setPollingRate(int rate) {
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
     * Set server status labels when not checking the server status.
     * 
     * @throws java.io.IOException
     */
    protected void setTextWhenOff() throws IOException {
        if(parser.networkCheck(getCurrentRegion())) {
            jTextArea1.setText(setNewTextAreaMessage());
        }
        else {
            networkErrorFound();
        }

        checkButtonTextOff();
        resetStatusLabels();
        turnAllIncidentButtonsOff();
        clearPingLabels();

        // Setup incident textbox.
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);  
    }
    
    /**
     * Adjust values of server status labels when checking is enabled.
     * 
     * @throws java.io.IOException
     */
    private void setTextWhenOn() throws IOException {
        handler = new StatusHandler(this);
        handler.setTextWhenOn();
    }
    
    /**
     * Sets the GUI form icon based on the status of services for the current
     * region.
     */
    protected void setFormIcon() {
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
            getTrayIcon().setImage(img);
        }
    }
    
    /**
     * Checks to see if all services are online.
     * 
     * @return True if all services are online, false otherwise. 
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
     * Checks to see if there are any incidents for any service.
     * 
     * @return True if there is at least one incident, false otherwise. 
     */
    private boolean checkForAnIncident() {
        if(!handler.getAllIncidents().isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Set jToggleButton1's text when selected.
     */
    protected void checkButtonTextOn() {
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
    protected void toggleCheckButton() {
        if(jToggleButton1.isSelected()) {
            jToggleButton1.doClick(); // Toggle off
            jToggleButton1.doClick(); // Toggle on
        }
    }
    
    /**
    * Change the color of service status labels based on current state
    * of server.
    * 
    * @param label The label to colorize.
    */
   protected void colorize(JLabel label) {
       switch (label.getText()) {
           case StaticData.SERVICE_ONLINE:
               label.setForeground(Color.green);
               break;
           case StaticData.SERVICE_OFFLINE:
               label.setForeground(Color.red);
               break;
           case StaticData.SERVICE_ALERT:
               label.setForeground(Color.orange);
               break;
           case StaticData.SERVICE_DEPLOYING:
               label.setForeground(Color.blue);
               break;
           default:
               label.setForeground(Color.magenta);
               break;
       }
   }
    
    /**
     * Reset the server status labels to black when the program is not checking
     * status.
     * 
     * @param label The label to decolorize. 
     */
    private void decolorize(JLabel label) {
        if(label.getText().equals(StaticData.POLLING_OFF_MSG)){
            label.setForeground(Color.black);
        }
    }
    
    /**
     * Displays a message in jTextBox1 if there is a network error.
     */
    protected void networkErrorFound() {
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
     * 
     * @throws java.io.IOException
     */
    private void minimizeToTray() throws IOException {
        notif = new NotificationTray(this);
        notif.minimizeToTray();
    }
    
    /**
     * Sets ping labels to "Off" state.
     */
    private void clearPingLabels() {
        jLabel11.setText("");
        jLabel12.setText("");
    }
    
    /**
     * Sets the ping label value for the current region.
     */
    protected void setPingValue() {
        String ping = parser.determinePing(sdata.getRegionIp(getCurrentRegion()));
        double pingValue = 999.99;
        try {
            pingValue = Double.parseDouble(ping.substring(0, ping.indexOf("ms")).trim());
        }
        catch(StringIndexOutOfBoundsException ex){
        }
        
        jLabel11.setText(getCurrentRegion().toUpperCase() + " ping is ");
        jLabel12.setText(ping);
        
        if(pingValue > 0 && pingValue < 50) {
            jLabel12.setForeground(Color.green);
        }
        else if(pingValue > 50 && pingValue < 150) {
            jLabel12.setForeground(Color.orange);
        }
        else {
            jLabel12.setForeground(Color.red);
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
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
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

        jLabel11.setText("jLabel11");

        jLabel12.setText("jLabel12");

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
                        .addComponent(jLabel10))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addContainerGap(18, Short.MAX_VALUE))
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
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
