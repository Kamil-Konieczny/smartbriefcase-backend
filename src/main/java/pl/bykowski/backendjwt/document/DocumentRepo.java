package pl.bykowski.backendjwt.document;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {
    @Query(value = "SELECT max(id) from document;", nativeQuery = true)
    Long findMaxId();
    List<Document> findByEmail(String email);

}
