(function () {
  'use strict';

  document.documentElement.classList.add('js');

  /* ---------- Nav state ---------- */
  var nav = document.getElementById('nav');
  var progress = document.getElementById('scrollProgress');
  function onScroll() {
    if (window.scrollY > 30) { nav.classList.add('scrolled'); } else { nav.classList.remove('scrolled'); }
    if (scrollTop) { scrollTop.classList.toggle('show', window.scrollY > 500); }
    if (progress) {
      var h = document.documentElement.scrollHeight - window.innerHeight;
      progress.style.transform = 'scaleX(' + (h > 0 ? Math.min(1, window.scrollY / h) : 0) + ')';
    }
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  /* ---------- Mobile nav toggle ---------- */
  var navToggle = document.getElementById('navToggle');
  if (navToggle) {
    navToggle.addEventListener('click', function () {
      var open = nav.classList.toggle('open');
      navToggle.setAttribute('aria-expanded', String(open));
    });
    nav.querySelectorAll('a').forEach(function (a) {
      a.addEventListener('click', function () {
        nav.classList.remove('open');
        navToggle.setAttribute('aria-expanded', 'false');
      });
    });
  }

  /* ---------- Active link on scroll ---------- */
  var links = document.querySelectorAll('.nav-links a[href^="#"]');
  var sections = [];
  links.forEach(function (l) {
    var s = document.querySelector(l.getAttribute('href'));
    if (s) { sections.push({ link: l, sec: s }); }
  });
  window.addEventListener('scroll', function () {
    var pos = window.scrollY + 160;
    var cur = null;
    sections.forEach(function (it) { if (it.sec.offsetTop <= pos) { cur = it; } });
    sections.forEach(function (it) { it.link.classList.toggle('active', it === cur); });
  }, { passive: true });

  /* ---------- Scroll reveal ---------- */
  var rv = document.querySelectorAll('.rv:not(.on)');
  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('on');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12 });
    rv.forEach(function (el) { io.observe(el); });
  } else {
    rv.forEach(function (el) { el.classList.add('on'); });
  }

  /* ---------- Word-reveal headings ---------- */
  document.querySelectorAll('.sec-label').forEach(function (el) {
    var words = el.textContent.trim().split(/\s+/);
    el.innerHTML = words.map(function (w, i) {
      return '<span class="wr" style="--i:' + i + '">' + w + '</span>';
    }).join(' ');
  });

  /* ---------- Animated timeline rail ---------- */
  var timelines = document.querySelectorAll('.timeline');
  if ('IntersectionObserver' in window) {
    var tio = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('on'); tio.unobserve(e.target); } });
    }, { threshold: 0.15 });
    timelines.forEach(function (t) { tio.observe(t); });
  } else {
    timelines.forEach(function (t) { t.classList.add('on'); });
  }

  /* ---------- Timeline rail draws with scroll ---------- */
  (function () {
    var tl = document.querySelector('.timeline');
    if (!tl || window.matchMedia('(prefers-reduced-motion: reduce)').matches) { return; }
    var ticking = false;
    function run() {
      var r = tl.getBoundingClientRect();
      var vh = window.innerHeight;
      var start = vh * 0.85;
      var p = (start - r.top) / (r.height + start - vh * 0.15);
      p = Math.max(0, Math.min(1, p));
      tl.style.setProperty('--tl', p.toFixed(3));
      ticking = false;
    }
    window.addEventListener('scroll', function () {
      if (!ticking) { ticking = true; requestAnimationFrame(run); }
    }, { passive: true });
    window.addEventListener('resize', run);
    run();
  })();

  /* ---------- Parallax dot-grid background ---------- */
  (function () {
    var grid = document.getElementById('bgGrid');
    if (!grid || window.matchMedia('(prefers-reduced-motion: reduce)').matches) { return; }
    var ticking = false;
    function run() {
      grid.style.transform = 'translate3d(0,' + (window.scrollY * -0.05).toFixed(1) + 'px,0)';
      ticking = false;
    }
    window.addEventListener('scroll', function () {
      if (!ticking) { ticking = true; requestAnimationFrame(run); }
    }, { passive: true });
    run();
  })();

  /* ---------- Stat counters ---------- */
  var counters = document.querySelectorAll('.stat-v[data-target]');
  function animate(el) {
    var target = parseFloat(el.getAttribute('data-target'));
    var decimals = parseInt(el.getAttribute('data-decimals') || '0', 10);
    if (isNaN(target)) return;
    var span = el.querySelector('span');
    if (!span) return;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      span.textContent = target.toFixed(decimals);
      return;
    }
    var duration = 1200, start = null;
    function step(ts) {
      if (start === null) start = ts;
      var p = Math.min(1, (ts - start) / duration);
      var eased = 1 - Math.pow(1 - p, 3);
      span.textContent = (target * eased).toFixed(decimals);
      if (p < 1) { requestAnimationFrame(step); }
    }
    requestAnimationFrame(step);
  }
  if ('IntersectionObserver' in window) {
    var cio = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) { animate(entry.target); cio.unobserve(entry.target); }
      });
    }, { threshold: 0.4 });
    counters.forEach(function (el) { cio.observe(el); });
  } else {
    counters.forEach(animate);
  }

  /* ---------- Typewriter role ---------- */
  var typed = document.querySelector('.typed');
  if (typed) {
    var roles = (typed.getAttribute('data-roles') || typed.textContent).split('|').filter(Boolean);
    if (roles.length > 1 && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      var ri = 0, ci = 0, deleting = false;
      (function type() {
        var word = roles[ri];
        ci += deleting ? -1 : 1;
        typed.textContent = word.slice(0, ci);
        var delay = deleting ? 45 : 85;
        if (!deleting && ci === word.length) { delay = 1600; deleting = true; }
        else if (deleting && ci === 0) { deleting = false; ri = (ri + 1) % roles.length; delay = 350; }
        setTimeout(type, delay);
      })();
    }
  }

  /* ---------- Tilt cards ---------- */
  if (window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
    document.querySelectorAll('[data-tilt]').forEach(function (el) {
      el.addEventListener('mousemove', function (e) {
        var r = el.getBoundingClientRect();
        var px = (e.clientX - r.left) / r.width - 0.5;
        var py = (e.clientY - r.top) / r.height - 0.5;
        el.style.transform = 'perspective(750px) rotateY(' + (px * 8).toFixed(2) + 'deg) rotateX(' + (-py * 8).toFixed(2) + 'deg)';
      });
      el.addEventListener('mouseleave', function () { el.style.transform = ''; });
    });
  }

  /* ---------- Certificate PDF modal ---------- */
  var certModal = document.getElementById('certModal');
  if (certModal) {
    var certFrame = document.getElementById('certModalFrame');
    var certTitle = document.getElementById('certModalTitle');
    var certDownload = document.getElementById('certModalDownload');
    function openCert(src, title) {
      certTitle.textContent = title || 'Certificate';
      certDownload.setAttribute('href', src);
      certModal.classList.remove('loaded');
      certFrame.setAttribute('src', src);
      certModal.classList.add('open');
      certModal.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
    }
    function closeCert() {
      certModal.classList.remove('open');
      certModal.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';
      window.setTimeout(function () {
        certFrame.setAttribute('src', 'about:blank');
        certModal.classList.remove('loaded');
      }, 320);
    }
    if (certFrame) { certFrame.addEventListener('load', function () { if (certModal.classList.contains('open')) { certModal.classList.add('loaded'); } }); }
    document.addEventListener('click', function (e) {
      var btn = e.target.closest ? e.target.closest('[data-cert-src]') : null;
      if (btn) { openCert(btn.getAttribute('data-cert-src'), btn.getAttribute('data-cert-title')); }
    });
    certModal.querySelectorAll('[data-cert-close]').forEach(function (el) {
      el.addEventListener('click', closeCert);
    });
    var certCloseBtn = document.getElementById('certModalClose');
    if (certCloseBtn) { certCloseBtn.addEventListener('click', closeCert); }
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && certModal.classList.contains('open')) { closeCert(); }
    });
  }

  /* ---------- Scroll to top ---------- */
  var scrollTop = document.getElementById('scrollTop');
  if (scrollTop) {
    scrollTop.addEventListener('click', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); });
  }

  /* ---------- Cursor glow + custom cursor (desktop only) ---------- */
  var finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
  if (finePointer) {
    document.documentElement.classList.add('cv-on');
    var glow = document.getElementById('cvGlow');
    var dot = document.getElementById('cvDot');
    var ring = document.getElementById('cvRing');
    if (glow && dot && ring) {
      var mx = window.innerWidth / 2, my = window.innerHeight / 2;
      var gx = mx, gy = my, rx = mx, ry = my;
      var idle = 0;
      document.addEventListener('mousemove', function (e) {
        mx = e.clientX; my = e.clientY;
        idle = 0;
        if (dot) { dot.style.left = mx + 'px'; dot.style.top = my + 'px'; }
      }, { passive: true });
      var interactive = document.querySelectorAll('a, button, .work, .skill-row, .tag, .service, .cert-card');
      interactive.forEach(function (el) {
        el.addEventListener('mouseenter', function () { if (ring) ring.classList.add('grow'); });
        el.addEventListener('mouseleave', function () { if (ring) ring.classList.remove('grow'); });
      });
      function cursorLoop() {
        idle++;
        var decay = idle > 90 ? 0.02 : 0.09;
        gx += (mx - gx) * decay; gy += (my - gy) * decay;
        rx += (mx - rx) * 0.18; ry += (my - ry) * 0.18;
        if (glow) { glow.style.transform = 'translate(' + gx + 'px,' + gy + 'px) translate(-50%,-50%)'; }
        if (ring) { ring.style.left = rx + 'px'; ring.style.top = ry + 'px'; }
        requestAnimationFrame(cursorLoop);
      }
      cursorLoop();
    }
  }

  /* ---------- Magnetic buttons ---------- */
  if (finePointer) {
    document.querySelectorAll('.btn').forEach(function (el) {
      el.addEventListener('mousemove', function (e) {
        var r = el.getBoundingClientRect();
        var x = ((e.clientX - r.left) / r.width - 0.5) * 14;
        var y = ((e.clientY - r.top) / r.height - 0.5) * 10;
        el.style.transform = 'translate(' + x.toFixed(1) + 'px,' + (y - 2).toFixed(1) + 'px)';
      });
      el.addEventListener('mouseleave', function () { el.style.transform = ''; });
    });
  }

  /* ---------- Card cursor spotlight ---------- */
  if (finePointer) {
    document.querySelectorAll('.glow-hover').forEach(function (el) {
      el.addEventListener('mousemove', function (e) {
        var r = el.getBoundingClientRect();
        el.style.setProperty('--mx', (e.clientX - r.left) + 'px');
        el.style.setProperty('--my', (e.clientY - r.top) + 'px');
      });
    });
  }

  /* ---------- Certifications marquee ---------- */
  (function () {
    var track = document.querySelector('.cert-track');
    var set = track && track.querySelector('.cert-set');
    if (!track || !set) { return; }
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) { return; }
    if (set.querySelectorAll('.cert-slide').length < 2) { return; }
    var clone = set.cloneNode(true);
    clone.setAttribute('aria-hidden', 'true');
    clone.querySelectorAll('a, button').forEach(function (el) { el.setAttribute('tabindex', '-1'); });
    track.appendChild(clone);
    track.classList.add('is-duplicated');
  })();

  /* ---------- Parallax depth ---------- */
  if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    var pxEls = document.querySelectorAll('[data-parallax]');
    if (pxEls.length) {
      var ticking = false;
      var runParallax = function () {
        var vh = window.innerHeight;
        pxEls.forEach(function (el) {
          var r = el.getBoundingClientRect();
          if (r.bottom < -240 || r.top > vh + 240) { return; }
          var speed = parseFloat(el.getAttribute('data-parallax')) || 0.1;
          var offset = (r.top + r.height / 2 - vh / 2) * speed;
          el.style.transform = 'translateY(' + offset.toFixed(1) + 'px)';
        });
        ticking = false;
      };
      window.addEventListener('scroll', function () {
        if (!ticking) { ticking = true; requestAnimationFrame(runParallax); }
      }, { passive: true });
      window.addEventListener('resize', runParallax, { passive: true });
      runParallax();
    }
  }

  /* ---------- Page load fade-in ---------- */
  function markLoaded() { document.body.classList.add('loaded'); }
  if (document.readyState !== 'loading') { markLoaded(); }
  else { document.addEventListener('DOMContentLoaded', markLoaded); }

  /* ---------- Preloader ---------- */
  var preloader = document.getElementById('preloader');
  if (preloader) {
    var hidePreloader = function () {
      preloader.classList.add('done');
      window.setTimeout(function () { if (preloader.parentNode) { preloader.parentNode.removeChild(preloader); } }, 600);
    };
    if (document.readyState === 'complete') { window.setTimeout(hidePreloader, 250); }
    else { window.addEventListener('load', function () { window.setTimeout(hidePreloader, 250); }); }
  }

  /* ---------- Toast ---------- */
  var toastEl = document.getElementById('toast');
  var toastTimer = null;
  function showToast(msg) {
    if (!toastEl) { return; }
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(function () { toastEl.classList.remove('show'); }, 2200);
  }

  /* ---------- Copy email ---------- */
  var copyBtn = document.getElementById('copyEmail');
  if (copyBtn) {
    copyBtn.addEventListener('click', function () {
      var email = copyBtn.getAttribute('data-email') || '';
      var done = function () { showToast('Email copied: ' + email); };
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(email).then(done).catch(function () { showToast(email); });
      } else {
        var tmp = document.createElement('textarea');
        tmp.value = email; document.body.appendChild(tmp); tmp.select();
        try { document.execCommand('copy'); done(); } catch (e) { showToast(email); }
        document.body.removeChild(tmp);
      }
    });
  }

  /* ---------- Command palette ---------- */
  var cmdk = document.getElementById('cmdk');
  var cmdkBtn = document.getElementById('cmdkBtn');
  var cmdkInput = document.getElementById('cmdkInput');
  var cmdkList = document.getElementById('cmdkList');
  if (cmdk && cmdkList) {
    var items = [
      { ic: '§', label: 'About', sub: 'Section', act: function () { go('#about'); } },
      { ic: '§', label: 'What I Do', sub: 'Section', act: function () { go('#what'); } },
      { ic: '§', label: 'Skills & Tools', sub: 'Section', act: function () { go('#stack'); } },
      { ic: '§', label: 'Experience', sub: 'Section', act: function () { go('#experience'); } },
      { ic: '§', label: 'My Work', sub: 'Section', act: function () { go('#work'); } },
      { ic: '§', label: 'Certifications', sub: 'Section', act: function () { go('#certifications'); } },
      { ic: '§', label: 'Education', sub: 'Section', act: function () { go('#education'); } },
      { ic: '§', label: 'Contact', sub: 'Section', act: function () { go('#contact'); } },
      { ic: '✉', label: 'Copy email address', sub: 'Action', act: function () { if (copyBtn) { copyBtn.click(); } } },
      { ic: '↓', label: 'Download resume', sub: 'Action', act: function () { window.location.href = '/resume'; } },
      { ic: '↑', label: 'Back to top', sub: 'Action', act: function () { window.scrollTo({ top: 0, behavior: 'smooth' }); } },
      { ic: 'in', label: 'LinkedIn', sub: 'Social', act: function () { openFirst('LinkedIn'); } },
      { ic: 'gh', label: 'GitHub', sub: 'Social', act: function () { openFirst('GitHub'); } },
      { ic: 'ig', label: 'Instagram', sub: 'Social', act: function () { openFirst('Instagram'); } }
    ];
    function go(sel) { var el = document.querySelector(sel); if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'start' }); } }
    function openFirst(label) {
      var el = document.querySelector('a[aria-label="' + label + '"]') ||
               document.querySelector('a[title="' + label + '"]');
      if (el) { window.open(el.getAttribute('href'), '_blank', 'noopener'); }
    }
    var rendered = items.slice();
    var activeIdx = 0;
    function render(filter) {
      var q = (filter || '').toLowerCase().trim();
      rendered = items.filter(function (it) { return !q || (it.label + ' ' + it.sub).toLowerCase().indexOf(q) >= 0; });
      activeIdx = 0;
      if (!rendered.length) { cmdkList.innerHTML = '<li class="cmdk-empty">No matches</li>'; return; }
      cmdkList.innerHTML = rendered.map(function (it, i) {
        return '<li class="cmdk-item' + (i === 0 ? ' active' : '') + '" data-idx="' + i + '">'
          + '<span class="ci-ic" aria-hidden="true">' + it.ic + '</span>'
          + '<span>' + it.label + '</span>'
          + '<span class="ci-sub">' + it.sub + '</span></li>';
      }).join('');
    }
    function setActive(i) {
      var nodes = cmdkList.querySelectorAll('.cmdk-item');
      if (!nodes.length) { return; }
      activeIdx = (i + nodes.length) % nodes.length;
      nodes.forEach(function (n, k) { n.classList.toggle('active', k === activeIdx); });
      nodes[activeIdx].scrollIntoView({ block: 'nearest' });
    }
    function run(idx) {
      var it = rendered[idx];
      if (!it) { return; }
      closeCmdk();
      window.setTimeout(function () { it.act(); }, 60);
    }
    function openCmdk() {
      cmdk.classList.add('open');
      cmdk.setAttribute('aria-hidden', 'false');
      cmdkInput.value = '';
      render('');
      document.body.style.overflow = 'hidden';
      window.setTimeout(function () { cmdkInput.focus(); }, 30);
    }
    function closeCmdk() {
      cmdk.classList.remove('open');
      cmdk.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';
    }
    render('');
    window.cmdkOpen = openCmdk;
    if (cmdkBtn) { cmdkBtn.addEventListener('click', openCmdk); }
    cmdk.querySelectorAll('[data-cmdk-close]').forEach(function (el) { el.addEventListener('click', closeCmdk); });
    cmdkInput.addEventListener('input', function () { render(cmdkInput.value); });
    cmdkInput.addEventListener('keydown', function (e) {
      if (e.key === 'ArrowDown') { e.preventDefault(); setActive(activeIdx + 1); }
      else if (e.key === 'ArrowUp') { e.preventDefault(); setActive(activeIdx - 1); }
      else if (e.key === 'Enter') { e.preventDefault(); run(activeIdx); }
      else if (e.key === 'Escape') { closeCmdk(); }
    });
    cmdkList.addEventListener('click', function (e) {
      var li = e.target.closest ? e.target.closest('.cmdk-item') : null;
      if (li) { run(parseInt(li.getAttribute('data-idx'), 10)); }
    });
    document.addEventListener('keydown', function (e) {
      if ((e.metaKey || e.ctrlKey) && (e.key === 'k' || e.key === 'K')) {
        e.preventDefault();
        if (cmdk.classList.contains('open')) { closeCmdk(); } else { openCmdk(); }
      }
    });
  }
})();