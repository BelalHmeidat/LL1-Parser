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

    private Map<String, Set<String>> firstMap;
    private Map<String, Set<String>> followMap;

    private Map<String, Map<String, List<String>>> parsingTable;

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
        this.startSymbol = findFirstSymbol();
        this.firstMap = new HashMap<>();
        this.followMap = new HashMap<>();
        for (String key : nonTerminals) {
            findFirst(key);
            findFollow(key);
            // Set<String> follows = findFollow(key);
            // for (String s : follows) {
            //     System.out.println("Key: " + key + " Follow: " + s);
            //     System.out.println("-----------");
            // }
            // Set<String> firsts = findFirst(key);
            // for (String s : firsts) {
            //     System.out.println("Key: " + key + " First: " + s);
            // }
        }
        this.parsingTable = createParsingTable();
        printParsingTable();

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
    
    private Set<String> findFirstOfProduction(String productioString){
        Set<String> first = new HashSet<>();
        String [] parts = productioString.split(" ");
        for (String part : parts) {
            if (isTerminal(part)) {
                first.add(part);
                break;
            }
            else if (isNonTerminal(part)) {
                Set<String> tempFirst = findFirst(part);
                if (tempFirst.contains("lambda")) {
                    tempFirst.remove("lambda");
                    first.addAll(tempFirst);
                }
                else {
                    first.addAll(tempFirst);
                    break;
                }
            }
        }
        return first;
    }

    private Set<String> findFirst(String keyOrTerminal){
        if (isTerminal(keyOrTerminal)) {
            return new HashSet<>(Arrays.asList(keyOrTerminal));
        }
        //is non terminal
        Set<String> first = new HashSet<>();
        List<String> productionRuleValue = productionRules.get(keyOrTerminal);
        for (String option : productionRuleValue) {
            String [] parts = option.split(" ");
            if(isTerminal(parts[0])){
                first.add(parts[0]);
            }
            else {
                int i =0;
                while(i < parts.length){
                    Set<String> tempFirst = findFirst(parts[i]);
                    if (tempFirst.contains("lambda")) {
                        tempFirst.remove("lambda");
                        first.addAll(tempFirst);
                        if(parts.length > i+1){
                            Set<String> tempFirstNext = findFirst(parts[i+1]);
                            if (tempFirstNext.contains("lambda"))
                            tempFirstNext.remove("lambda");
                            first.addAll(tempFirstNext);
                        }
                    }
                    else {
                        first.addAll(tempFirst);
                        break;
                    }
                    i++;
                }
            }
        }
        if (first.isEmpty()){
            throw new IllegalArgumentException("First set is empty!");
        }
        firstMap.put(keyOrTerminal, first);
        return first;
    }

    private Set<String> savedProductions = new HashSet<>(); // a set to keep track of past productions to avoid infinite recursion

    private boolean isInSavedProductions(String production) {
        return savedProductions.contains(production);
    }

    private Set<String> findFollow(String targetKey){
        Set<String> follow = new HashSet<>();
        if (targetKey.trim().equals(startSymbol.trim())) {
            followMap.put(targetKey, new HashSet<>(Arrays.asList("$")));
            return new HashSet<>(Arrays.asList("$"));
        }
        for (String key : productionRules.keySet()){
            List<String> productionRuleValue = productionRules.get(key);
            for (String option : productionRuleValue) {
                String [] parts = option.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals(targetKey)){
                        if (i == parts.length - 1) { //if target key is the last symbol in the production, find the follow of the left hand side of the production
                            // Infinite recursion avoidance
                            if (isInSavedProductions(key + " -> " + option)) { // if the production is already saved check if the target follows were already found before and add them
                                if (followMap.keySet().contains(key))
                                    follow.addAll(followMap.get(key));
                            }
                            else if (!key.equals(targetKey)) { // if the production is not saved, find the follow of the left hand side of the production
                                savedProductions.add(key + " -> " + option);
                                follow.addAll(findFollow(key));
                            }
                        }
                        else if (isTerminal(parts[i+1])){
                            follow.add(parts[i+1]);
                        }
                        else if (isNonTerminal(parts[i+1])){
                            Set<String> tempFollow = findFirst(parts[i+1]);
                            if (tempFollow.contains("lambda")) {
                                tempFollow.remove("lambda");
                                follow.addAll(tempFollow);
                                // follow.addAll(findFollow(key));
                                follow.addAll(findFollow(parts[i+1]));
                            }
                            else {
                                follow.addAll(tempFollow);
                            }
                        }
                        if (Arrays.asList(findFirst(key)).contains("lambda")) {
                            follow.addAll(findFollow(key));
                        }
                    }
                }
            }
        }
        followMap.put(targetKey, follow);
        return follow;
    }

    private Set<String> extractNonTerminals(){
        if (productionRules == null) {
            throw new IllegalArgumentException("Production rules not initialized!");
        }
        if (productionRules.isEmpty()) {
            throw new IllegalArgumentException("Production rules are empty!");
        }
        if (productionRules.keySet().isEmpty()) {
            throw new IllegalArgumentException("Production rules keys are empty!");
        }
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
        if (terminalSetList.isEmpty()) {
            throw new IllegalArgumentException("No terminals found!");
        }
        Set<String> terminalSet = new HashSet<>(terminalSetList);
        return terminalSet;
    }

    private String findFirstSymbol(){
        ArrayList<String> productionValues = new ArrayList<>();
        for (List<String> values : productionRules.values()) {
            for (String value : values) {
                productionValues.addAll(Arrays.asList(value.split(" ")));
            }
        }
        for (String key : productionRules.keySet()) {
            if (!productionValues.contains(key)) {
                return key;
            }
        }
        throw new IllegalArgumentException("No start symbol found!");
    }

    private boolean isTerminal(String item){
        return terminals.contains(item);
    }

    private boolean isNonTerminal(String item){
        return nonTerminals.contains(item);
    }

    private Map<String, Map<String, List<String>>> createParsingTable() {
        Map<String, Map<String,  List<String>>> parsingTable = new HashMap<>();
        for (String key : nonTerminals) {
            Set<String> first = findFirst(key);
            Set<String> follow = findFollow(key);
            List<String> productions = productionRules.get(key);
            boolean containsLambda = productions.contains("lambda");
            Map<String, List<String>> innerMap = new HashMap<>();
            Set<String> terminalsWith$ = new HashSet<>(terminals);
            terminalsWith$.add("$");
            for (String production: productions){
                if (production.equals("lambda")){
                    continue;
                }
                Set<String> firstOfProduction = findFirstOfProduction(production);
                firstOfProduction.remove("lambda");
                for (String terminal : firstOfProduction){
                    if (innerMap.containsKey(terminal)){
                        innerMap.get(terminal).add(production);
                    }
                    else {
                        innerMap.put(terminal, new ArrayList<>(Arrays.asList(production)));
                    }
                }
            }
            // for (String terminal : terminalsWith$) {
            //     if (containsLambda && follow.contains(terminal)) {
            //         innerMap.put(terminal, new ArrayList<>(Arrays.asList("lambda")));
            //     }
            // }
            parsingTable.put(key, innerMap);
        }
        return parsingTable;
    }

    private void printParsingTable() {
        for (String key : parsingTable.keySet()) {
            System.out.println("Key: " + key);
            Map<String, List<String>> innerMap = parsingTable.get(key);
            for (String innerKey : innerMap.keySet()) {
                System.out.print(innerKey + ": ");
                List<String> innerList = innerMap.get(innerKey);
                for (String innerValue : innerList) {
                    System.out.print(key + " -> " + innerValue + ", ");
                }
            }
            System.out.println();
        }
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
