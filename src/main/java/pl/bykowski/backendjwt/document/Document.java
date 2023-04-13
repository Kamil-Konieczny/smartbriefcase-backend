package pl.bykowski.backendjwt.document;

import javax.persistence.*;
import java.io.*;
import java.util.*;

@Entity
public class Document implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
    private String extension;
    private Date date;
    @Lob
    private byte[] fileBytes;

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document() {
    }


    public Document(String email, String name, String extension, Date date, byte[] fileBytes) {
        this.email = email;
        this.name = name;
        this.extension = extension;
        this.date = date;
        this.fileBytes = fileBytes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
