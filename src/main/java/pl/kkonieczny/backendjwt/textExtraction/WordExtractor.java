package pl.kkonieczny.backendjwt.textExtraction;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class WordExtractor {
    public static String extractTextFromWord(MultipartFile file) throws IOException {
        XWPFDocument doc = new XWPFDocument(file.getInputStream());
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        doc.close();

        return text;
    }

}
