package com.portfolio.controller;

import com.portfolio.model.ContactMessage;
import com.portfolio.model.Experience;
import com.portfolio.model.PersonalInfo;
import com.portfolio.model.Project;
import com.portfolio.model.Skill;
import com.portfolio.service.FileStorageService;
import com.portfolio.service.PortfolioService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PublicController {

    private final PortfolioService service;
    private final FileStorageService fileStorage;

    public PublicController(PortfolioService service, FileStorageService fileStorage) {
        this.service = service;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("visitorCounted") == null) {
            session.setAttribute("visitorCounted", true);
            service.incrementVisitCount();
        }
        PersonalInfo info = service.getPersonalInfo();
        List<Skill> skills = service.getSkills();
        List<Project> projects = service.getProjects();

        Map<String, List<Skill>> skillsByCategory = new LinkedHashMap<>();
        for (Skill skill : skills) {
            String category = (skill.getCategory() == null || skill.getCategory().isBlank())
                    ? "Skills" : skill.getCategory();
            skillsByCategory.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(skill);
        }

        Map<Long, String> projectKickers = new java.util.HashMap<>();
        for (Project p : projects) {
            String title = p.getTitle() == null ? "" : p.getTitle();
            if (title.contains("Hybris")) {
                projectKickers.put(p.getId(), "E-Commerce · SAP Hybris");
            } else if (title.contains("KloudGin")) {
                projectKickers.put(p.getId(), title.contains("CWM") ? "Utility · FSM, EAM & CWM" : "Utility · FSM & EAM");
            } else {
                projectKickers.put(p.getId(), "Project");
            }
        }

        List<Experience> experiences = service.getExperiences();
        Map<Long, String> experienceDurations = new java.util.HashMap<>();
        for (Experience ex : experiences) {
            experienceDurations.put(ex.getId(), durationText(ex.getStartDate(), ex.getEndDate()));
        }

        model.addAttribute("info", info);
        model.addAttribute("skillsByCategory", skillsByCategory);
        model.addAttribute("experiences", experiences);
        model.addAttribute("experienceDurations", experienceDurations);
        model.addAttribute("educations", service.getEducations());
        model.addAttribute("projects", projects);
        model.addAttribute("projectKickers", projectKickers);
        model.addAttribute("certifications", service.getCertifications());
        model.addAttribute("initials", initials(info.getFullName()));
        model.addAttribute("statYears", experienceYears(service.getExperiences()));
        model.addAttribute("statProjects", service.getProjects().size());
        model.addAttribute("statCerts", service.getCertifications().size());
        model.addAttribute("visitCount", service.getVisitCount());
        String bgFile = service.getAppearance().getBackgroundFilename();
        model.addAttribute("bgStyle", bgFile == null || bgFile.isBlank()
                ? null : "--bg-img: url('/background');");
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(HttpServletRequest request) {
        String base = baseUrl(request);
        return "User-agent: *\n"
                + "Allow: /\n"
                + "Disallow: /admin\n"
                + "Disallow: /login\n"
                + "Disallow: /h2-console\n"
                + "Sitemap: " + base + "/sitemap.xml\n";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(HttpServletRequest request) {
        String base = baseUrl(request);
        String lastmod = LocalDate.now().toString();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
                + "  <url><loc>" + base + "/</loc><lastmod>" + lastmod + "</lastmod>"
                + "<changefreq>weekly</changefreq><priority>1.0</priority></url>\n"
                + "</urlset>\n";
    }

    private String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isBlank()) {
            host = request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443
                    ? "" : ":" + request.getServerPort());
        }
        return scheme + "://" + host;
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String subject,
                                @RequestParam String message,
                                RedirectAttributes ra) {
        if (name.isBlank() || email.isBlank() || message.isBlank()) {
            ra.addFlashAttribute("contactError", "Please fill in your name, email and message.");
            return "redirect:/#contact";
        }
        ContactMessage m = new ContactMessage();
        m.setName(name.trim());
        m.setEmail(email.trim());
        m.setSubject(subject == null ? null : subject.trim());
        m.setMessage(message.trim());
        service.saveContactMessage(m);
        ra.addFlashAttribute("contactOk", "Thanks, " + name.trim() + "! Your message has been received — I'll get back to you soon.");
        return "redirect:/#contact";
    }

    @GetMapping("/project-image/{id}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> projectImage(@PathVariable Long id) throws IOException {
        Project project = service.getProject(id);
        if (project == null || project.getImageFilename() == null || project.getImageFilename().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(project.getImageFilename());
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        String lower = project.getImageFilename().toLowerCase();
        if (lower.endsWith(".png")) {
            type = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            type = MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".gif")) {
            type = MediaType.IMAGE_GIF;
        } else if (lower.endsWith(".webp")) {
            type = MediaType.parseMediaType("image/webp");
        }
        return ResponseEntity.ok()
                .contentType(type)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    @GetMapping("/certificate/{id}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> certificate(@PathVariable Long id) throws IOException {
        com.portfolio.model.Certification cert = service.getCertification(id);
        if (cert == null || cert.getCredentialFile() == null || cert.getCredentialFile().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(cert.getCredentialFile());
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        String downloadName = (cert.getTitle() == null ? "certificate" : cert.getTitle().replaceAll("\\s+", "_"))
                + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    @GetMapping("/certificate-thumb/{id}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> certificateThumb(@PathVariable Long id) throws IOException {
        com.portfolio.model.Certification cert = service.getCertification(id);
        if (cert == null || cert.getThumbnailFile() == null || cert.getThumbnailFile().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(cert.getThumbnailFile());
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    @GetMapping("/resume")
    public ResponseEntity<InputStreamResource> resume() throws IOException {
        PersonalInfo info = service.getPersonalInfo();
        String storedName = info.getResumeFileName();
        if (storedName == null || storedName.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(storedName);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        String downloadName = (info.getFullName() == null ? "resume" : info.getFullName().replaceAll("\\s+", "_"))
                + extension(storedName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    @GetMapping("/photo")
    @ResponseBody
    public ResponseEntity<InputStreamResource> photo() throws IOException {
        PersonalInfo info = service.getPersonalInfo();
        String storedName = info.getProfilePhoto();
        if (storedName == null || storedName.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(storedName);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        String lower = storedName.toLowerCase();
        if (lower.endsWith(".png")) {
            type = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            type = MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".gif")) {
            type = MediaType.IMAGE_GIF;
        } else if (lower.endsWith(".webp")) {
            type = MediaType.parseMediaType("image/webp");
        }
        return ResponseEntity.ok()
                .contentType(type)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    @GetMapping("/background")
    @ResponseBody
    public ResponseEntity<InputStreamResource> background() throws IOException {
        String storedName = service.getAppearance().getBackgroundFilename();
        if (storedName == null || storedName.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorage.load(storedName);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(imageType(storedName))
                .contentLength(Files.size(path))
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    private MediaType imageType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "P";
        }
        StringBuilder sb = new StringBuilder();
        for (String word : fullName.trim().split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return sb.length() > 2 ? sb.substring(0, 2) : sb.toString();
    }

    private String durationText(LocalDate start, LocalDate end) {
        if (start == null) {
            return null;
        }
        LocalDate finish = end != null ? end : LocalDate.now();
        if (finish.isBefore(start)) {
            return null;
        }
        java.time.Period p = java.time.Period.between(start, finish);
        StringBuilder sb = new StringBuilder();
        if (p.getYears() > 0) {
            sb.append(p.getYears()).append(p.getYears() == 1 ? " yr" : " yrs");
        }
        if (p.getMonths() > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(p.getMonths()).append(p.getMonths() == 1 ? " mo" : " mos");
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String experienceYears(List<Experience> experiences) {
        if (experiences.isEmpty()) {
            return "0";
        }
        LocalDate start = experiences.stream()
                .map(Experience::getStartDate)
                .filter(java.util.Objects::nonNull)
                .min(java.util.Comparator.naturalOrder())
                .orElse(LocalDate.now());
        long months = java.time.temporal.ChronoUnit.MONTHS.between(start, LocalDate.now());
        if (months <= 0) {
            return "0";
        }
        double years = Math.round((months / 12.0) * 10) / 10.0;
        if (years == Math.floor(years)) {
            return String.valueOf((int) years);
        }
        return String.valueOf(years);
    }
}
