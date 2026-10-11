package com.portfolio.config;

import com.portfolio.model.Certification;
import com.portfolio.service.CertificateThumbnailService;
import com.portfolio.service.FileStorageService;
import com.portfolio.service.PortfolioService;
import java.nio.file.Files;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CertificateThumbnailInitializer implements ApplicationRunner {

    private final PortfolioService service;
    private final CertificateThumbnailService thumbnails;
    private final FileStorageService fileStorage;

    public CertificateThumbnailInitializer(PortfolioService service,
                                           CertificateThumbnailService thumbnails,
                                           FileStorageService fileStorage) {
        this.service = service;
        this.thumbnails = thumbnails;
        this.fileStorage = fileStorage;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (Certification cert : service.getCertifications()) {
            if (cert.getCredentialFile() == null || cert.getCredentialFile().isBlank()) {
                continue;
            }
            boolean missing = cert.getThumbnailFile() == null || cert.getThumbnailFile().isBlank()
                    || !Files.exists(fileStorage.load(cert.getThumbnailFile()));
            if (!missing) {
                continue;
            }
            String generated = thumbnails.generate(cert.getCredentialFile());
            if (generated != null) {
                cert.setThumbnailFile(generated);
                service.saveCertification(cert);
            }
        }
    }
}
