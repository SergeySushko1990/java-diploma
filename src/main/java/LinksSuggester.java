import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinksSuggester {

    private List<Suggest> suggestList = new ArrayList<>();

    public List<Suggest> getSuggestList() {
        return suggestList;
    }

    public LinksSuggester(File file) throws IOException, WrongLinksFormatException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        StringBuilder text = new StringBuilder();
        while (line != null) {
            text.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        suggestList = suggest(text.toString());

    }

    public List<Suggest> suggest(String text) {

        String[] lines = text.split("\n");
        List<Suggest> suggestList = new ArrayList<>();

        for (String line : lines) {
            String[] t = line.split("\t");
            if (t.length != 3) {
                throw new WrongLinksFormatException("Wrong number of arguments " + line);
            }
            suggestList.add(new Suggest(t[0], t[1], t[2]));
        }
        return suggestList;
    }
}
