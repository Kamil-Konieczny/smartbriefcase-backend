package pl.kkonieczny.backendjwt.textExtraction;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class OCR {
        static String extractText(MultipartFile file) throws IOException, TesseractException {
            ITesseract instance = new Tesseract();
            instance.setDatapath("C:\\Users\\kamil\\Downloads");
            instance.setLanguage("eng");
            InputStream inputStream = file.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);
            String result = instance.doOCR(image);
            return result;
        }
}
