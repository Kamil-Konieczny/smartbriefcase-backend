package pl.bykowski.backendjwt.textExtraction;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TxtExtraction {
    public static String convertTextFileToText(MultipartFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        StringBuilder text = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            text.append(line).append(System.lineSeparator());
        }
        reader.close();

        return text.toString();
    }
}
