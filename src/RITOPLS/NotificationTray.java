package RITOPLS;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
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
    
    /**
     * NotificationTray constructor.
     * 
     * @param g The current GUI instance.
     * @throws IOException 
     */
    public NotificationTray(GUI g) throws IOException {
        gui = g;
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
        gui.setTrayIcon(gui.getIconImage());

        // Create popup menu components
        gui.setInfoTrayMenuItem();
        gui.setPingTrayMenuItem();
        gui.setTogglePollingTrayMenuItem();
        setVariableMenuItems();
        gui.getInfoTrayMenuItem().setEnabled(false);
        gui.getPingTrayMenuItem().setEnabled(false);
        MenuItem about = new MenuItem(StaticData.MENU_ABOUT);
        Menu setRegion = new Menu(StaticData.MENU_SET_REGION);
        setupRegionTrayMenu(setRegion);
        Menu setPolling = new Menu(StaticData.MENU_POLLING);
        setupPollingRateTrayMenu(setPolling);
        MenuItem maximize = new MenuItem(StaticData.MENU_MAXIMIZE);
        MenuItem quit = new MenuItem(StaticData.MENU_EXIT);
  
        //Add components to popup menu
        popup.add(gui.getInfoTrayMenuItem());
        popup.add(gui.getPingTrayMenuItem());
        popup.add(about);
        popup.addSeparator(); //=============
        popup.add(gui.getTogglePollingTrayMenuItem());
        popup.addSeparator(); //=============
        popup.add(setRegion);
        popup.add(setPolling);
        popup.addSeparator(); //=============
        popup.add(maximize);
        popup.add(quit);
         
        gui.getTrayIcon().setPopupMenu(popup);
        gui.getTrayIcon().setImageAutoSize(true);
        
        try {
            gui.getSystemTray().add(gui.getTrayIcon());
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
       
        gui.getTrayIcon().addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVariableMenuItems();
                
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
               
        gui.getTogglePollingTrayMenuItem().addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                gui.getJToggleButton(1).doClick();
                setVariableMenuItems();
            }        
        });
        
        setPolling.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                setVariableMenuItems();
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
        gui.getSystemTray().remove(gui.getTrayIcon());
    }
    
    /**
     * Populates the system tray region menu.
     * 
     * @param setRegion The tray menu that contains the regions.
     */
    private void setupRegionTrayMenu(final Menu setRegion) {
        String[] regions = gui.getStaticData().getRegions();
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
                    setVariableMenuItems();
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
        String[] rates = gui.getStaticData().getPollingRatesArr();
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
                    setVariableMenuItems();
                }        
            });
        }
    }
    
    /**
     * Populates tray menu items that periodically change.
     */
    protected void setVariableMenuItems() {
        if(gui.getJToggleButton(1).isSelected()) {
            gui.getInfoTrayMenuItem().setLabel("[" + gui.getCurrentRegion().toUpperCase() + "] :: " + "Refreshing every " + gui.getPollingRate() + "s");
            gui.getPingTrayMenuItem().setLabel(gui.getCurrentRegion().toUpperCase() + " ping is " + gui.getParser().getPing());
            gui.getTogglePollingTrayMenuItem().setLabel(StaticData.MENU_POLLING_OFF);
        }
        else {
            gui.getInfoTrayMenuItem().setLabel("[" + gui.getCurrentRegion().toUpperCase() + "] :: " + "Not currently polling");
            gui.getPingTrayMenuItem().setLabel(gui.getCurrentRegion().toUpperCase() + " ping is " + "Not Available");
            gui.getTogglePollingTrayMenuItem().setLabel(StaticData.MENU_POLLING_ON);
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
}
