package pl.bykowski.backendjwt.filsServices;

import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpHost;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentType;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.bykowski.backendjwt.document.Document;
import pl.bykowski.backendjwt.document.DocumentService;
import pl.bykowski.backendjwt.textExtraction.FileExtentionRecognition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import java.io.IOException;

@Component
public class FileStorageActions {
    @Autowired
    DocumentService documentService;

    public void saveFile(MultipartFile file, String email) throws IOException, InvalidFormatException, URISyntaxException, Docx4JException, TesseractException {
        Long lastID = getLastDocumentID();
        String extractedText = FileExtentionRecognition.extractText(file);
        System.out.println("lastID "+lastID);
        saveFileInfoInDatabase(file, email);
        System.out.println("lastID after operation: "+getLastDocumentID());

        if(lastID.equals(getLastDocumentID()-1L)){
            if(saveFileInFileStorage(file, lastID))
            {
                sendExtractedTextToElastic(extractedText);
            }
            else {
                System.out.println("File failed to save in file storage, database is about to rollback last row !");
                documentService.removeLastRow();
            }
        }

    }

    private String pathToCreateFile = "C://Users/kamil/Documents/tmp/";

    public long getLastDocumentID() {
        return documentService.getLastId();
    }

    public void saveFileInfoInDatabase(MultipartFile file, String email) throws IOException, InvalidFormatException, URISyntaxException, Docx4JException {
        String originalFileName = file.getOriginalFilename();
        int lastDotIndex = originalFileName.lastIndexOf(".");
        originalFileName.substring(lastDotIndex + 1);
        Calendar calendar = Calendar.getInstance();
        documentService.saveDocument(new Document(email, file.getOriginalFilename(), originalFileName.substring(lastDotIndex + 1).toUpperCase(), calendar.getTime(), extractFirstPageAsJpg(file)));
    }

    private boolean saveFileInFileStorage(MultipartFile file, Long lastIDFromDB) {
        boolean operation_success = true;
        byte[] bytes = new byte[0];
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            operation_success = false;
        }
        Long lastID = getLastDocumentID();
        System.out.println(getLastDocumentID());
        System.out.println("Przetwarzanie pliku " + file.getOriginalFilename());

        File newDirectory = new File(pathToCreateFile, String.valueOf(lastID));
        if (!newDirectory.exists()) {
            boolean success = newDirectory.mkdir();
            if (success) {
                System.out.println("Directory created successfully");
            } else {
                operation_success = false;
                System.out.println("Operation about to rollback. Failed to create directory");
            }
        } else {
            operation_success = false;
            System.out.println("Operation about to rollback. Directory already exists");
        }
        Path path = Paths.get(pathToCreateFile + lastID + "/" + file.getOriginalFilename());
        if(operation_success){
            try {
                Files.write(path, bytes);
            } catch (IOException e) {
                operation_success = false;
            }
            System.out.println("Zapisano plik w bazie plików: " + file.getOriginalFilename());
            System.out.println("Plik zapisany w folderze z id: " + lastID);
        }
        return operation_success;
    }

    public void sendExtractedTextToElastic(String DOCUMENT_NAME_FIELD, String DOCUMENT_ID_FIELD, String DOCUMENT_CONTENT_FIELD) throws IOException {
         String INDEX_NAME = "documents";
         String DOCUMENT_TYPE = "_doc";

            // Create a client for connecting to Elasticsearch
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http")));

            try {
                // Check if the index already exists, and create it if it doesn't
                if (!client.indices().exists(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT)) {
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_NAME);
                    client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                }
            } catch (IndexNotFoundException e) {
                System.out.println("Index not found: " + INDEX_NAME);
            }

            // Add a document to the index
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put(DOCUMENT_NAME_FIELD, DOCUMENT_NAME_FIELD);
            jsonMap.put(DOCUMENT_ID_FIELD, DOCUMENT_ID_FIELD);
            jsonMap.put(DOCUMENT_CONTENT_FIELD, DOCUMENT_CONTENT_FIELD);

            IndexRequest indexRequest = new IndexRequest(INDEX_NAME, DOCUMENT_TYPE)
                    .source(jsonMap, XContentType.JSON);
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

            if (indexResponse.status() == RestStatus.CREATED) {
                System.out.println("Document added successfully.");
            } else {
                System.out.println("Failed to add document.");
            }

            // Close the client connection
            client.close();
        }

    public byte[] extractFirstPageAsJpg(MultipartFile file) throws IOException, InvalidFormatException, URISyntaxException, Docx4JException {
        System.out.println("-------------------------Extracting first page of file--------------------");
        File tmpFile = File.createTempFile("temp", ".pdf");
        System.out.println(file.getContentType());
        switch (file.getContentType()) {
            case "application/pdf":
                file.transferTo(tmpFile);
                PDDocument document = PDDocument.load(tmpFile);
                PDPage page = document.getPage(0);
                PDFRenderer renderer = new PDFRenderer(document);
                BufferedImage image = renderer.renderImage(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                System.out.println("-------------------------First page extracted--------------------");
                document.close();
                tmpFile.delete();
                return baos.toByteArray();
            case "image/tiff":
            case "image/bmp":
            case "image/jpeg":
            case "image/png":
                InputStream inputStream = file.getInputStream();
                BufferedImage image1 = ImageIO.read(inputStream);
                BufferedImage resizedImage = Scalr.resize(image1, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, 1000, Scalr.OP_ANTIALIAS);
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos1);
                System.out.println("-------------------------First page extracted--------------------");
                return baos1.toByteArray();

            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                InputStream inputStream2 = file.getInputStream();
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream2);

                MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                FileOutputStream os = new FileOutputStream(pathToCreateFile+"tmp.pdf");
                Docx4J.toPDF(wordMLPackage,os);
                os.flush();
                os.close();
                File pdf = new File(pathToCreateFile+"tmp.pdf");
                PDDocument pdDocument = PDDocument.load(pdf);
                PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
                BufferedImage bufferedImage = pdfRenderer.renderImage(0);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                System.out.println("-------------------------First page extracted--------------------");
                pdDocument.close();
                tmpFile.delete();
                return byteArrayOutputStream.toByteArray();
            default:
                throw new IllegalArgumentException("Unsupported file type");
        }
    }


}
