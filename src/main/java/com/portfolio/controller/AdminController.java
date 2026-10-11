package com.portfolio.controller;

import com.portfolio.model.Certification;
import com.portfolio.model.Education;
import com.portfolio.model.Experience;
import com.portfolio.model.PersonalInfo;
import com.portfolio.model.Project;
import com.portfolio.model.Skill;
import com.portfolio.model.AppearanceSetting;
import com.portfolio.service.CertificateThumbnailService;
import com.portfolio.service.FileStorageService;
import com.portfolio.service.PortfolioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final PortfolioService service;
    private final FileStorageService fileStorage;
    private final CertificateThumbnailService thumbnails;

    public AdminController(PortfolioService service, FileStorageService fileStorage,
                           CertificateThumbnailService thumbnails) {
        this.service = service;
        this.fileStorage = fileStorage;
        this.thumbnails = thumbnails;
    }

    // -------------------- Dashboard --------------------

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("info", service.getPersonalInfo());
        model.addAttribute("skillCount", service.getSkills().size());
        model.addAttribute("experienceCount", service.getExperiences().size());
        model.addAttribute("educationCount", service.getEducations().size());
        model.addAttribute("projectCount", service.getProjects().size());
        model.addAttribute("certificationCount", service.getCertifications().size());
        model.addAttribute("messageCount", service.getUnreadMessageCount());
        return "admin/dashboard";
    }

    // -------------------- Profile --------------------

    @GetMapping("/admin/profile")
    public String editProfile(Model model) {
        model.addAttribute("info", service.getPersonalInfo());
        return "admin/profile";
    }

    @PostMapping("/admin/profile")
    public String saveProfile(PersonalInfo info, RedirectAttributes ra) {
        PersonalInfo existing = service.getPersonalInfo();
        if (existing.getId() != null) {
            info.setId(existing.getId());
            info.setProfilePhoto(existing.getProfilePhoto());
            info.setResumeFileName(existing.getResumeFileName());
        }
        service.savePersonalInfo(info);
        ra.addFlashAttribute("message", "Profile updated successfully.");
        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/profile/photo")
    public String uploadPhoto(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (file != null && !file.isEmpty()) {
            PersonalInfo info = service.getPersonalInfo();
            fileStorage.delete(info.getProfilePhoto());
            info.setProfilePhoto(fileStorage.store(file));
            service.savePersonalInfo(info);
            ra.addFlashAttribute("message", "Profile photo updated.");
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/profile/resume")
    public String uploadResume(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (file != null && !file.isEmpty()) {
            PersonalInfo info = service.getPersonalInfo();
            fileStorage.delete(info.getResumeFileName());
            info.setResumeFileName(fileStorage.store(file));
            service.savePersonalInfo(info);
            ra.addFlashAttribute("message", "Resume uploaded successfully.");
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/admin/profile/resume/delete")
    public String deleteResume(RedirectAttributes ra) {
        PersonalInfo info = service.getPersonalInfo();
        fileStorage.delete(info.getResumeFileName());
        info.setResumeFileName(null);
        service.savePersonalInfo(info);
        ra.addFlashAttribute("message", "Resume removed.");
        return "redirect:/admin/profile";
    }

    // -------------------- Appearance --------------------

    @GetMapping("/admin/appearance")
    public String appearance(Model model) {
        model.addAttribute("appearance", service.getAppearance());
        return "admin/appearance";
    }

    @PostMapping("/admin/appearance")
    public String uploadBackground(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (file != null && !file.isEmpty()) {
            AppearanceSetting appearance = service.getAppearance();
            fileStorage.delete(appearance.getBackgroundFilename());
            appearance.setBackgroundFilename(fileStorage.store(file));
            service.saveAppearance(appearance);
            ra.addFlashAttribute("message", "Background image updated.");
        } else {
            ra.addFlashAttribute("error", "Choose an image file to upload.");
        }
        return "redirect:/admin/appearance";
    }

    @PostMapping("/admin/appearance/remove")
    public String removeBackground(RedirectAttributes ra) {
        AppearanceSetting appearance = service.getAppearance();
        fileStorage.delete(appearance.getBackgroundFilename());
        appearance.setBackgroundFilename(null);
        service.saveAppearance(appearance);
        ra.addFlashAttribute("message", "Background image removed.");
        return "redirect:/admin/appearance";
    }

    // -------------------- Skills --------------------

    @GetMapping("/admin/skills")
    public String skills(Model model) {
        model.addAttribute("skills", service.getSkills());
        return "admin/skills";
    }

    @GetMapping("/admin/skills/new")
    public String newSkill(Model model) {
        model.addAttribute("skill", new Skill());
        return "admin/skill-form";
    }

    @GetMapping("/admin/skills/edit/{id}")
    public String editSkill(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Skill skill = service.getSkill(id);
        if (skill == null) {
            ra.addFlashAttribute("error", "Skill not found.");
            return "redirect:/admin/skills";
        }
        model.addAttribute("skill", skill);
        return "admin/skill-form";
    }

    @PostMapping("/admin/skills/save")
    public String saveSkill(Skill skill, RedirectAttributes ra) {
        service.saveSkill(skill);
        ra.addFlashAttribute("message", "Skill saved.");
        return "redirect:/admin/skills";
    }

    @PostMapping("/admin/skills/delete/{id}")
    public String deleteSkill(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteSkill(id);
        ra.addFlashAttribute("message", "Skill deleted.");
        return "redirect:/admin/skills";
    }

    // -------------------- Experience --------------------

    @GetMapping("/admin/experiences")
    public String experiences(Model model) {
        model.addAttribute("experiences", service.getExperiences());
        return "admin/experiences";
    }

    @GetMapping("/admin/experiences/new")
    public String newExperience(Model model) {
        model.addAttribute("experience", new Experience());
        return "admin/experience-form";
    }

    @GetMapping("/admin/experiences/edit/{id}")
    public String editExperience(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Experience experience = service.getExperience(id);
        if (experience == null) {
            ra.addFlashAttribute("error", "Experience not found.");
            return "redirect:/admin/experiences";
        }
        model.addAttribute("experience", experience);
        return "admin/experience-form";
    }

    @PostMapping("/admin/experiences/save")
    public String saveExperience(Experience experience, RedirectAttributes ra) {
        service.saveExperience(experience);
        ra.addFlashAttribute("message", "Experience saved.");
        return "redirect:/admin/experiences";
    }

    @PostMapping("/admin/experiences/delete/{id}")
    public String deleteExperience(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteExperience(id);
        ra.addFlashAttribute("message", "Experience deleted.");
        return "redirect:/admin/experiences";
    }

    // -------------------- Education --------------------

    @GetMapping("/admin/educations")
    public String educations(Model model) {
        model.addAttribute("educations", service.getEducations());
        return "admin/educations";
    }

    @GetMapping("/admin/educations/new")
    public String newEducation(Model model) {
        model.addAttribute("education", new Education());
        return "admin/education-form";
    }

    @GetMapping("/admin/educations/edit/{id}")
    public String editEducation(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Education education = service.getEducation(id);
        if (education == null) {
            ra.addFlashAttribute("error", "Education not found.");
            return "redirect:/admin/educations";
        }
        model.addAttribute("education", education);
        return "admin/education-form";
    }

    @PostMapping("/admin/educations/save")
    public String saveEducation(Education education, RedirectAttributes ra) {
        service.saveEducation(education);
        ra.addFlashAttribute("message", "Education saved.");
        return "redirect:/admin/educations";
    }

    @PostMapping("/admin/educations/delete/{id}")
    public String deleteEducation(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteEducation(id);
        ra.addFlashAttribute("message", "Education deleted.");
        return "redirect:/admin/educations";
    }

    // -------------------- Projects --------------------

    @GetMapping("/admin/projects")
    public String projects(Model model) {
        model.addAttribute("projects", service.getProjects());
        return "admin/projects";
    }

    @GetMapping("/admin/projects/new")
    public String newProject(Model model) {
        model.addAttribute("project", new Project());
        return "admin/project-form";
    }

    @GetMapping("/admin/projects/edit/{id}")
    public String editProject(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Project project = service.getProject(id);
        if (project == null) {
            ra.addFlashAttribute("error", "Project not found.");
            return "redirect:/admin/projects";
        }
        model.addAttribute("project", project);
        return "admin/project-form";
    }

    @PostMapping("/admin/projects/save")
    public String saveProject(Project project,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        if (project.getId() != null) {
            Project existing = service.getProject(project.getId());
            if (existing != null) {
                project.setImageFilename(existing.getImageFilename());
            }
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorage.delete(project.getImageFilename());
            project.setImageFilename(fileStorage.store(imageFile));
        }
        service.saveProject(project);
        ra.addFlashAttribute("message", "Project saved.");
        return "redirect:/admin/projects";
    }

    @PostMapping("/admin/projects/image/delete/{id}")
    public String deleteProjectImage(@PathVariable Long id, RedirectAttributes ra) {
        Project project = service.getProject(id);
        if (project != null) {
            fileStorage.delete(project.getImageFilename());
            project.setImageFilename(null);
            service.saveProject(project);
            ra.addFlashAttribute("message", "Project image removed.");
        }
        return "redirect:/admin/projects/edit/" + id;
    }

    @PostMapping("/admin/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes ra) {
        Project project = service.getProject(id);
        if (project != null) {
            fileStorage.delete(project.getImageFilename());
        }
        service.deleteProject(id);
        ra.addFlashAttribute("message", "Project deleted.");
        return "redirect:/admin/projects";
    }

    // -------------------- Messages --------------------

    @GetMapping("/admin/messages")
    public String messages(Model model) {
        model.addAttribute("messages", service.getContactMessages());
        return "admin/messages";
    }

    @PostMapping("/admin/messages/read/{id}")
    public String readMessage(@PathVariable Long id) {
        service.markContactMessageRead(id);
        return "redirect:/admin/messages";
    }

    @PostMapping("/admin/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes ra) {
        service.deleteContactMessage(id);
        ra.addFlashAttribute("message", "Message deleted.");
        return "redirect:/admin/messages";
    }

    // -------------------- Certifications --------------------

    @GetMapping("/admin/certifications")
    public String certifications(Model model) {
        model.addAttribute("certifications", service.getCertifications());
        return "admin/certifications";
    }

    @GetMapping("/admin/certifications/new")
    public String newCertification(Model model) {
        model.addAttribute("certification", new Certification());
        return "admin/certification-form";
    }

    @GetMapping("/admin/certifications/edit/{id}")
    public String editCertification(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Certification certification = service.getCertification(id);
        if (certification == null) {
            ra.addFlashAttribute("error", "Certification not found.");
            return "redirect:/admin/certifications";
        }
        model.addAttribute("certification", certification);
        return "admin/certification-form";
    }

    @PostMapping("/admin/certifications/save")
    public String saveCertification(Certification certification,
                                    @RequestParam(value = "certificateFile", required = false) MultipartFile certificateFile,
                                    RedirectAttributes ra) {
        if (certification.getId() != null) {
            Certification existing = service.getCertification(certification.getId());
            if (existing != null) {
                certification.setCredentialFile(existing.getCredentialFile());
                certification.setThumbnailFile(existing.getThumbnailFile());
            }
        }
        if (certificateFile != null && !certificateFile.isEmpty()) {
            String name = certificateFile.getOriginalFilename();
            if (name == null || !name.toLowerCase().endsWith(".pdf")) {
                ra.addFlashAttribute("error", "Certificate file must be a PDF.");
                return "redirect:/admin/certifications/edit/"
                        + (certification.getId() != null ? certification.getId() : "");
            }
            fileStorage.delete(certification.getCredentialFile());
            fileStorage.delete(certification.getThumbnailFile());
            certification.setCredentialFile(fileStorage.store(certificateFile));
            certification.setThumbnailFile(thumbnails.generate(certification.getCredentialFile()));
        }
        service.saveCertification(certification);
        ra.addFlashAttribute("message", "Certification saved.");
        return "redirect:/admin/certifications";
    }

    @PostMapping("/admin/certifications/file/delete/{id}")
    public String deleteCertificationFile(@PathVariable Long id, RedirectAttributes ra) {
        Certification certification = service.getCertification(id);
        if (certification != null) {
            fileStorage.delete(certification.getCredentialFile());
            fileStorage.delete(certification.getThumbnailFile());
            certification.setCredentialFile(null);
            certification.setThumbnailFile(null);
            service.saveCertification(certification);
            ra.addFlashAttribute("message", "Certificate PDF removed.");
        }
        return "redirect:/admin/certifications/edit/" + id;
    }

    @PostMapping("/admin/certifications/delete/{id}")
    public String deleteCertification(@PathVariable Long id, RedirectAttributes ra) {
        Certification certification = service.getCertification(id);
        if (certification != null) {
            fileStorage.delete(certification.getCredentialFile());
            fileStorage.delete(certification.getThumbnailFile());
        }
        service.deleteCertification(id);
        ra.addFlashAttribute("message", "Certification deleted.");
        return "redirect:/admin/certifications";
    }
}
