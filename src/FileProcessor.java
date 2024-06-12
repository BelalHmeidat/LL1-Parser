import java.util.ArrayList;
import java.util.List;

public class FileProcessor {

    static private char [] spaceOptionalTokens = {'+', '-', '*', '/', '(', ')', ';', '.', ','};
    static private char [] uncertainChars = {'<', '>', ':', '|'};

    static String processFileContent(String [] lines){
        ArrayList<String> processedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                processedLines.add("!ln");
                continue;
            }
            String processedLine = addSpacesAroundTokens(line.trim());
            processedLine = processedLine + " !ln";
            processedLine = removeExtraSpaces(processedLine);
            processedLines.add(processedLine);
        }
        return String.join(" ", processedLines);
    }

    static public String addSpacesAroundTokens(String line){
        StringBuilder sb = new StringBuilder(line);
        int i = 0;
        while (i < sb.length()){
            if (checkEqualAt(i, sb.toString())){
                if(checkUncertainAt(i - 1, sb.toString())){
                    if (!checkSpaceAt(i-2, sb.toString())){
                        sb = insertSpaceAt(i-1, sb.toString());
                        i++;
                    }
                }
                else if(!checkSpaceAt(i-1, sb.toString())){
                    sb = insertSpaceAt(i, sb.toString());
                    i++;
                }
                if(!checkSpaceAt(i+1, sb.toString())){
                    sb = insertSpaceAt(i+1, sb.toString());
                    i++;
                }
            }
            else {
                boolean didBreak = false;
                for (char c : uncertainChars){
                    if(sb.charAt(i) == c){
                        if (!checkSpaceAt(i-1, sb.toString())){
                            sb = insertSpaceAt(i, sb.toString());
                            i++;
                        }
                        if (checkEqualAt(i+1, sb.toString())){
                            if (!checkSpaceAt(i+2, sb.toString())){
                                sb = insertSpaceAt(i+2, sb.toString());
                                i++;
                            }
                        }
                        else if (!checkSpaceAt(i+1, sb.toString())){
                            sb = insertSpaceAt(i+1, sb.toString());
                            i++;
                        }
                        didBreak = true;
                        break;
                    }
                }
                if(!didBreak){
                    for (char c : spaceOptionalTokens){
                        if(sb.charAt(i) == c){
                            if(sb.charAt(i) == '.' && checkNumberAt(i-1, sb.toString()) && checkNumberAt(i+1, sb.toString())){
                                break;
                            }
                            if (!checkSpaceAt(i-1, sb.toString())){
                                sb = insertSpaceAt(i, sb.toString());
                                i++;
                            }
                            if (!checkSpaceAt(i+1, sb.toString())){
                                sb = insertSpaceAt(i+1, sb.toString());
                                i++;
                            }
                            break;
                        }
                    }
                }
            }
            i++;
        }
        return sb.toString();
    }

    static private boolean checkNumberAt(int index, String line){
        if (index < 0 || index >= line.length()){
            return false;
        }
        return Character.isDigit(line.charAt(index));
    }

    static private boolean checkUncertainAt(int index, String line){
        if (index < 0 || index >= line.length()){
            return false;
        }
        for (char c : uncertainChars){
            if (line.charAt(index) == c){
                return true;
            }
        }
        return false;
    }

    static private boolean checkEqualAt(int index, String line){
        if (index < 0 || index >= line.length()){
            return false;
        }
        return line.charAt(index) == '=';
    
    }

    static private boolean checkSpaceAt(int index, String line){
        if (index < 0 || index >= line.length()){
            return false;
        }
        return line.charAt(index) == ' ';
    }


    static private StringBuilder insertSpaceAt(int index, String line){
        StringBuilder sb = new StringBuilder(line);
        sb.insert(index, ' ');
        return sb;
    }

    static public String removeExtraSpaces(String content){
        StringBuilder sb = new StringBuilder(content);
        int i = 0;
        while (i < sb.length()){
            if (checkSpaceAt(i, sb.toString())){
                int j = i + 1;
                while (j < sb.length() && checkSpaceAt(j, sb.toString())){
                    sb.deleteCharAt(j);
                }
            }
            i++;
        }
        return sb.toString().trim();
    }
    
}
