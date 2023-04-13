package pl.bykowski.backendjwt.document;

import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.exception.TikaException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import pl.bykowski.backendjwt.filsServices.FileStorageActions;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@RestController
public class DocumentApi {

    private final DocumentService documentService;
    @Autowired
    private FileStorageActions fileStorageActions;
    public DocumentApi(DocumentService documentService) {
        this.documentService = documentService;
    }


    @GetMapping("/documents")
    public List<Document> getDoc(@RequestParam String email){
        System.out.println("email from backend: " + email);
        System.out.println("data: "+ documentService.getDocumentsbyUserEmail(email));
        return documentService.getDocumentsbyUserEmail(email);
    }

    @PostMapping("/document")
    public ResponseEntity<String> uploadDocument(@RequestParam("email") String email, @RequestParam("file") MultipartFile file) throws IOException, URISyntaxException, InvalidFormatException, Docx4JException, TesseractException {
            fileStorageActions.saveFile(file, email);
            return ResponseEntity.ok().body("File uploaded successfully: " + file.getOriginalFilename());
    }
}
