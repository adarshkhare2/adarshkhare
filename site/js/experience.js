(function () {
  // Earliest professional experience start date (year, monthIndex (0=Jan), day).
  // Keep this in sync with the earliest "experience" record so the displayed
  // years-of-experience stays accurate over time without redeploying.
  var START = new Date(2022, 5, 1); // 2022-06-01

  var now = new Date();
  var months = (now.getFullYear() - START.getFullYear()) * 12 + (now.getMonth() - START.getMonth());
  if (now.getDate() < START.getDate()) {
    months -= 1;
  }
  if (months <= 0) {
    return;
  }

  var years = Math.round((months / 12) * 10) / 10;
  var text = String(years);
  var decimals = (years % 1 === 0) ? '0' : '1';

  document.querySelectorAll('.hero-sub strong').forEach(function (el) {
    if (/years/i.test(el.textContent)) {
      el.textContent = text + '+ years';
    }
  });

  document.querySelectorAll('.stat').forEach(function (stat) {
    var label = stat.querySelector('.stat-l');
    if (label && /years of experience/i.test(label.textContent)) {
      var v = stat.querySelector('.stat-v');
      if (v) {
        v.setAttribute('data-target', text);
        v.setAttribute('data-decimals', decimals);
        var span = v.querySelector('span');
        if (span) {
          span.textContent = text;
        }
      }
    }
  });

  document.querySelectorAll('.contact-meta-item').forEach(function (item) {
    var k = item.querySelector('.contact-k');
    var v = item.querySelector('.contact-v');
    if (k && v && /experience/i.test(k.textContent)) {
      v.textContent = text + '+ Years';
    }
  });
})();
