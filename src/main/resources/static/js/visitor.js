(function () {
  var NS = 'adarshkhare-site';
  var KEY = 'visits';
  var API = 'https://abacus.jasoncameron.dev/';
  var seen = false;
  try { seen = sessionStorage.getItem('ak_visit_counted') === '1'; } catch (e) {}

  var url = API + (seen ? 'get' : 'hit') + '/' + NS + '/' + KEY + '?_=' + Date.now();

  function fmt(n) {
    try { return Number(n).toLocaleString('en-US'); } catch (e) { return String(n); }
  }

  function apply(n) {
    var txt = fmt(n);
    var items = document.querySelectorAll('.contact-meta-item');
    for (var i = 0; i < items.length; i++) {
      var k = items[i].querySelector('.contact-k');
      if (k && /visitors/i.test(k.textContent)) {
        var v = items[i].querySelector('.contact-v');
        if (v) { v.textContent = txt; }
      }
    }
    var spans = document.querySelectorAll('.footer-meta span');
    for (var j = 0; j < spans.length; j++) {
      if (/visitors/i.test(spans[j].textContent)) {
        spans[j].textContent = spans[j].textContent.replace(/[\d,]+(\s*visitors)/i, txt + '$1');
      }
    }
  }

  if (!window.fetch) { return; }

  fetch(url, { cache: 'no-store' })
    .then(function (r) { return r.ok ? r.json() : null; })
    .then(function (d) {
      if (!d || d.value == null) { return; }
      apply(d.value);
      if (!seen) { try { sessionStorage.setItem('ak_visit_counted', '1'); } catch (e) {} }
    })
    .catch(function () {});
})();
