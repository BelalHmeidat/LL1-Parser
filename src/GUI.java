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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GUI extends JFrame {

    private Parser parser;
    

    public GUI(Parser parser) {
        this.parser = parser;
        //file panel
        JPanel filePanel = new JPanel();
        filePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        JTextField filePathField = new JTextField();
        filePathField.setPreferredSize(new Dimension(300, 24));
        filePathField.setMaximumSize(filePathField.getPreferredSize());
        JButton browseButton = new JButton("Browse");
        filePanel.add(filePathField);
        filePanel.add(browseButton);

        //File preview panel
        JPanel fileViewPanel = new JPanel(new GridBagLayout());
        // TextArea fileView = new TextArea("File Content will be viewed here",10, 70);
        JTextPane fileView = new JTextPane();
        StyledDocument doc = fileView.getStyledDocument();
        fileView.setEditable(false);
        fileView.setPreferredSize(new Dimension(400, 300));
        JScrollPane scrollPane = new JScrollPane(fileView);
        scrollPane.setMinimumSize(new Dimension(400, 200));
        fileViewPanel.add(scrollPane);
        // fileViewPanel.add(fileView);


        //status panel
        JPanel statusPanel = new JPanel(new GridBagLayout()); //flexible layout to fill available space

        //root panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(filePanel);
        mainPanel.add(fileViewPanel);
        mainPanel.add(statusPanel);

        //browse button action
        browseButton.addActionListener(e -> {
            //Choosing File
            JFileChooser fileChooser = new JFileChooser();
            // Start at current directory
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            //Accepting file type of .txt and pascal only
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text or Pascal files only", "txt", "pas"));
            int result = fileChooser.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                statusPanel.removeAll();
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
                String [] fileContent = readFile(selectedFile);
                String processedContent = FileProcessor.processFileContent(fileContent);
                for (int i =0 ; i < fileContent.length; i++){
                    fileContent[i] = (i + 1) + " " + fileContent[i];
                }
                if (String.join("\n", fileContent).trim().isEmpty()){
                    fileView.setText("File is empty!");
                    return;
                }
                fileView.setText(String.join("\n", fileContent));

                //Validating through Parser
                String output = validate(processedContent);
                if (output == null){ //Code is valid
                    JLabel label = new JLabel("Code is valid!");
                    label.setForeground(Color.getHSBColor(0.3f, 0.8f, 0.6f));
                    label.setFont(new Font("Arial", Font.BOLD, 20)); // Change text size
                    statusPanel.add(label);
                }
                else { //Code is invalid
                    fileView.setText("");
                    for (int i =0 ; i < fileContent.length; i++){
                        Style style = null;
                        if(parser.lineNo == i+1){
                            style = fileView.addStyle(fileContent[i], null);
                            StyleConstants.setForeground(style, Color.RED);
                        }
                        try{
                            doc.insertString(doc.getLength(), fileContent[i] + "\n",style);
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    TextArea textArea = new TextArea(output, 10, 70);
                    textArea.setEditable(false);
                    statusPanel.add(textArea);
                    textArea.setForeground(Color.RED);
                }
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        this.setTitle("Belal's Parser");
        this.setContentPane(mainPanel);
        this.setSize(600, 500);
        this.setVisible(true);
        this.setResizable(false);


    }


    private String validate(String code) {
        // System.out.println(code);
        String result = parser.validateCode(code);
        return result;
    }

    private String [] readFile(File file) {
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
        return lines.toArray(new String[0]);
    }

}
