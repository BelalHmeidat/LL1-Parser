import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Parser {
    private Map<String, List<String>> productionRules;
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;

    public Parser(){}

    /***
     * Constructor from map
     * @param productionRules
     */
    public Parser(Map<String, List<String>> productionRules) {
        this.productionRules = productionRules;
        initialize();
    }

    private void initialize() {
        this.nonTerminals = extractNonTerminals();
        this.terminals = extractTerminals();
        this.startSymbol = this.printProductionRules().split("->")[0].trim();
    }

    /***
     * Constructor from file
     * @param productionRulesFile
     */
    public Parser(File productionRulesFile){
        Scanner scanner = null;
        this.productionRules = new HashMap<>();
        try {
            scanner = new Scanner(productionRulesFile);
            while (scanner.hasNextLine()) { 
                String line = scanner.nextLine();
                String[] parts = line.split("->");
                String key = parts[0].trim();
                String[] values = parts[1].split("\\|");
                List<String> valueList = new ArrayList<>();
                for (String value : values) {
                    valueList.add(value.trim());
                }
                productionRules.put(key, valueList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
            initialize();
            // System.out.println("Non Terminals: " + nonTerminals);
            // System.out.println("Terminals: " + terminals);
            // findFirst("S");
        }
    }

    public boolean checkInteger(String input) {
        return input.matches("\\d+");
    }

    public boolean checkName(String input) {
        return input.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    public boolean checkDouble(String input) {
        return input.matches("\\d+\\.\\d+");
    }

    public String printProductionRules(){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : productionRules.entrySet()) {
            sb.append(entry.getKey()).append(" -> ");
            for (String value : entry.getValue()) {
                sb.append(value).append(" | ");
            }
            sb.delete(sb.length()-3, sb.length()); // deleting the last " | "
            sb.append("\n");
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    private String [] findFirst(String keyOrTerminal){
        if (isTerminal(keyOrTerminal)) {
            return new String[]{keyOrTerminal};
        }
        ArrayList<String> first = new ArrayList<>();
        List<String> productionRuleValue = productionRules.get(keyOrTerminal);
        for (String option : productionRuleValue) {
            String [] parts = option.split(" ");
            if(isTerminal(parts[0])){
                first.add(parts[0]);
            }
            else if(isNonTerminal(parts[0])){
                first = new ArrayList<>(Arrays.asList(findFirst(parts[0])));
                if (first.contains("lambda")) {
                    first.remove("lambda");
                    first.addAll(Arrays.asList(findFirst(parts[1])));
                }
            }
        }
        return first.toArray(new String[0]);
    }

    // private String [] findFollow(String targetKey){
    //     if (targetKey.equals(startSymbol)) {
    //         return new String[]{"$"};
    //     }
    //     for (String key : productionRules.keySet()){
    //         List<String> productionRuleValue = productionRules.get(key);
    //         for (String option : productionRuleValue) {
    //             String [] parts = option.split(" ");
    //             for (int i = 0; i < parts.length; i++) {
    //                 if (parts[i].equals(targetKey)){
    //                     if (i == parts.length - 1) {
    //                         return findFollow(key);
    //                     }
    //                     else {
    //                         if (isTerminal(parts[i+1])){

    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }

    private Set<String> extractNonTerminals(){
        return productionRules.keySet();
    }

    private Set<String> extractTerminals(){
        List<String> terminalSetList = new ArrayList<>();
        for (List<String> values : productionRules.values()) {
            for (String value : values) {
                String [] parts = value.split(" ");
                for (String part : parts) {
                    if (!extractNonTerminals().contains(part)) {
                        terminalSetList.add(part);
                    }
                }
            }
        }
        Set<String> terminalSet = new HashSet<>(terminalSetList);
        return terminalSet;
    }

    private boolean isTerminal(String item){
        return terminals.contains(item);
    }

    private boolean isNonTerminal(String item){
        return nonTerminals.contains(item);
    }

    private String[] readFile(File file) {
        ArrayList<String> content = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                content.add(scanner.nextLine());
            }
            scanner.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return content.toArray(new String[0]);
    }
}
