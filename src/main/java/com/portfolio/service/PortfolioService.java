package com.portfolio.service;

import com.portfolio.model.Certification;
import com.portfolio.model.AppearanceSetting;
import com.portfolio.model.ContactMessage;
import com.portfolio.model.Education;
import com.portfolio.model.Experience;
import com.portfolio.model.PersonalInfo;
import com.portfolio.model.Project;
import com.portfolio.model.Skill;
import com.portfolio.model.VisitStat;
import com.portfolio.repository.CertificationRepository;
import com.portfolio.repository.AppearanceRepository;
import com.portfolio.repository.ContactMessageRepository;
import com.portfolio.repository.EducationRepository;
import com.portfolio.repository.ExperienceRepository;
import com.portfolio.repository.PersonalInfoRepository;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.repository.SkillRepository;
import com.portfolio.repository.VisitStatRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PortfolioService {

    private final PersonalInfoRepository personalInfoRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final VisitStatRepository visitStatRepository;
    private final AppearanceRepository appearanceRepository;
    private final ContactMessageRepository contactMessageRepository;

    public PortfolioService(PersonalInfoRepository personalInfoRepository,
                            SkillRepository skillRepository,
                            ExperienceRepository experienceRepository,
                            EducationRepository educationRepository,
                            ProjectRepository projectRepository,
                            CertificationRepository certificationRepository,
                            VisitStatRepository visitStatRepository,
                            AppearanceRepository appearanceRepository,
                            ContactMessageRepository contactMessageRepository) {
        this.personalInfoRepository = personalInfoRepository;
        this.skillRepository = skillRepository;
        this.experienceRepository = experienceRepository;
        this.educationRepository = educationRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.visitStatRepository = visitStatRepository;
        this.appearanceRepository = appearanceRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional(readOnly = true)
    public PersonalInfo getPersonalInfo() {
        return personalInfoRepository.findAll().stream().findFirst().orElseGet(PersonalInfo::new);
    }

    public PersonalInfo savePersonalInfo(PersonalInfo info) {
        return personalInfoRepository.save(info);
    }

    @Transactional(readOnly = true)
    public List<Skill> getSkills() {
        return skillRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    public Skill saveSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Transactional(readOnly = true)
    public Skill getSkill(Long id) {
        return skillRepository.findById(id).orElse(null);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    public long getVisitCount() {
        return visitStatRepository.findAll().stream().mapToLong(VisitStat::getCount).sum();
    }

    public void incrementVisitCount() {
        java.util.List<VisitStat> all = visitStatRepository.findAll();
        VisitStat stat = all.isEmpty() ? new VisitStat() : all.get(0);
        stat.setCount(stat.getCount() + 1);
        visitStatRepository.save(stat);
    }

    public com.portfolio.model.AppearanceSetting getAppearance() {
        return appearanceRepository.findAll().stream().findFirst()
                .orElseGet(com.portfolio.model.AppearanceSetting::new);
    }

    public com.portfolio.model.AppearanceSetting saveAppearance(com.portfolio.model.AppearanceSetting appearance) {
        return appearanceRepository.save(appearance);
    }

    @Transactional(readOnly = true)
    public List<Experience> getExperiences() {
        return experienceRepository.findAllByOrderByStartDateDesc();
    }

    public Experience saveExperience(Experience experience) {
        return experienceRepository.save(experience);
    }

    @Transactional(readOnly = true)
    public Experience getExperience(Long id) {
        return experienceRepository.findById(id).orElse(null);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Education> getEducations() {
        return educationRepository.findAllByOrderByEndYearDesc();
    }

    public Education saveEducation(Education education) {
        return educationRepository.save(education);
    }

    @Transactional(readOnly = true)
    public Education getEducation(Long id) {
        return educationRepository.findById(id).orElse(null);
    }

    public void deleteEducation(Long id) {
        educationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjects() {
        return projectRepository.findAllByOrderByDisplayOrderAscTitleAsc();
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Project getProject(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Certification> getCertifications() {
        return certificationRepository.findAllByOrderByYearDesc();
    }

    public Certification saveCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    @Transactional(readOnly = true)
    public Certification getCertification(Long id) {
        return certificationRepository.findById(id).orElse(null);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    public void saveContactMessage(ContactMessage message) {
        message.setCreatedAt(java.time.LocalDateTime.now());
        contactMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> getContactMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount() {
        return contactMessageRepository.findAll().stream().filter(m -> !m.isRead()).count();
    }

    public void deleteContactMessage(Long id) {
        contactMessageRepository.deleteById(id);
    }

    public void markContactMessageRead(Long id) {
        contactMessageRepository.findById(id).ifPresent(m -> {
            m.setRead(true);
            contactMessageRepository.save(m);
        });
    }
}
