# Portfolio - Build & Architecture Guide

Author: Adarsh Khare
Repo: https://github.com/adarshkhare2/adarshkhare
Live site: https://adarshkhare2.github.io/adarshkhare/

---

## 1. What this project is

A personal portfolio website with **two independent delivery modes** that share the
same source content:

1. **A full Java / Spring Boot web app** (dynamic). It has a database, an admin
   panel, a contact form that stores messages, and a server-side visitor counter.
   You run it locally (or on a server) to *edit* content.
2. **A static snapshot** of that app's rendered home page (plain HTML/CSS/JS with
   relative paths). This is what is published, for free and forever, on
   **GitHub Pages**.

The key idea: you maintain content in the Java app, then **export** the rendered
page into the `site/` folder and push it. GitHub Pages serves the `site/` folder.
There is no server required for the public site.

```
       YOU (author)                         VISITORS
           |                                   |
   run Java app locally                        |
   edit content via /admin                     |
           |                                   |
   export rendered page -> site/               |
           |                                   |
   git push  -------------------------->  GitHub Actions
                                              |
                                     publishes site/ to
                                     GitHub Pages (CDN)
                                              |
                                     https://adarshkhare2.github.io/adarshkhare/
```

---

## 2. Tech stack

| Layer            | Technology                                            |
|------------------|-------------------------------------------------------|
| Language         | Java 17                                               |
| Framework        | Spring Boot 3.3.5                                     |
| Templating       | Thymeleaf (+ thymeleaf-extras-springsecurity6)        |
| Persistence      | Spring Data JPA + Hibernate + H2 (file database)      |
| Security         | Spring Security (form login, BCrypt, in-memory admin) |
| Validation       | spring-boot-starter-validation                        |
| PDF handling     | Apache PDFBox 3.0.3 (certificate thumbnails)          |
| Build tool       | Maven                                                 |
| Frontend         | Hand-written HTML, CSS, vanilla JS (no framework)     |
| Hosting (public) | GitHub Pages via GitHub Actions                       |
| Hosting (app)    | Optional Docker image (Dockerfile + railway.json)     |
| Visitor counter  | Abacus free counting API (client-side, no backend)    |

Versions are pinned in `pom.xml`.

---

## 3. Directory map

```
D:\portfolio
|-- pom.xml                     Maven build + dependencies
|-- Dockerfile                  Optional container image for the Java app
|-- railway.json                Optional deploy config (Railway)
|-- .dockerignore               Files excluded from the Docker build context
|-- .gitignore                  Files excluded from git
|-- run-portfolio.cmd           Windows helper to run the built jar (logs to target\taskboot.log)
|
|-- data\                       H2 database files (portfolio-db.mv.db) - PERSISTED, committed
|-- uploads\                    Uploaded resume / photo / certificate PDFs + thumbnails - committed
|-- target\                     Maven build output (jar) - git-ignored
|
|-- src\main\java\com\portfolio
|   |-- PortfolioApplication.java          Spring Boot entry point
|   |-- config\
|   |   |-- SecurityConfig.java            Auth, login, headers, CSP
|   |   |-- DataInitializer.java           Seeds DB on first run if empty
|   |   |-- CertificateThumbnailInitializer.java  Generates PDF thumbnails with PDFBox
|   |-- controller\
|   |   |-- PublicController.java          Public pages + file endpoints + contact
|   |   |-- AdminController.java           Admin CRUD (behind login)
|   |-- model\                             JPA entities (Skill, Experience, Education,
|   |                                      Project, Certification, PersonalInfo,
|   |                                      ContactMessage, AppearanceSetting, VisitStat)
|   |-- repository\                        Spring Data repositories (one per entity)
|   |-- service\
|       |-- PortfolioService.java          Read/write content, visit counter
|       |-- FileStorageService.java        Save/load uploaded files
|       |-- CertificateThumbnailService.java  PDF -> PNG thumbnail
|
|-- src\main\resources
|   |-- application.properties            Port, DB, uploads, admin creds
|   |-- static\
|   |   |-- css\style.css, admin.css
|   |   |-- images\ (logo, og, apple-touch + tech\*.svg icons)
|   |   |-- js\script.js, experience.js, visitor.js, head.js, admin.js
|   |-- templates\
|       |-- index.html                    Public single-page site
|       |-- login.html, error.html
|       |-- admin\*.html                  Admin panel pages
|
|-- site\                       ** DEPLOYED STATIC SITE **
|   |-- index.html                        Exported + rewritten public page
|   |-- css\style.css
|   |-- images\ (same as static)
|   |-- js\script.js, experience.js, visitor.js, head.js
|   |-- certificates\ 1.pdf 1.png 2.* 3.* 33.* 65.* 66.*   Local cert files
|
|-- .github\workflows\deploy-pages.yml    GitHub Pages deploy workflow
```

---

## 4. Data model

All content lives in an H2 file database at `data/portfolio-db.mv.db`.

| Entity            | Purpose                                             |
|-------------------|-----------------------------------------------------|
| PersonalInfo      | Name, title, email, phone, location, summary, links, resume/photo filenames |
| Skill             | name, category, level %, displayOrder               |
| Experience        | role, company, location, startDate, endDate, current, description |
| Education         | degree, institution, location, startYear, endYear, description |
| Project           | title, description, techStack, link, repoLink, displayOrder, imageFilename |
| Certification     | title, issuer, year, description, credentialUrl, credentialFile, thumbnailFile |
| ContactMessage    | name, email, subject, message (submitted via the contact form) |
| AppearanceSetting | backgroundFilename (custom background image)       |
| VisitStat         | count (single-row server-side visit counter)        |

Uploaded files (certificate PDFs, thumbnails, resume, photo, background) are stored
in `uploads\` with random UUID filenames and referenced by name from the DB.

The `data\` and `uploads\` folders are committed to git on purpose, so the app has
its content on a fresh clone (and so the Docker image can seed itself).

### Seed data
`DataInitializer.java` runs at startup and only inserts rows **if a table is empty**.
It seeds personal info, 17 skills, 2 experiences, 1 education, 4 projects, and 4
certifications. Certificates (KloudGin x3, AWS, OOPs, Java) were added later and
have IDs 1, 2, 3, 33, 65, 66.

---

## 5. Backend flow

### Request lifecycle
1. Spring Boot starts, `DataInitializer` seeds the DB if empty.
2. `SecurityConfig` builds an in-memory `admin` user and the filter chain.
3. Public requests hit `PublicController`; admin requests require the ADMIN role.

### Public routes (`PublicController.java`)
| Route                     | What it does                                        |
|---------------------------|-----------------------------------------------------|
| `GET /`                   | Renders `index.html`. Increments visit counter once per HTTP session |
| `GET /login`              | Login page                                          |
| `POST /contact`           | Validates + stores a `ContactMessage`               |
| `GET /project-image/{id}` | Streams a project image from `uploads\`             |
| `GET /certificate/{id}`   | Streams the certificate PDF (inline)                |
| `GET /certificate-thumb/{id}` | Streams the thumbnail PNG                       |
| `GET /resume`             | Streams the resume PDF (attachment)                 |
| `GET /photo`              | Streams the profile photo                           |
| `GET /background`         | Streams the custom background image                 |
| `GET /robots.txt`         | SEO robots file (blocks /admin, /login, /h2-console)|
| `GET /sitemap.xml`        | SEO sitemap                                         |

`GET /` also computes derived values: experience duration text per job,
months-of-experience rounded to 1 decimal (`statYears`), project counts, and the
real visitor count.

### Admin routes (`AdminController.java`)
All under `/admin/**`, protected by login:
`/admin` (dashboard), `/admin/profile`, `/admin/appearance`, `/admin/skills`,
`/admin/experiences`, `/admin/educations`, `/admin/projects`,
`/admin/certifications`, `/admin/messages` - each with new/edit/save/delete
endpoints. Uploads go through `FileStorageService`; certificate PDFs get a
thumbnail via `CertificateThumbnailService` (PDFBox).

### Security (`SecurityConfig.java`)
- One in-memory ADMIN user. Username/password from `application.properties`
  (`portfolio.admin.username` / `portfolio.admin.password`), overridable by env vars.
- Passwords BCrypt-encoded.
- Permit-all: `/`, `/login`, `/css/**`, `/js/**`, `/images/**`, `/uploads/**`,
  `/resume`, `/photo`, `/project-image/**`, `/certificate/**`, `/certificate-thumb/**`.
- ADMIN only: `/admin/**`, `/h2-console/**`.
- Form login -> `/login`, success -> `/admin`.
- CSRF enabled (except `/h2-console`).
- Security headers set: `Content-Security-Policy` (strict, `script-src 'self'`),
  `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`,
  `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy`.

Note: the CSP is a **server header** on the Java app. GitHub Pages cannot set custom
headers, so the static site runs without CSP (which is why the client-side counter
can call Abacus).

---

## 6. Frontend

### Templates
- `index.html` - the whole public page: hero, about, "What I Do", skills, experience
  timeline, projects, certifications marquee + PDF modal, education, contact, footer.
- `login.html`, `error.html`, and `admin/*.html` for the panel.
- Thymeleaf (`th:*`) injects the DB content. `index.html` is the only template that
  also exists as a static export in `site/`.

### CSS (`static/css/style.css`)
Hand-written. Design tokens at `:root`:
```
--bg:#f7f8fc  --card:#ffffff  --line:#e2e6f2  --text:#10131a
--muted:#5b6478  --accent:#4f46e5  --accent2:#06b6d4  --on-accent:#ffffff
```
Light theme only. Uses `color-mix(in srgb, ...)`. Responsive breakpoints hide the
custom cursor / badges on small screens. `admin.css` is separate for the panel.

### JS modules (`static/js/`)
| File          | Responsibility |
|---------------|----------------|
| `head.js`     | Adds `.js` class early (progressive enhancement) |
| `script.js`   | Nav, scroll reveal (IntersectionObserver), word-reveal headings, timeline rail, parallax dot grid, stat counters, typewriter role, tilt cards, certificate PDF modal, scroll-to-top, custom cursor, marquees, preloader hide, toast, command palette (Ctrl/CMD+K) |
| `experience.js` | Auto-calculates "years of experience" from a fixed start date (see 7) |
| `visitor.js`  | Live visitor counter via Abacus (see 8) |
| `admin.js`    | Admin panel helpers |

No build step, no framework, no bundler. Files are loaded with `<script defer>` at
the end of `index.html`.

---

## 7. Dynamic feature: years-of-experience auto-calc (`experience.js`)

Why: so the displayed experience stays correct over time without a redeploy.

- `START = new Date(2022, 5, 1)` -> 2022-06-01 (month index 5 = June).
- `months = (yearDiff * 12) + monthDiff`, minus 1 if today's day < 1st.
- `years = round((months / 12) * 10) / 10` (one decimal).
- It then updates:
  - `.hero-sub strong` containing "years" -> `"X.Y+ years"`
  - the `.stat` whose label is "Years of Experience" (sets `data-target` so the
    count-up animation lands on the right number)
  - the contact meta row whose key matches "experience" -> `"X.Y+ Years"`

It runs on every page load in the browser. Today it shows ~4.3 and will become 4.4
automatically next month. **To change the start date, edit the `START` constant in
both `src\main\resources\static\js\experience.js` and `site\js\experience.js`.**

---

## 8. Dynamic feature: live visitor counter (`visitor.js`)

Why: a static site has no backend, so the server-side counter can't work. We use a
free, no-signup, CORS-enabled counting API instead.

- Provider: **Abacus** - `https://abacus.jasoncameron.dev`
- Counter identity: namespace `adarshkhare-site`, key `visits`.
- On load the script calls `GET /hit/adarshkhare-site/visits` (increments and returns
  `{"value": N}`) the first time per browser session, or `GET /get/...` afterwards.
  `sessionStorage` key `ak_visit_counted` ensures one increment per session.
- It writes the number into the contact "Visitors" row and the footer "N visitors".
- If the API is unreachable it fails silently and leaves the last value.

Endpoints used:
```
GET https://abacus.jasoncameron.dev/hit/adarshkhare-site/visits   -> {"value": N}   (increment)
GET https://abacus.jasoncameron.dev/get/adarshkhare-site/visits   -> {"value": N}   (read)
```

The counter was seeded at 143 (via `GET /create/adarshkhare-site/visits?initializer=143`
which returned an **admin key**). The admin key is stored privately (not in this repo)
- it is only needed to `set`/`reset`/`delete` the counter, e.g.:
```
POST /set/adarshkhare-site/visits?value=100    (Header: Authorization: Bearer <admin_key>)
```
Abacus is rate-limited to 30 requests / 10s per IP and free for this use.

---

## 9. CRITICAL: the UTF-8 -> CP437 "mojibake" gotcha

Symptom: special characters render as garbage, e.g. `ΓÇö` instead of `-` (em dash),
`┬╖` instead of `·`, `Γåæ` instead of the up arrow, `Γîÿ` instead of the command key.

Cause: when the static `site/index.html` was produced, UTF-8 bytes were decoded as
**CP437** (the Windows OEM code page used by the PowerShell console/pipe), then
written back as UTF-8. Every multi-byte glyph got double-encoded. This happens if
you capture a native program's UTF-8 output through a PowerShell pipeline
(`... | Out-File`, `>` redirection, `Invoke-WebRequest` piped to a file, etc.).

Rules to avoid it:
1. Never `Set-Content -Encoding UTF8` / `| Out-File` on HTML/JS templates.
2. Write files with an explicit no-BOM UTF-8 writer:
   ```powershell
   [IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding($false)))
   ```
3. When capturing native exe output, redirect with `cmd /c "exe ... 1>file 2>file"`
   rather than PowerShell pipes.

Repair an already-corrupted file (reverses the CP437 round-trip):
```powershell
$f="D:\portfolio\site\index.html"
$s=[IO.File]::ReadAllText($f,[Text.Encoding]::UTF8)
$bytes=[Text.Encoding]::GetEncoding(437).GetBytes($s)
[IO.File]::WriteAllText($f,[Text.Encoding]::UTF8.GetString($bytes),(New-Object System.Text.UTF8Encoding($false)))
```
Verify it worked: the file should contain real `-`/`·`/`*` glyphs and **no** `U+0393`
(Greek capital gamma, the tell-tale mojibake marker).

---

## 10. Build and run the Java app locally

Prerequisites: JDK 17+ (used: OpenJDK 25), Maven.

```powershell
# from D:\portfolio
& "C:\apache-maven-3.6.3\bin\mvn.cmd" -B clean package
# produces target\portfolio-1.0.0.jar
```

Run it:
```powershell
# simple
run-portfolio.cmd
# or directly
& "C:\Program Files\Java\OpenJDK 25.0.2\bin\java.exe" -jar target\portfolio-1.0.0.jar
```
Open http://localhost:8080 . Admin panel: http://localhost:8080/login
(default `admin` / `changeme`; override with env vars, see section 13).

On Windows this project is started via a scheduled task named `PortfolioApp`:
```powershell
schtasks /run   /tn "PortfolioApp"   # start
schtasks /end   /tn "PortfolioApp"   # stop
```
App log: `target\taskboot.log`.

---

## 11. The static export (how `site/` is produced)

There is **no script** for this; it is a manual, repeatable recipe. Do it whenever
content or front-end files change, then push.

1. Start the Java app and load `http://localhost:8080/` in a browser
   (or fetch it), then save the rendered HTML as `site/index.html`.
2. **Rewrite paths** for a folder that is served at a sub-path on GitHub Pages:
   - `href="/css/..."` -> `href="css/..."` and `src="/js/..."` -> `src="js/..."`
     and `/images/...` -> `images/...` (i.e. root-absolute -> relative).
   - `/certificate/{id}` -> `certificates/{id}.pdf`
   - `/certificate-thumb/{id}` -> `certificates/{id}.png`
3. **Copy assets** from `src\main\resources\static\` into `site\`:
   `css\`, `js\` (script.js, experience.js, visitor.js, head.js), `images\`.
4. **Copy certificate files** from `uploads\` into `site\certificates\` as
   `{id}.pdf` and `{id}.png`.
5. **Remove the CSRF token** hidden input from the contact form and make it a pure
   mailto form:
   ```html
   <form action="mailto:adarshkhare25032000@gmail.com" method="post" enctype="text/plain">
   ```
6. **Make `og:image` / `twitter:image`** relative (`images/og.png`).
7. **Add the dynamic scripts** before the closing body tag (order matters):
   ```html
   <script defer src="js/visitor.js"></script>
   <script defer src="js/experience.js"></script>
   <script defer src="js/script.js"></script>
   ```
8. Verify locally by opening `file:///D:/portfolio/site/index.html` and checking
   for missing assets and correct glyphs (see sections 9 and 12).

Why relative paths: a GitHub **project** page is served from
`/adarshkhare/`, not from the domain root, so `/css/style.css` would 404 while
`css/style.css` resolves correctly.

---

## 12. Deploy

### 12a. Public static site -> GitHub Pages (free, permanent)

Workflow: `.github/workflows/deploy-pages.yml`
```yaml
name: Deploy static site to GitHub Pages
on:
  push:
    branches: [main]
  workflow_dispatch:
permissions:
  contents: read
  pages: write
  id-token: write
concurrency:
  group: pages
  cancel-in-progress: true
jobs:
  deploy:
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/configure-pages@v5
        with: { enablement: true }
      - uses: actions/upload-pages-artifact@v3
        with: { path: site }
      - id: deployment
        uses: actions/deploy-pages@v4
```
So: **commit + push to `main` -> the `site/` folder is published automatically.**
No secrets required.

Publish procedure:
```powershell
cd D:\portfolio
git add -A
git commit -m "Update site"
git push origin main
```
Then wait ~1 minute and check https://adarshkhare2.github.io/adarshkhare/ .

### 12b. Optional: run the full Java app on a server
If you ever want the live admin panel online, build and run the Docker image
(`Dockerfile`, multi-stage: Maven build -> JRE runtime; seeds `data/` and
`uploads/`). `railway.json` is an example Railway config (healthcheck `/`). Note:
Railway/Render are **not** free-forever, which is why the public site uses Pages.

---

## 13. Environment variables

| Variable                    | Default     | Purpose                                  |
|-----------------------------|-------------|------------------------------------------|
| `PORT`                      | `8080`      | HTTP port (hosts usually inject this)    |
| `PORTFOLIO_ADMIN_USERNAME`  | `admin`     | Admin login user                         |
| `PORTFOLIO_ADMIN_PASSWORD`  | `changeme`  | Admin login password (change in prod!)   |
| `PORTFOLIO_UPLOAD_DIR`      | `./uploads` | Where uploaded files are stored          |

For the app, `server.port=${PORT:8080}`, H2 at `./data/portfolio-db`, uploads at
`./uploads` (see `application.properties`).

---

## 14. QA checklist (how to verify after a change)

Front-end / static site:
- [ ] All relative assets return 200 (css, js, images, certificates).
- [ ] Special glyphs correct: real `-`, `·`, arrows; **no `U+0393`** mojibake.
- [ ] No horizontal overflow at 390 / 768 / 1440 px widths.
- [ ] No broken `<img>` (`naturalWidth === 0`).
- [ ] Counter updates the "Visitors" row and the footer text.
- [ ] `experience.js` shows the correct "X.Y+ years".
- [ ] Browser console: no JS errors, no 404s, no CORS failures.

Backend (Java app):
- [ ] `mvn -B clean package` -> BUILD SUCCESS.
- [ ] `/`, `/robots.txt`, `/sitemap.xml`, `/css/**`, `/js/**`, `/login` -> 200.
- [ ] `/admin`, `/h2-console` without login -> 302 to `/login`.
- [ ] Login with correct creds -> `/admin`; wrong creds -> `/login?error`.
- [ ] All `/admin/*` pages -> 200 when logged in.
- [ ] `POST /contact` without CSRF token -> 403.
- [ ] Security headers present on `/`.

Useful one-liners:
```powershell
# security headers
(Invoke-WebRequest http://localhost:8080/ -UseBasicParsing).Headers

# CSRF rejection (expect 403)
try { Invoke-WebRequest http://localhost:8080/contact -Method Post -Body @{name='x';email='x@x.com';message='y'} -UseBasicParsing } catch { $_.Exception.Response.StatusCode.value__ }

# redirect check
curl.exe -s -o NUL -w "%{http_code} -> %{redirect_url}`n" http://localhost:8080/admin
```

---

## 15. Troubleshooting / FAQ

- **"Symbols look like garbage (`ΓÇö`, `┬╖`)."** -> CP437 mojibake. See section 9.
- **Assets 404 on the live site but work locally.** -> Paths are still root-absolute
  (`/css/...`). They must be relative (`css/...`). See section 11 step 2.
- **Counter stuck / wrong number.** -> Check `https://abacus.jasoncameron.dev/get/adarshkhare-site/visits`.
  Reseed with the private admin key via `/set`.
- **Years of experience wrong.** -> Edit `START` in `experience.js` (both copies).
- **Can I edit the live site without a computer?** -> No; the public site is static.
  Edit via the local Java admin panel, re-export `site/`, and push.
- **Is there an admin panel on the live site?** -> No. `/admin` only exists in the
  Java app when you run it.
- **App won't start / port in use.** -> Check `target\taskboot.log`; ensure no stale
  `java.exe` holds port 8080.
- **H2 console.** -> Disabled by default; set `spring.h2.console.enabled=true`
  locally. DB user `sa`, empty password.

---

## 16. Key git commits (history)

| Commit    | Change                                                        |
|-----------|---------------------------------------------------------------|
| `eb0e024` | Initial project                                               |
| `9d111d0` | GitHub Pages workflow + relative meta                         |
| `e76ab4a` | `experience.js` auto-calculated years                         |
| `293e7d9` | Auto-enable GitHub Pages in the workflow                      |
| `1b6ed1e` | Fix CP437 mojibake in `site/index.html` (restore glyphs)      |
| `f2408c5` | Add Abacus live visitor counter (`visitor.js`)                |
| `f662a2e` | Mirror `visitor.js` into app static resources                 |

---

## 17. Daily workflow recap

1. Start the Java app: `schtasks /run /tn "PortfolioApp"` (or `run-portfolio.cmd`).
2. Edit content at http://localhost:8080/login -> `/admin`.
3. Export the rendered home page into `site/` (section 11).
4. Sanity-check `file:///D:/portfolio/site/index.html` (assets, glyphs, counter).
5. `git add -A && git commit -m "..." && git push origin main`.
6. Wait ~1 min, verify https://adarshkhare2.github.io/adarshkhare/ .

That's the entire flow. The public site costs nothing and never expires.
