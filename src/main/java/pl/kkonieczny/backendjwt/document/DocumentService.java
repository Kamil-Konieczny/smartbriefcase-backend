package pl.kkonieczny.backendjwt.document;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import pl.kkonieczny.backendjwt.elasticsearch.EsDocument;
import pl.kkonieczny.backendjwt.elasticsearch.EsDocumentService;
import pl.kkonieczny.backendjwt.filsServices.FileStorageActions;

import java.util.*;

@Service
public class DocumentService {
    @Autowired
    DocumentRepo documentRepo;
    @Autowired
    EsDocumentService esDocumentService;

    public List<Document> getDocumentsbyUserEmail(String email) {
        for(Document document : documentRepo.findByEmail(email)) {
            document.setSubtext(String.valueOf(document.getDate()));
            System.out.println(document.getTitle());
        }
        return documentRepo.findByEmail(email);
    }

    public List<Document> getDocumentsbyUserEmail(String email, String keyword) {
        List<EsDocument> byEmailAndContentContaining = esDocumentService.findByEmailAndContentContaining(email, keyword);
        for(EsDocument document:byEmailAndContentContaining){
            String content = document.getContent();
            String[] words = content.split("\\s+");

            for (int i = 0; i < words.length; i++) {
                if (words[i].contains(keyword) ||  words[i].equalsIgnoreCase(keyword)) {
                    int startIndex = Math.max(0, i - 4);
                    int endIndex = Math.min(words.length - 1, i + 4);
                    String[] result = Arrays.copyOfRange(words, startIndex, endIndex + 1);
                    String output="";
                    for(String str : result){
                        output += " "+str;
                    }
                    document.setSubtext(output);
                    break;
                }
        }}
        return convertEStoDBtypeKeyWord(byEmailAndContentContaining, keyword);
    }
    public List<Document> getDocumentsbyUserEmail(String email, String keyword, String title){
        List<EsDocument> byEmailAndTitleContaining = esDocumentService.findByEmailAndTitle(email, title);
        return convertEStoDBtypeTitle(byEmailAndTitleContaining, title);
    }

    public List<Document> getDocumentsbyUserEmail(String email, String keyword, String title, String minDate, String maxDate){
        List<EsDocument> byEmailAndDate = esDocumentService.findByEmailAndDate(email, minDate, maxDate);
        return convertEStoDBtype(byEmailAndDate);
    }

    public List<Document> getDocumentsbyUserEmail(String email, String keyword, String title, String minDate, String maxDate, String minWordsNumber, String maxWordsNumber){
        List<EsDocument> byEmailAndDate = esDocumentService.findByEmailAndWordsNumber(email, minWordsNumber, maxWordsNumber);
        return convertEStoDBtypeWordsNumber(byEmailAndDate);
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
        public List<Document> convertEStoDBtypeKeyWord(List<EsDocument> esDocumentList, String keyword){
        List<Document> documentList = new ArrayList();
        for(EsDocument esdocument:esDocumentList){
            Document referenceById = documentRepo.getById(esdocument.getDocumentId());
            String editedSubtext = esdocument.getSubtext().replaceAll("(?i)" + keyword, "<span style='background-color: yellow;'>"+keyword+"</span>");
            System.out.println("edited: "+ editedSubtext);
            referenceById.setSubtext(editedSubtext);
            documentList.add(referenceById);
        }
        return documentList;
    }
    public List<Document> convertEStoDBtypeTitle(List<EsDocument> esDocumentList, String title){
        List<Document> documentList = new ArrayList();
        for(EsDocument esdocument:esDocumentList){
            Document referenceById = documentRepo.getById(esdocument.getDocumentId());
            String editedTitle = esdocument.getTitle().replaceAll("(?i)" + title, "<span style='background-color: yellow;'>"+title+"</span>");
            referenceById.setSubtext(String.valueOf(referenceById.getDate()));
            referenceById.setTitle(editedTitle);
            documentList.add(referenceById);
        }
        return documentList;
    }

    public List<Document> convertEStoDBtypeWordsNumber(List<EsDocument> esDocumentList){
        List<Document> documentList = new ArrayList();
        for(EsDocument esdocument:esDocumentList){
            String[] words = esdocument.getContent().split("\\s+");
            int numWords = words.length;
            Document referenceById = documentRepo.getById(esdocument.getDocumentId());
            String num = "Words: "+numWords;
            referenceById.setSubtext("<span style='background-color: yellow;'>"+ num + "</span>");
            documentList.add(referenceById);
        }
        return documentList;
    }
    public List<Document> convertEStoDBtype(List<EsDocument> esDocumentList){
        List<Document> documentList = new ArrayList();
        for(EsDocument esdocument:esDocumentList){
            Document referenceById = documentRepo.getById(esdocument.getDocumentId());
            referenceById.setSubtext(String.valueOf(referenceById.getDate()));
            documentList.add(referenceById);
        }
        return documentList;
    }

    public void deleteDocument(Long id) {
        FileStorageActions fileStorageActions = new FileStorageActions();
        fileStorageActions.removeFromStorageById(id);
        documentRepo.deleteById(id);
        esDocumentService.deleteById(id);
    }
}
