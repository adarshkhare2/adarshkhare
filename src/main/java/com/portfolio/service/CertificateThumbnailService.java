package com.portfolio.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

@Service
public class CertificateThumbnailService {

    private static final int DPI = 110;

    private final FileStorageService fileStorage;

    public CertificateThumbnailService(FileStorageService fileStorage) {
        this.fileStorage = fileStorage;
    }

    public String generate(String pdfStoredName) {
        if (pdfStoredName == null || pdfStoredName.isBlank()) {
            return null;
        }
        Path pdf = fileStorage.load(pdfStoredName);
        if (!Files.exists(pdf)) {
            return null;
        }
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            if (doc.getNumberOfPages() < 1) {
                return null;
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImageWithDPI(0, DPI, ImageType.RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return fileStorage.storeBytes(out.toByteArray(), ".png");
        } catch (Exception e) {
            return null;
        }
    }
}
