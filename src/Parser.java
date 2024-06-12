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
import java.util.Stack;

public class Parser {
    private Map<String, List<String>> productionRules;
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;
    private Map<String, Set<String>> followMap;
    private boolean isLL1;
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
        System.out.println(findFirst("declarations"));
        System.out.println(" -------");
        this.isLL1 = checkLL1();
        System.out.println(checkLL1());
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

    private boolean checkInteger(String input) {
        return input.matches("\\d+");
    }

    private boolean checkName(String input) {
        return input.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private boolean checkDouble(String input) {
        return input.matches("\\d+\\.\\d+");
    }

    private String printProductionRules(){
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
    
    // private Set<String> findFirstOfProduction(String productionString){
    //     Set<String> first = new HashSet<>();
    //     String [] parts = productionString.split(" ");
    //     for (String part : parts) {
    //         if (isTerminal(part)) {
    //             first.add(part);
    //             break;
    //         }
    //         else if (isNonTerminal(part)) {
    //             Set<String> tempFirst = findFirst(part);
    //             if (tempFirst.contains("lambda")) {
    //                 tempFirst.remove("lambda");
    //                 first.addAll(tempFirst);
    //             }
    //             else {
    //                 first.addAll(tempFirst);
    //                 break;
    //             }
    //         }
    //     }
    //     return first;
    // }

    private Set<String> findFirst(String productionString){
        Set<String> first = findFirstHelper(productionString);
        if (isNonTerminal(productionString)){
            List<String> productions = productionRules.get(productionString);
            if(productions.contains("lambda")){
                first.add("lambda");
            }
        }
        if (first.isEmpty()){
            throw new IllegalArgumentException("First set is empty!");
        }
        return first;
    }
    
    private Set<String> findFirstHelper(String productionString){
        Set<String> first = new HashSet<>();
        String [] parts = productionString.split(" ");
        boolean containsLambda = false;
        for (int i = 0; i < parts.length; i++){ 
            String part = parts[i];
            if (isTerminal(part)) {
                first.add(part);
                break;
            }
            else if (isNonTerminal(part)) {
                List<String> productions = productionRules.get(part);
                for (String production : productions) {
                    Set<String> tempFirst = findFirstHelper(production);
                    if (tempFirst.contains("lambda")) {
                        if (i != parts.length - 1) { // if it is the last symbol in the production, do not remove lambda
                            tempFirst.remove("lambda");
                        }
                        containsLambda = true;
                    }
                    first.addAll(tempFirst);
                }
                if(!containsLambda){
                    break;
                }
            }
        }
        return first;
    }

    // private Set<String> findFirst(String keyOrTerminal){
    //     if (isTerminal(keyOrTerminal)) {
    //         return new HashSet<>(Arrays.asList(keyOrTerminal));
    //     }
    //     //is non terminal
    //     Set<String> first = new HashSet<>();
    //     List<String> productionRuleValue = productionRules.get(keyOrTerminal);
    //     for (String option : productionRuleValue) {
    //         String [] parts = option.split(" ");
    //         if(isTerminal(parts[0])){
    //             first.add(parts[0]);
    //         }
    //         else {
    //             int i =0;
    //             while(i < parts.length){
    //                 Set<String> tempFirst = findFirst(parts[i]);
    //                 if (tempFirst.contains("lambda")) {
    //                     tempFirst.remove("lambda");
    //                     first.addAll(tempFirst);
    //                     if(parts.length > i+1){
    //                         Set<String> tempFirstNext = findFirst(parts[i+1]);
    //                         if (tempFirstNext.contains("lambda"))
    //                         tempFirstNext.remove("lambda");
    //                         first.addAll(tempFirstNext);
    //                     }
    //                 }
    //                 else {
    //                     first.addAll(tempFirst);
    //                     break;
    //                 }
    //                 i++;
    //             }
    //         }
    //     }
    //     if (first.isEmpty()){
    //         throw new IllegalArgumentException("First set is empty!");
    //     }
    //     firstMap.put(keyOrTerminal, first);
    //     return first;
    // }

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

    private boolean checkLL1(){
        for (String key: nonTerminals){
            Set<String> follow = null;
            if (productionRules.get(key).contains("lambda")){
               follow = findFollow(key);
            }
            for (int i =0; i < productionRules.get(key).size(); i++){
                String production = productionRules.get(key).get(i);
                if (production.equals("lambda")){
                    continue;
                }
                Set<String> firstOfProduction = findFirst(production);
                for (int j = 0; j < productionRules.get(key).size(); j++){
                    if (i == j){
                        continue;
                    }
                    String otherProduction = productionRules.get(key).get(j);
                    if (otherProduction.equals("lambda")){
                        continue;
                    }
                    Set<String> firstOfOtherProduction = findFirst(otherProduction);
                    Set<String> intersection = new HashSet<>(firstOfProduction);
                    intersection.retainAll(firstOfOtherProduction);
                    if (!intersection.isEmpty()){
                        System.out.println("Key: " + key + " Production: " + production + " Other Production: " + otherProduction);
                        return false;
                    }
                }
                if (follow != null){ //means that lambda is in the production
                    for (String terminal : firstOfProduction){
                        if (follow.contains(terminal)){
                            System.out.println("Key: " + key + " Production: " + production + " Terminal: " + terminal);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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
                Set<String> firstOfProduction = findFirst(production);
                firstOfProduction.remove("lambda");
                for (String terminal : firstOfProduction){
                    if (innerMap.containsKey(terminal)){
                        innerMap.get(terminal).add(production);
                        if (isLL1){
                            throw new IllegalArgumentException("Conflict in parsing table! It should be LL1! but it found 2 productions for the same terminal!");
                        }
                    }
                    else {
                        innerMap.put(terminal, new ArrayList<>(Arrays.asList(production)));
                    }
                }
            }
            for (String terminal : terminalsWith$) {
                if (containsLambda && follow.contains(terminal)) {
                    if (innerMap.containsKey(terminal)){
                        innerMap.get(terminal).add("lambda");
                        if (isLL1){
                            throw new IllegalArgumentException("Conflict in parsing table! It should be LL1! but it found 2 productions for the same terminal!");
                        }
                    }
                    else {
                        innerMap.put(terminal, new ArrayList<>(Arrays.asList("lambda")));
                    }
                }
            }
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

    public int lineNo = 1;

    //returns first token from the list of tokes and increments the line number if new line found
    private String getToken(List<String> tokens){
        while (!tokens.isEmpty()){
            if (tokens.get(0).equals("!ln")){
                lineNo++;
                tokens.remove(0);
            } else {
                return tokens.get(0);
            }
        }
        return "End of code!";
    }

    public String validateCode(String code){
        lineNo = 1;
        System.out.println("Code: " + code);
        List<String> tokens = new ArrayList<>(Arrays.asList(code.split(" ")));
        // for (String token : tokens){
        //     if (!isTerminal(token) && !isNonTerminal(token)){
        //         throw new IllegalArgumentException("Token: " + token + " is not a terminal or non terminal!");
        //     }
        // }
        Stack<String> parsingStack = new Stack<>();
        parsingStack.push("$");
        parsingStack.push(startSymbol);
        System.out.println(parsingStack);
        while (!parsingStack.empty()){
            System.out.println(parsingStack);
            String top = parsingStack.pop();
            System.out.println("Top: " + top);
            System.out.println(getToken(tokens));
            if (isTerminal(top)){
                if (top.equals("$")){
                    if (!tokens.isEmpty()){
                        return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid because parsing stack is empty!"; //TODO: check this correct?
                    }
                    else {
                        return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected end of code but found: " + getToken(tokens) + " left!";
                    }
                }
                if (tokens.isEmpty()){
                    return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected end of code but found: '$' missing!";
                }
                String token = getToken(tokens);
                if (top.equals(token)){
                    tokens.remove(0);
                }
                else if (top.equals("name")){
                    if (checkName(token)){
                        tokens.remove(0);
                    }
                    else {
                        return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected a name but found: \"" + token + "\"" + " which is not a valid name!";
                    }
                }
                else if (top.equals("integer-value")){
                    if (checkInteger(token)){
                        tokens.remove(0);
                    }
                    else {
                        return "Error at line " + lineNo +": Unexpected Token: \"" +getToken(tokens) + "\"\nCode is invalid! Expected integer but found: \"" + token + "\"!";
                    }
                }
                else if (top.equals("real-value")){
                    if (checkDouble(token)){
                        tokens.remove(0);
                    }
                    else {
                        return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected double but found: \"" + token + "\"!";
                    }
                }
                else {
                    return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected: \"" + top + "\" but found: \"" + token + "\"!";
                }
            }
            else if (isNonTerminal(top)){
                if (!parsingTable.containsKey(top)){
                    return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! \"" + top + "\" is not a valid non terminal!";
                }
                if (parsingTable.get(top).keySet().contains(getToken(tokens)) == false){
                    if (replaceVar(tokens, parsingTable.get(top).keySet())){
                        System.out.println("Replaced variable!" + " " + getToken(tokens));
                    }
                    else return "Error at line " + lineNo +": Unexpected Token: \"" + getToken(tokens) + "\"\nCode is invalid! Expected instance of: \"" + top + "\" but found: \"" + getToken(tokens) + "\"!";
                }
                List<String> productions = parsingTable.get(top).get(getToken(tokens));
                if (productions.size() > 1){
                    throw new IllegalArgumentException("Conflict in parsing table! It should be LL1! but it found 2 productions for the same terminal!");
                }
                List<String> production = Arrays.asList(productions.get(0).split(" ")); // LL1 has only one production
                for (int i = production.size() - 1; i >= 0; i--){
                    if (!production.get(i).equals("lambda")){
                        parsingStack.push(production.get(i));
                    }
                }
            }
        }
        return null;
    }
    //Replaces an int value with a var terminal known to the parsing table
    // returns false if non of the keys have production rule that contains variable 
    private boolean replaceVar(List<String> tokens, Set<String> productionKeys){
        String var = tokens.get(0);
        if (productionKeys.contains("name") && checkName(var)){
            tokens.remove(0);
            tokens.addFirst("name");
            return true;
        }
        else if (productionKeys.contains("integer-value") && checkInteger(var)){
            tokens.remove(0);
            tokens.addFirst("integer-value");
            return true;
        }
        else if (productionKeys.contains("real-value") && checkDouble(var)){
            tokens.remove(0);
            tokens.addFirst("real-value");
            return true;
        }
        return false;
    }

}
