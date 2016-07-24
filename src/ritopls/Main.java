package ritopls;

import java.io.IOException;

/**
 * This class instantiates a GUI object.
 * 
 * @author Chris Meyers
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        GUI gui = new GUI();
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
    }  
    
}
