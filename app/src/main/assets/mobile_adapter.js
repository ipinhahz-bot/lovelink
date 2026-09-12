// Auto-Fit & Mobile UI for WhatsApp Web
(function() {
    function fitScreen() {
        try {
            // 1. Sembunyikan banner download desktop WhatsApp untuk Windows
            var banners = document.querySelectorAll('header._ak8j, [data-testid="intro-md-beta-logo-light"], div[class*="banner"]');
            for (var i = 0; i < banners.length; i++) {
                banners[i].style.display = 'none';
            }

            // 2. Sembunyikan kotak banner laptop
            var allDivs = document.querySelectorAll('div');
            for (var j = 0; j < allDivs.length; j++) {
                var d = allDivs[j];
                if (d.innerText && (d.innerText.indexOf('Unduh WhatsApp untuk Windows') !== -1 || d.innerText.indexOf('Dapatkan aplikasi') !== -1)) {
                    if (d.offsetWidth > 200 && d.offsetHeight < 150) {
                        d.style.display = 'none';
                    }
                }
            }

            // 3. Pastikan kartu login berada rapi di tengah
            var card = document.querySelector('div[data-ref]') || 
                       document.querySelector('._ak97') || 
                       document.querySelector('._ak96') || 
                       document.querySelector('div[class*="landing-window"]') ||
                       document.querySelector('div[class*="landing-wrapper"]');

            if (card) {
                card.style.margin = '10px auto';
                card.style.float = 'none';
            }

            // 4. Pastikan #side dan #main SELALU terlihat dan tidak pernah disembunyikan
            var side = document.getElementById('side');
            if (side && side.style.display === 'none') {
                side.style.display = '';
            }

            var main = document.getElementById('main');
            if (main && main.style.display === 'none') {
                main.style.display = '';
            }
        } catch(e) {}
    }

    setInterval(fitScreen, 300);
    fitScreen();
})();
