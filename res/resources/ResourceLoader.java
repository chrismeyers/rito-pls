package resources;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

/**
 *
 * @author Chris Meyers
 */
final public class ResourceLoader {
    static ResourceLoader rl = new ResourceLoader();
    
    public static Image getImage(String fileName) {
        return Toolkit.getDefaultToolkit().getImage(rl.getClass().getResource("images/" + fileName));
    }

    public static File getFile(String fileName) {
        return new File(rl.getClass().getResource("file/" + fileName).getPath());
    }
}
