package pl.bykowski.backendjwt.textExtraction;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class PDFExtractor {
    public static String convertPdfToText(MultipartFile file) throws IOException {
        PdfReader reader = new PdfReader(file.getInputStream());
        PdfDocument pdfDoc = new PdfDocument(reader);
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
        }
        pdfDoc.close();

        return text.toString();
    }
}
