import javax.swing.SwingUtilities;
import java.io.File;

public class App {
    public static void main(String[] args) {
        // Parser parser = new Parser(new File("src/production_simple.txt"));
        // Parser parser = new Parser(new File("src/production_rules.txt"));
        Parser parser = new Parser(new File("src/yt_productions.txt"));
        // parser.printProductionRules();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GUI();
            }
        });
    }
    
}
