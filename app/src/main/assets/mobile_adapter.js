// Clean Helper Script - Tanpa loop agresif
(function() {
    function cleanUI() {
        try {
            // Sembunyikan banner download desktop jika ada
            var banner = document.querySelector('header._ak8j');
            if (banner) {
                banner.style.display = 'none';
            }
        } catch(e) {}
    }

    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        cleanUI();
    } else {
        window.addEventListener('DOMContentLoaded', cleanUI);
    }
})();
