package ritopls;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Handles notification tray functionality.
 * 
 * @author Chris Meyers
 */
public class NotificationTray {
    private final GUI gui;
    private final MenuItem update;
    private final MenuItem ping;
    private final MenuItem polling;
    private TrayIcon trayIcon;
    private final SystemTray tray;
    
    /**
     * NotificationTray constructor.
     * 
     * @param g The current GUI instance.
     * @throws IOException 
     */
    public NotificationTray(GUI g) throws IOException {
        gui = g;
        update = new MenuItem();
        ping = new MenuItem();
        polling = new MenuItem();
        tray = SystemTray.getSystemTray();
    }
    
    /**
     * Minimizes the GUI to an icon on the notification area.
     */
    public void minimizeToTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            JOptionPane.showMessageDialog(new JFrame(), 
                "SystemTray is not supported" , "Minimize Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        gui.setVisible(false);
        
        PopupMenu popup = new PopupMenu();
        //trayIcon.setTrayIcon(gui.getIconImage());
        gui.setFormIcon();

        // Create popup menu components
        setVariableMenuItems(-1);
        update.setEnabled(false);
        ping.setEnabled(false);
        MenuItem about = new MenuItem(StaticData.MENU_ABOUT);
        Menu setRegion = new Menu(StaticData.MENU_SET_REGION);
        setupRegionTrayMenu(setRegion);
        Menu setPolling = new Menu(StaticData.MENU_POLLING);
        setupPollingRateTrayMenu(setPolling);
        MenuItem maximize = new MenuItem(StaticData.MENU_MAXIMIZE);
        MenuItem quit = new MenuItem(StaticData.MENU_EXIT);
  
        //Add components to popup menu
        popup.add(update);
        popup.add(ping);
        popup.add(about);
        popup.addSeparator(); //=============
        popup.add(polling);
        popup.addSeparator(); //=============
        popup.add(setRegion);
        popup.add(setPolling);
        popup.addSeparator(); //=============
        popup.add(maximize);
        popup.add(quit);
         
        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
       
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVariableMenuItems(-1);
                
                if(e.getClickCount() >= 2) {
                    try {
                        // Prevents other tray icons from accidentally being clicked.
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {}
                    
                   maximizeFromTray();
                }
            }
            
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        
        about.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                gui.displayAboutWindow();
            }        
        });    
               
        polling.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                gui.getJToggleButton(1).doClick();
                setVariableMenuItems(-1);
            }        
        });
        
        setPolling.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                setVariableMenuItems(-1);
            }        
        });
        
        maximize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maximizeFromTray();
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
     */
    private void maximizeFromTray() {
        gui.setVisible(true);
        tray.remove(trayIcon);
    }
    
    /**
     * Populates the system tray region menu.
     * 
     * @param setRegion The tray menu that contains the regions.
     */
    private void setupRegionTrayMenu(final Menu setRegion) {
        String[] regions = StaticData.getRegions();
        final MenuItem[] regionMenuItems = new MenuItem[regions.length];
        int i = 0;

        for(final String r : regions) {
            final MenuItem currentRegion = new MenuItem(r);
            regionMenuItems[i] = currentRegion;
            setRegion.add(currentRegion);
            if(r.toLowerCase().equals(gui.getCurrentRegion())) {
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
                    gui.setCurrentRegion(regionMenuItems[index].getLabel());
                    regionMenuItems[index].setFont(new Font("default", Font.BOLD, 12));
                    gui.getJComboBox(1).setSelectedIndex(index);
                    setVariableMenuItems(-1);
                }        
            });
        }
    }
    
    /**
     * Populates the system tray polling rate menu.
     * 
     * @param setPolling The tray menu that contains the polling rates. 
     */
    private void setupPollingRateTrayMenu(final Menu setPolling) {
        String[] rates = StaticData.getPollingRatesArr();
        final MenuItem[] pollingRateMenuItems = new MenuItem[rates.length];
        int i = 0;

        for(final String r : rates) {
            final MenuItem currentRate = new MenuItem(r + "s");
            pollingRateMenuItems[i] = currentRate;
            setPolling.add(currentRate);
            if(Integer.parseInt(r) == gui.getPollingRate()) {
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
                    gui.setPollingRate(sanitizePollingRate(pollingRateMenuItems[index].getLabel()));
                    pollingRateMenuItems[index].setFont(new Font("default", Font.BOLD, 12));
                    setVariableMenuItems(-1);
                }        
            });
        }
    }
    
    /**
     * Populates tray menu items that periodically change.
     * 
     * @param refresh the time until the next refresh.
     */
    protected void setVariableMenuItems(int refresh) {
        if(gui.getJToggleButton(1).isSelected()) {
            if(refresh != -1) {
                update.setLabel("[" + gui.getCurrentRegion().toUpperCase() + "] :: " + "Refreshing in " + refresh + "s");
            }
            ping.setLabel(gui.getCurrentRegion().toUpperCase() + " ping is " + gui.getParser().getPing());
            polling.setLabel(StaticData.MENU_POLLING_OFF);
        }
        else {
            update.setLabel("[" + gui.getCurrentRegion().toUpperCase() + "] :: " + "Not currently polling");
            ping.setLabel(gui.getCurrentRegion().toUpperCase() + " ping is " + "Not Available");
            polling.setLabel(StaticData.MENU_POLLING_ON);
        }
    }
    
    /**
     * Takes the "s" off the polling rate menu selection and returns the 
     * selected string as an int.
     * 
     * @param menuSelection The time interval selected in the notification tray menu.
     * @return A sanitized polling rate integer.
     */
    private int sanitizePollingRate(String menuSelection) {
        String timeStr = menuSelection.substring(0, menuSelection.indexOf("s"));
        return Integer.parseInt(timeStr);
    }
    
    /**
     * Gets the current TrayIcon.
     * 
     * @return current TrayIcon
     */
    protected TrayIcon getTrayIcon() {
        return trayIcon;
    }
    
    /**
     * Sets the image of the current TrayIcon.  If no TrayIcon exists, a new
     * object is instantiated with img.  Otherwise, set the image of the current
     * TrayIcon.
     * 
     * @param img the Image for the TrayIcon.
     */
    protected void setTrayIcon(Image img) {
        if(trayIcon == null) {
            trayIcon = new TrayIcon(img);
        }
        else {
            trayIcon.setImage(img);
        }
    }
}
