package pl.kkonieczny.backendjwt.filsServices;

import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.kkonieczny.backendjwt.document.Document;
import pl.kkonieczny.backendjwt.document.DocumentService;
import pl.kkonieczny.backendjwt.elasticsearch.EsDocument;
import pl.kkonieczny.backendjwt.elasticsearch.EsDocumentService;
import pl.kkonieczny.backendjwt.textExtraction.FileExtentionRecognition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;


import java.io.IOException;

@Component
public class FileStorageActions {
    @Autowired
    DocumentService documentService;
    @Autowired
    EsDocumentService esDocumentService;

    public void saveFile(MultipartFile file, String email) throws IOException, InvalidFormatException, URISyntaxException, Docx4JException, TesseractException {
        Long lastID = getLastDocumentID();
        String extractedText = FileExtentionRecognition.extractText(file);
        System.out.println("lastID "+lastID);
        saveFileInfoInDatabase(file, email);
        System.out.println("lastID after operation: "+getLastDocumentID());
        String originalFileName = file.getOriginalFilename();

        if(lastID.equals(getLastDocumentID()-1L)){
            if(saveFileInFileStorage(file, lastID))
            {
                sendExtractedTextToElastic(extractedText, getLastDocumentID(), email, originalFileName);
            }
            else {
                System.out.println("File failed to save in file storage, database is about to rollback last row !");
                documentService.removeLastRow();
            }
        }

    }

    private String pathToFile = "C://Users/kamil/Documents/tmp/";

    public String getPathToFileById(String id){
        String fileName = getSingleFileName(pathToFile+id);
        System.out.println("************************************"+pathToFile+id+"/"+fileName);
        return pathToFile+id+"/"+fileName;
    }
    public String getSingleFileName(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files == null || files.length != 1) {
            // either the directory is empty or there are multiple files in it
            return null;
        }

        return files[0].getName();
    }
    public long getLastDocumentID() {
        return documentService.getLastId();
    }

    public void saveFileInfoInDatabase(MultipartFile file, String email) throws IOException, Docx4JException {
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
            System.out.println(e.getMessage());
            operation_success = false;
        }
        Long lastID = getLastDocumentID();
        System.out.println(getLastDocumentID());
        System.out.println("Przetwarzanie pliku " + file.getOriginalFilename());

        File newDirectory = new File(pathToFile, String.valueOf(lastID));
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
        Path path = Paths.get(pathToFile + lastID + "/" + file.getOriginalFilename());
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

    public void sendExtractedTextToElastic(String content, Long id, String email, String name)  {
        System.out.println("Temple date: "+LocalDate.now());
        EsDocument esDocument = new EsDocument(name, id, email, content, LocalDate.now());
        esDocumentService.createProductIndex(esDocument);
    }


    public byte[] extractFirstPageAsJpg(MultipartFile file) throws IOException, Docx4JException {
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
                FileOutputStream os = new FileOutputStream(pathToFile +"tmp.pdf");
                Docx4J.toPDF(wordMLPackage,os);
                os.flush();
                os.close();
                File pdf = new File(pathToFile +"tmp.pdf");
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


    public void removeFromStorageById(Long id) {
        System.out.println("Deleted from api");
        File directory = new File(pathToFile, String.valueOf(id));
        System.out.println("first exist");
        if (!directory.exists()) {
            System.out.println("directory exist");
            return;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            System.out.println("File is different than null");
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("File is directory");
                    removeFromStorageById(Long.valueOf(file.getAbsolutePath()));
                } else {
                    System.out.println("else");
                    file.delete();
                }
            }
        }

        directory.delete();
    }
}
