import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI extends JFrame {

    private Parser parser;
    

    public GUI(Parser parser) {
        this.parser = parser;
        //file panel
        JPanel filePanel = new JPanel();
        filePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        JTextField filePathField = new JTextField();
        filePathField.setPreferredSize(new Dimension(200, 24));
        filePathField.setMaximumSize(filePathField.getPreferredSize());
        JButton browseButton = new JButton("Browse");
        filePanel.add(filePathField);
        filePanel.add(browseButton);

        //status panel
        JPanel statusPanel = new JPanel(new GridBagLayout()); //flexible layout to fill available space

        //root panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(filePanel);
        mainPanel.add(statusPanel);

        //browse button action
        browseButton.addActionListener(e -> {
            //Choosing File
            JFileChooser fileChooser = new JFileChooser();
            // Start at current directory
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            //Accepting file type of .txt only
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text or Pascal files only", "txt", "pas"));
            int result = fileChooser.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                statusPanel.removeAll();
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
                //Validating through Parser
                String output = validate(selectedFile);
                if (output == null){
                    JLabel label = new JLabel("Code is valid!");
                    label.setForeground(Color.getHSBColor(0.3f, 0.8f, 0.6f));
                    label.setFont(new Font("Arial", Font.BOLD, 20)); // Change text size
                    statusPanel.add(label);
                }
                else {
                    TextArea textArea = new TextArea(output, 10, 50);
                    textArea.setEditable(false);
                    statusPanel.add(textArea);
                    textArea.setForeground(Color.RED);
                    //Error table
                    // DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Error", "Line"}, 0);
                      // for (String line : lines){
                        //     tableModel.addRow(new Object[]{line, "OK"});
                    // }
                    // JTable table = new JTable(tableModel);
                    // JScrollPane scrollPane = new JScrollPane(table);
                    // mainPanel.add(scrollPane);
                }
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        this.setTitle("Belal's Parser");
        this.setContentPane(mainPanel);
        this.setSize(500, 400);
        this.setVisible(true);
        this.setResizable(false);


    }


    private String validate(File file) {
        String code = readFile(file);
        // System.out.println(code);
        String result = parser.validateCode(code);
        return result;
    }

    private String readFile(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return FileProcessor.processFileContent(lines.toArray(new String[0]));
    }

}
