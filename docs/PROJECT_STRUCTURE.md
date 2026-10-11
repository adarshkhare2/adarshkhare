# Portfolio Project - File & Folder Guide

A complete summary of what every file and folder in the project does.

Project root: `D:\portfolio`
Live site: https://adarshkhare2.github.io/adarshkhare/

---

## 1. Top-level folders

| Folder | What it is |
|--------|------------|
| `src\` | All application source code: Java classes, config, templates, static assets |
| `site\` | The **deployed static website** (published to GitHub Pages). A standalone copy of the rendered page with relative paths |
| `data\` | The H2 database files that store all site content |
| `uploads\` | User-uploaded files (certificate PDFs + thumbnails, project images, resume, photo, background) |
| `target\` | Maven build output (the runnable jar). Git-ignored - safe to delete |
| `.github\` | GitHub configuration, including the deploy workflow |

---

## 2. Root-level files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven project file. Declares Spring Boot 3.3.5, Java 17, and dependencies (Web, Thymeleaf, Data JPA, Security, Validation, thymeleaf-extras-springsecurity6, PDFBox 3.0.3). Defines the build and the `portfolio-1.0.0.jar` artifact |
| `Dockerfile` | Optional multi-stage container build for the Java app: stage 1 compiles with Maven, stage 2 runs the jar on a JRE image and copies in `data/` and `uploads/` |
| `.dockerignore` | Tells Docker which paths to skip when building the image (`target`, `site`, `.git`, `.github`, IDE files, logs, H2 lock file) |
| `.gitignore` | Tells git what not to commit (`target/`, `*.log`, `data/*.lock.db`, IDE/OS files). Note: `data/`, `uploads/`, and `site/` **are** committed on purpose |
| `railway.json` | Optional Railway deploy config (uses the Dockerfile, health-checks `/`, restarts on failure) |
| `run-portfolio.cmd` | Windows helper: runs the built jar and writes output to `target\taskboot.log`. Used by the local `PortfolioApp` scheduled task |
| `BUILD.md` | The build & architecture guide (how the project is built, run, exported, and deployed) |
| `INTERVIEW.md` | Interview talking-points document (pitch, design decisions, STAR stories, Q&A) |
| `PROJECT_STRUCTURE.md` | This file - describes every file and folder |

---

## 3. `.github\workflows\deploy-pages.yml`

GitHub Actions workflow that publishes the site. On every push to `main` (or a manual
run) it checks out the repo, configures GitHub Pages, uploads the `site\` folder as
the Pages artifact, and deploys it. This is the entire CI/CD pipeline - no secrets.

---

## 4. `data\` - the database

| File | Purpose |
|------|---------|
| `portfolio-db.mv.db` | The H2 database file holding all content: personal info, skills, experience, education, projects, certifications, contact messages, appearance settings, and the server-side visit counter |
| `portfolio-db.lock.db` | Runtime lock file created while the app is running. Git-ignored; deleted safely when the app stops |

Committed so a fresh clone has content. Seeded by `DataInitializer` only when empty.

---

## 5. `uploads\` - stored files

Contains files stored under random UUID names (the original names are kept only in the
database). These include:

- Certificate PDFs and their generated PNG thumbnails
- Project images
- Profile photo and resume (only present after you upload them; until then `/photo`
  and `/resume` return 404)

The `FileStorageService` writes here and `PublicController` streams from here.

---

## 6. `site\` - the deployed static website

This is what the world sees on GitHub Pages. It is a self-contained copy of the app's
home page, rewritten to use relative paths so it works from a sub-path.

| Path | Purpose |
|------|---------|
| `site\index.html` | The public page (hero, about, skills, experience, projects, certifications, education, contact) |
| `site\css\style.css` | Public site styles |
| `site\js\script.js` | All UI interactivity (nav, reveal animations, counters, modal, command palette, etc.) |
| `site\js\experience.js` | Auto-calculates the displayed "years of experience" from a fixed start date |
| `site\js\visitor.js` | Live visitor counter (calls the Abacus API - works without a backend) |
| `site\js\head.js` | Adds a `.js` class early for progressive enhancement |
| `site\images\logo.svg` | Site logo |
| `site\images\og.png` | Social sharing (Open Graph / Twitter) preview image |
| `site\images\apple-touch-icon.png` | Icon for iOS home-screen bookmarks |
| `site\images\tech\*.svg` | Technology logos used in the skills/about section (java.svg, spring.svg, aws.svg, docker.svg, kafka.svg, mongodb.svg, redis.svg, kubernetes.svg, git.svg, jenkins.svg, maven.svg, mysql.svg, postgresql.svg, python.svg, hibernate.svg, html5.svg, css3.svg, javascript.svg) |
| `site\certificates\1.pdf`, `1.png` | Certificate file + preview thumbnail (one pair per certificate: 1, 2, 3, 33, 65, 66) |

---

## 7. `src\main\java\com\portfolio` - the Java code

### Root class
| File | Purpose |
|------|---------|
| `PortfolioApplication.java` | The Spring Boot entry point (`main` method). Starts the whole application |

### `config\` - startup configuration
| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Security setup: the in-memory ADMIN user (BCrypt), which URLs are public vs protected, form login, CSRF, and security headers including the strict Content-Security-Policy |
| `DataInitializer.java` | Runs at startup and seeds the database **only if a table is empty** (personal info, 17 skills, 2 experiences, 1 education, 4 projects, 4 certifications, a visit counter) |
| `CertificateThumbnailInitializer.java` | Runs at startup and generates a PNG thumbnail for any certificate that has a PDF but no thumbnail yet |

### `controller\` - web endpoints
| File | Purpose |
|------|---------|
| `PublicController.java` | Public requests: renders `/` (and counts the visit once per session), `/login`, `POST /contact`, streams `/project-image/{id}`, `/certificate/{id}`, `/certificate-thumb/{id}`, `/resume`, `/photo`, `/background`, and serves `/robots.txt` and `/sitemap.xml` |
| `AdminController.java` | The whole `/admin/**` panel behind login: dashboard, profile, appearance, skills, experiences, educations, projects, certifications, and messages - each with list, new, edit, save, delete, and file-upload actions |

### `model\` - JPA entities (database tables)
| File | Purpose |
|------|---------|
| `PersonalInfo.java` | Name, title, email, phone, location, summary, social links, resume + photo filenames |
| `Skill.java` | Skill name, category, level %, display order |
| `Experience.java` | Role, company, location, start/end dates, `current` flag, description |
| `Education.java` | Degree, institution, location, start/end year, description |
| `Project.java` | Title, description, tech stack, links, display order, image filename |
| `Certification.java` | Title, issuer, year, description, credential URL, PDF filename, thumbnail filename |
| `ContactMessage.java` | A message from the contact form (name, email, subject, message, read flag, created time) |
| `AppearanceSetting.java` | Site appearance - currently the custom background image filename |
| `VisitStat.java` | A single-row counter for the server-side visit count |

### `repository\` - Spring Data JPA interfaces
One repository per entity, each providing CRUD plus custom finder methods used for
ordering (`SkillRepository`, `ExperienceRepository`, `EducationRepository`,
`ProjectRepository`, `CertificationRepository`, `ContactMessageRepository`,
`PersonalInfoRepository`, `AppearanceRepository`, `VisitStatRepository`).

### `service\` - business logic
| File | Purpose |
|------|---------|
| `PortfolioService.java` | The single content service. Read/write methods for every entity, the visit counter, contact messages, and unread-message count. Marked `@Transactional` |
| `FileStorageService.java` | Saves uploaded files to the upload folder under random UUID names, and loads/deletes them. Blocks invalid filenames |
| `CertificateThumbnailService.java` | Renders page 1 of a certificate PDF to a PNG (110 DPI) using PDFBox, for the preview thumbnails |

---

## 8. `src\main\resources` - configuration, static assets, templates

### `application.properties`
Main Spring config: server port (`${PORT:8080}`), H2 file database location, upload
directory, admin credentials (env-overridable), multipart limits (20MB/25MB), error
handling (never expose stack traces), and Thymeleaf cache settings.

### `static\` - assets served directly
| Path | Purpose |
|------|---------|
| `static\css\style.css` | Public site styles (same file that is copied to `site\css`) |
| `static\css\admin.css` | Styles for the admin panel only |
| `static\js\script.js`, `experience.js`, `visitor.js`, `head.js` | Public site scripts (copied to `site\js`) |
| `static\js\admin.js` | Admin panel helper script |
| `static\images\...` | Logo, OG image, apple-touch icon, and `tech\*.svg` logos (copied to `site\images`) |

### `templates\` - Thymeleaf views
| File | Purpose |
|------|---------|
| `index.html` | The public single-page site (the source that is exported into `site\index.html`) |
| `login.html` | Admin login page |
| `error.html` | Custom error page (hides the default whitelabel page) |
| `admin\dashboard.html` | Admin home with counts and quick links |
| `admin\profile.html` | Edit personal info; upload photo and resume |
| `admin\appearance.html` | Upload/remove the custom background image |
| `admin\skills.html` / `admin\skill-form.html` | List skills / add-edit a skill |
| `admin\experiences.html` / `admin\experience-form.html` | List experience / add-edit an experience |
| `admin\educations.html` / `admin\education-form.html` | List education / add-edit an education |
| `admin\projects.html` / `admin\project-form.html` | List projects / add-edit a project, including its image |
| `admin\certifications.html` / `admin\certification-form.html` | List certifications / add-edit a certification, including its PDF |
| `admin\messages.html` | View, mark-read, and delete contact messages |
| `admin\fragments.html` | Shared Thymeleaf pieces (head, sidebar navigation, flash messages) reused by every admin page |

---

## 9. How it fits together (quick recap)

1. `PortfolioApplication` starts Spring Boot; `DataInitializer` seeds an empty DB and
   `CertificateThumbnailInitializer` generates missing PDF thumbnails.
2. `SecurityConfig` locks `/admin/**` behind login and sets security headers.
3. `PublicController` renders `templates\index.html` from data provided by
   `PortfolioService`, and streams uploaded files from `uploads\`.
4. `AdminController` lets you edit that data and upload files via the panel; files land
   in `uploads\` through `FileStorageService`.
5. Content lives in `data\portfolio-db.mv.db`.
6. The rendered page is exported into `site\` and pushed; the GitHub Actions workflow
   publishes `site\` to GitHub Pages.
