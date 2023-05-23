package pl.kkonieczny.backendjwt.document;

import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import pl.kkonieczny.backendjwt.filsServices.FileStorageActions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
public class DocumentApi extends WebMvcConfigurerAdapter {

    private final DocumentService documentService;
    @Autowired
    private FileStorageActions fileStorageActions;
    public DocumentApi(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    public List<Document> getDoc(@RequestParam String email, @RequestParam String keyword, @RequestParam String title, @RequestParam String minDate, @RequestParam String maxDate, @RequestParam String minWordsNumber, @RequestParam String maxWordsNumber){
        if(keyword!=null && keyword!=""){
            System.out.println("z keyword");
            return documentService.getDocumentsbyUserEmail(email,keyword);
        }
        else if(title!=null && title!=""){
            System.out.println("z title");
            return documentService.getDocumentsbyUserEmail(email,null, title);
        }
        else if(minDate!=null && minDate!="" || maxDate!=null && maxDate!="") {
            System.out.println("Dates included");
            return documentService.getDocumentsbyUserEmail(email,null, null, minDate,maxDate);
        }
        else if(minWordsNumber!=null && minWordsNumber!="" || maxWordsNumber!=null && maxWordsNumber!="") {
            System.out.println("Words numbers inc");
            return  documentService.getDocumentsbyUserEmail(email, null, null, null, null, minWordsNumber, maxWordsNumber);
        }
        else {
            System.out.println("bez keyword");
            return documentService.getDocumentsbyUserEmail(email);
        }
    }
    @PostMapping("/document")
    public ResponseEntity<String> uploadDocument(@RequestParam("email") String email,
                                                 @RequestParam("file") MultipartFile file)
            throws IOException, URISyntaxException, InvalidFormatException, Docx4JException, TesseractException {
            fileStorageActions.saveFile(file, email);
            return ResponseEntity.ok().body("File uploaded successfully: " + file.getOriginalFilename());
    }

    @DeleteMapping("/remove-document")
    public ResponseEntity<String> removeDocument(@RequestParam("id") String id) {
        documentService.deleteDocument(Long.valueOf(id));
        return ResponseEntity.ok().body("File deleted successfully: ");
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadDocument(@RequestParam String id) throws IOException {
        System.out.println("called: /document/download/"+id);
        File file = new File(fileStorageActions.getPathToFileById(id));
        byte[] bytes = Files.readAllBytes(file.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getName()).build());
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
