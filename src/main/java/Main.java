import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        // создаём конфиг
        LinksSuggester linksSuggester = new LinksSuggester(new File("data/config"));
        List<Suggest> suggestList = linksSuggester.getSuggestList();

        // перебираем пдфки в data/pdfs
        var dir = new File("data/pdfs");
        ArrayList<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(dir.listFiles())));

        // для каждой пдфки создаём новую в data/converted
        for (var file : files) {
            var doc = new PdfDocument(new PdfReader(file),
                    new PdfWriter(new File("data/converted/" + file.getName()))
            );

            // перебираем страницы pdf
            ArrayList<String> pgs = new ArrayList<>();
            for (int i = 1; i < doc.getNumberOfPages(); i++) {
                pgs.add(PdfTextExtractor.getTextFromPage(doc.getPage(i)));


                List<String> keyWords = new ArrayList<>();
                for (String page : pgs) {

                    List<String> urls = new ArrayList<>();
                    List<String> titles = new ArrayList<>();

                    for (Suggest suggest : suggestList) {
                        if (page.toLowerCase().contains(suggest.getKeyWord().toLowerCase()) &&
                                !keyWords.contains(suggest.getKeyWord())) {
                            keyWords.add(suggest.getKeyWord());
                            urls.add(suggest.getUrl());
                            titles.add(suggest.getTitle());
                        }
                    }
                    // если в странице есть неиспользованные ключевые слова, создаём новую страницу за ней
                    if (urls.size() > 0) {
                        var newPage = doc.addNewPage(page.indexOf(page) + i + 1);
                        i++;
                        var rect = new Rectangle(newPage.getPageSize()).moveRight(10).moveDown(10);
                        Canvas canvas = new Canvas(newPage, rect);
                        Paragraph paragraph = new Paragraph("Suggestions:\n");
                        paragraph.setFontSize(25);

                        // вставляем туда рекомендуемые ссылки из конфига
                        for (int j = 0; j < urls.size(); j++) {
                            PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
                            PdfAction action = PdfAction.createURI(urls.get(j));
                            annotation.setAction(action);
                            Link link = new Link(titles.get(j), annotation);
                            paragraph.add(link.setUnderline());
                            paragraph.add("\n");
                        }

                        canvas.add(paragraph);
                        canvas.close();
                    }
                }
            }
            doc.close();
        }
        System.out.println("Files converted");
    }
}

