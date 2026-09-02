(function () {
  function updateExternalLinks() {
    document.querySelectorAll('a[href^="http://"], a[href^="https://"]').forEach(function (link) {
      if (link.origin === window.location.origin) return;
      link.target = "_blank";
      link.rel = "noopener";
    });
  }

  if (typeof document$ !== "undefined") {
    document$.subscribe(updateExternalLinks);
  } else {
    document.addEventListener("DOMContentLoaded", updateExternalLinks);
  }
})();
