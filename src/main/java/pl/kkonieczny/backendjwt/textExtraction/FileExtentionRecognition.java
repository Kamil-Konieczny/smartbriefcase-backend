package pl.kkonieczny.backendjwt.textExtraction;

import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileExtentionRecognition {

    public static String getExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        int lastDotIndex = originalFileName.lastIndexOf(".");
        return originalFileName.substring(lastDotIndex + 1);
    }
    public static String extractText(MultipartFile file) throws TesseractException, IOException {
        String extension = getExtension(file);
        String extractedText = "";
        if(extension.equals("TIFF") || extension.equals("tiff") ||
           extension.equals("JPEG") || extension.equals("jpeg") ||
                extension.equals("JPG") || extension.equals("jpg") ||
                extension.equals("PNG")  || extension.equals("png") ||
           extension.equals("BMP")  || extension.equals("bmp") ||
           extension.equals("PBM")  || extension.equals("pbm") ||
           extension.equals("PGM")  || extension.equals("pgm") ||
           extension.equals("PPM")  || extension.equals("ppm") ||
           extension.equals("PNM")  || extension.equals("pnm")){
            System.out.println("1"+extension);
           extractedText = OCR.extractText(file);
        }
        else if (extension.equals("DOCX") || extension.equals("docx")){
            System.out.println(extension);
           extractedText = WordExtractor.extractTextFromWord(file);
        }
        else if (extension.equals("PDF") || extension.equals("pdf")){
            System.out.println(extension);
            extractedText = PDFExtractor.convertPdfToText(file);
        }
        else if (extension.equals("TXT") || extension.equals("txt")){
            System.out.println(extension);
            extractedText = TxtExtraction.convertTextFileToText(file);
        }
        return extractedText;
    }
}
