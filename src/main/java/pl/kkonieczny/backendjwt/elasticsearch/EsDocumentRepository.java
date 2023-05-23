package pl.kkonieczny.backendjwt.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, Long> {

    List<EsDocument> findFuzzyByEmailAndContentContaining(String email, String keyword);

    List<EsDocument> findByEmailAndTitleContaining(String email, String title);

    List<EsDocument> findByEmailAndDateAddedBetween(String email, LocalDate minDate, LocalDate maxDate);

    List<EsDocument> findByEmail(String email);

}
