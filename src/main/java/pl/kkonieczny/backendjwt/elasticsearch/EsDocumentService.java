package pl.kkonieczny.backendjwt.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

@Service
public class EsDocumentService {
    @Autowired
    private EsDocumentRepository esDocumentRepository;

    public void createProductIndexBulk(final List<EsDocument> products) {
        esDocumentRepository.saveAll(products);
    }

    public void createProductIndex(final EsDocument product) {
        esDocumentRepository.save(product);
    }

    public List<EsDocument> findByEmailAndContentContaining(String email, String keyword) {
        return esDocumentRepository.findFuzzyByEmailAndContentContaining(email, keyword);
    }

    public List<EsDocument> findByEmailAndTitle(String email, String title) {
        return esDocumentRepository.findByEmailAndTitleContaining(email, title);
    }

    public List<EsDocument> findByEmailAndDate(String email, String minDate, String maxDate) {
        LocalDate parse = LocalDate.parse(minDate);
        LocalDate parse1 = LocalDate.parse(maxDate);

        return esDocumentRepository.findByEmailAndDateAddedBetween(email, parse, parse1);
    }

    public List<EsDocument> findByEmailAndWordsNumber(String email, String minWordsNumber, String maxWordsNumber) {
        List<EsDocument> all = esDocumentRepository.findFuzzyByEmailAndContentContaining(email, "");
        List<EsDocument> correctDocs = new ArrayList<>();
        for(EsDocument es: all){
            String[] words = es.getContent().split("\\s+");
            int numWords = words.length;
            if(minWordsNumber==""){minWordsNumber= String.valueOf(0);}
            if(maxWordsNumber==""){maxWordsNumber=String.valueOf(99999999);}
            if (numWords>=parseInt(minWordsNumber) && numWords<=parseInt(maxWordsNumber)){
                correctDocs.add(es);
            }
        }
        return correctDocs;
    }

    public void deleteById(Long id) {
    esDocumentRepository.deleteById(id);
    }
}
