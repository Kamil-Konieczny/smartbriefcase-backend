package pl.bykowski.backendjwt.document;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.print.Doc;
import java.util.*;

@Service
public class DocumentService {
    @Autowired
    DocumentRepo documentRepo;

    public List<Document> getDocumentsbyUserEmail(String email)
    {
        return documentRepo.findByEmail(email);
    }
    public void removeLastRow(){
        Long maxId = documentRepo.findMaxId();
        documentRepo.deleteById(maxId);
    }
    public Optional<Document> getOneDocument(Long doc_id) {
        return documentRepo.findById(doc_id);
    }

    public Long getLastId(){
        Long maxID ;
        if(documentRepo.findMaxId()!=null){
           maxID = documentRepo.findMaxId();
        }
        else {
            maxID = 0L;
        }
        return maxID;
    }

    public Document saveDocument(Document document){

        System.out.println(document.getFileBytes());
        return documentRepo.save(document);
    }
}
