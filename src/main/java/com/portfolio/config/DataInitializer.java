package com.portfolio.config;

import com.portfolio.model.Certification;
import com.portfolio.model.Education;
import com.portfolio.model.Experience;
import com.portfolio.model.PersonalInfo;
import com.portfolio.model.Project;
import com.portfolio.model.Skill;
import com.portfolio.repository.CertificationRepository;
import com.portfolio.repository.EducationRepository;
import com.portfolio.repository.ExperienceRepository;
import com.portfolio.repository.PersonalInfoRepository;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.repository.SkillRepository;
import com.portfolio.repository.VisitStatRepository;
import com.portfolio.model.VisitStat;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(PersonalInfoRepository personalRepo,
                                      SkillRepository skillRepo,
                                      ExperienceRepository experienceRepo,
                                      EducationRepository educationRepo,
                                      ProjectRepository projectRepo,
                                      CertificationRepository certificationRepo,
                                      VisitStatRepository visitRepo) {
        return args -> {
            if (personalRepo.count() == 0) {
                PersonalInfo info = new PersonalInfo();
                info.setFullName("Adarsh Khare");
                info.setTitle("Software Developer");
                info.setEmail("adarshkhare25032000@gmail.com");
                info.setPhone("+91 76920 37490");
                info.setLocation("Hyderabad, India");
                info.setSummary("Software Developer with 4+ years of experience designing and building scalable "
                        + "backend systems and enterprise e-commerce platforms using Java, Spring Boot, and SAP Hybris. "
                        + "Currently at KloudGin, Inc., working on a cloud-native SaaS platform for Field Service "
                        + "Management (FSM), Enterprise Asset Management (EAM), and Construction Work Management (CWM) "
                        + "on US utility engagements. Previously delivered SAP Hybris B2C e-commerce for leading retail "
                        + "clients at Techouts. Experienced across AWS cloud, microservices, CI/CD, and Agile delivery.");
                info.setLinkedin("https://www.linkedin.com/in/adarsh-khare-1822651ba");
                info.setInstagram("https://www.instagram.com/adarsh_khare_");
                personalRepo.save(info);
            }

            if (skillRepo.count() == 0) {
                skillRepo.save(skill("Java", "Backend", 90, 1));
                skillRepo.save(skill("Spring Boot", "Backend", 85, 2));
                skillRepo.save(skill("Spring MVC", "Backend", 80, 3));
                skillRepo.save(skill("Spring Security", "Backend", 75, 4));
                skillRepo.save(skill("Apache Kafka", "Backend", 60, 5));
                skillRepo.save(skill("SAP Hybris Commerce", "Platforms", 85, 6));
                skillRepo.save(skill("KloudGin FSM/EAM", "Platforms", 80, 7));
                skillRepo.save(skill("Oracle", "Databases", 75, 8));
                skillRepo.save(skill("MySQL", "Databases", 75, 9));
                skillRepo.save(skill("MongoDB", "Databases", 70, 10));
                skillRepo.save(skill("AWS (S3, EC2, RDS)", "Cloud & DevOps", 70, 11));
                skillRepo.save(skill("Jenkins", "Cloud & DevOps", 70, 12));
                skillRepo.save(skill("Git", "Tools", 80, 13));
                skillRepo.save(skill("Bitbucket", "Tools", 80, 14));
                skillRepo.save(skill("Postman", "Tools", 80, 15));
                skillRepo.save(skill("JIRA", "Tools", 70, 16));
                skillRepo.save(skill("Impex & Groovy", "Scripting", 75, 17));
            }

            if (experienceRepo.count() == 0) {
                experienceRepo.save(experience(
                        "Software Developer", "KloudGin, Inc.", "Hyderabad, India",
                        LocalDate.of(2025, 5, 1), null, true,
                        "Developing Java Spring Boot microservices for a cloud-native SaaS platform in Field Service "
                                + "Management (FSM), Enterprise Asset Management (EAM), and Construction Work "
                                + "Management (CWM).\n"
                                + "Working across the Tempe, AZ and Pasadena, CA utility engagements — handling "
                                + "transactional and master data with MongoDB and SQL, and deploying on AWS "
                                + "(S3, EC2, RDS). Includes GIS integration: assets pulled from GIS for attribute "
                                + "review and correction, then updated back to GIS.\n"
                                + "Collaborating in Agile teams using Bitbucket for version control and CI/CD.\n"
                                + "Environment: Java, Spring Boot, Microservices, MongoDB, SQL, AWS, KloudGin"));

                experienceRepo.save(experience(
                        "Software Developer", "Techouts", "India",
                        LocalDate.of(2022, 6, 1), LocalDate.of(2025, 4, 30), false,
                        "Developer on SAP Hybris B2C e-commerce engagements for clients Shoppers Stop and Croma.\n"
                                + "Customized the SAP Hybris Multichannel Suite — OMS, live pricing, cart and checkout, "
                                + "controllers, facades, DAOs, DTOs, WCMS components, and the OCC layer for integrations.\n"
                                + "Resolved high-priority production issues (Wishlist, RMA, Cart, Orders) and wrote "
                                + "Groovy and Impex scripts for automation, data migration, and customization.\n"
                                + "Environment: Java, SAP Hybris, Oracle, MySQL, Jenkins, Git, JIRA"));
            }

            if (educationRepo.count() == 0) {
                educationRepo.save(education(
                        "B.E. in Computer Science & Engineering", "IES IPS Academy, Indore", "Indore", "2018", "2022",
                        "Graduated with 79.5%."));
            }

            if (projectRepo.count() == 0) {
                projectRepo.save(project("Shoppers Stop – SAP Hybris E-Commerce",
                        "B2C storefront built on the SAP Hybris Multichannel Suite. Customized OMS and live pricing "
                                + "modules, built and optimized controllers, facades, DAOs, DTOs and WCMS components, "
                                + "and resolved high-priority production issues across Wishlist, RMA, Cart and Orders.",
                        "Java, SAP Hybris, Oracle, Jenkins, Git, JIRA", 1));
                projectRepo.save(project("Croma – SAP Hybris E-Commerce",
                        "B2C e-commerce engagement focused on the OCC layer — built APIs for system integrations, "
                                + "improved cart and checkout flows, customized interceptors and Backoffice logic, and "
                                + "used Impex for product data import and validation.",
                        "Java, SAP Hybris, MySQL, Jenkins", 2));
                projectRepo.save(project("Tempe, AZ – KloudGin FSM & EAM",
                        "US utility engagement (Tempe, Arizona) on KloudGin. Java Spring Boot microservices for "
                                + "transactional and master data with MongoDB and SQL, deployed on AWS. Includes GIS "
                                + "integration: assets pulled from GIS for attribute review and correction, then updated "
                                + "back to GIS.",
                        "Java, Spring Boot, MongoDB, SQL, AWS, KloudGin", 3));
                projectRepo.save(project("Pasadena, CA – KloudGin FSM, EAM & CWM",
                        "US utility engagement (Pasadena, California) on KloudGin. Java Spring Boot microservices, "
                                + "MongoDB and SQL data handling, and AWS-based infrastructure. Covers Field Service "
                                + "Management (FSM), Enterprise Asset Management (EAM), and Construction Work "
                                + "Management (CWM). Includes GIS integration: assets pulled from GIS for attribute "
                                + "review and correction, then updated back to GIS.",
                        "Java, Spring Boot, MongoDB, SQL, AWS, KloudGin", 4));
            }

            if (certificationRepo.count() == 0) {
                certificationRepo.save(certification("KloudGin Certified Platform Developer", "KloudGin, Inc.",
                        "2026", "", ""));
                certificationRepo.save(certification("KloudGin Asset Management Suite Specialist", "KloudGin, Inc.",
                        "2026", "", ""));
                certificationRepo.save(certification("AWS Cloud Practitioner Essentials", "Simplilearn SkillUp",
                        "2025", "Fundamentals of AWS cloud services.", ""));
                certificationRepo.save(certification("Java Programming Certification",
                        "Spoken Tutorial Project, IIT Bombay (IES IPS Academy, Indore)", "2021", "", ""));
            }

            if (visitRepo.count() == 0) {
                VisitStat visitStat = new VisitStat();
                visitStat.setCount(0);
                visitRepo.save(visitStat);
            }
        };
    }

    private Skill skill(String name, String category, int level, int order) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        skill.setLevel(level);
        skill.setDisplayOrder(order);
        return skill;
    }

    private Experience experience(String role, String company, String location,
                                  LocalDate start, LocalDate end, boolean current, String description) {
        Experience exp = new Experience();
        exp.setRole(role);
        exp.setCompany(company);
        exp.setLocation(location);
        exp.setStartDate(start);
        exp.setEndDate(end);
        exp.setCurrent(current);
        exp.setDescription(description);
        return exp;
    }

    private Project project(String title, String description, String techStack, int order) {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(description);
        p.setTechStack(techStack);
        p.setLink("");
        p.setRepoLink("");
        p.setDisplayOrder(order);
        return p;
    }

    private Certification certification(String title, String issuer, String year,
                                        String description, String credentialUrl) {
        Certification c = new Certification();
        c.setTitle(title);
        c.setIssuer(issuer);
        c.setYear(year);
        c.setDescription(description);
        c.setCredentialUrl(credentialUrl);
        return c;
    }

    private Education education(String degree, String institution, String location,
                                String startYear, String endYear, String description) {
        Education edu = new Education();
        edu.setDegree(degree);
        edu.setInstitution(institution);
        edu.setLocation(location);
        edu.setStartYear(startYear);
        edu.setEndYear(endYear);
        edu.setDescription(description);
        return edu;
    }
}