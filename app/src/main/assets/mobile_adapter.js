// Auto-Fit & Mobile Native UI for WhatsApp Web
(function() {
    function fitScreen() {
        try {
            // 1. Set viewport lebar tetap 800px agar TIDAK terpotong ke kanan!
            var meta = document.querySelector('meta[name="viewport"]');
            if (!meta) {
                meta = document.createElement('meta');
                meta.name = 'viewport';
                (document.head || document.documentElement).appendChild(meta);
            }
            if (meta.content !== 'width=800, user-scalable=yes') {
                meta.content = 'width=800, user-scalable=yes';
            }

            // 2. Sembunyikan banner download desktop WhatsApp untuk Windows
            var banners = document.querySelectorAll('header, [data-testid="intro-md-beta-logo-light"], div[class*="banner"]');
            for (var i = 0; i < banners.length; i++) {
                banners[i].style.display = 'none';
            }

            // 3. Sembunyikan juga elemen banner yang ada di screenshot (kotak laptop)
            var allDivs = document.querySelectorAll('div');
            for (var j = 0; j < allDivs.length; j++) {
                var d = allDivs[j];
                if (d.innerText && (d.innerText.indexOf('Unduh WhatsApp untuk Windows') !== -1 || d.innerText.indexOf('Dapatkan aplikasi') !== -1)) {
                    // Cari kontainer pembungkusnya
                    if (d.offsetWidth > 200 && d.offsetHeight < 150) {
                        d.style.display = 'none';
                    }
                }
            }

            // 4. Pastikan kartu login / QR berada rapi di tengah
            var card = document.querySelector('div[data-ref]') || 
                       document.querySelector('._ak97') || 
                       document.querySelector('._ak96') || 
                       document.querySelector('div[class*="landing-window"]') ||
                       document.querySelector('div[class*="landing-wrapper"]');

            if (card) {
                card.style.margin = '10px auto';
                card.style.float = 'none';
            }

            // 5. Tampilan Chat 1-Kolom Full Screen Murni Saat Obrolan Dibuka
            var main = document.getElementById('main');
            var side = document.getElementById('side');
            var hasActiveChat = main && (main.querySelector('footer') || main.querySelector('div[contenteditable="true"]'));

            if (hasActiveChat && main && side) {
                main.style.display = 'flex';
                main.style.position = 'fixed';
                main.style.top = '0';
                main.style.left = '0';
                main.style.width = '100vw';
                main.style.height = '100vh';
                main.style.zIndex = '9999';
                side.style.display = 'none';
                ensureBackButton();
            } else if (side) {
                side.style.display = 'flex';
                side.style.width = '100vw';
                if (main) main.style.display = 'none';
            }
        } catch(e) {}
    }

    function ensureBackButton() {
        var main = document.getElementById('main');
        if (!main) return;
        var header = main.querySelector('header');
        if (!header || document.getElementById('custom-mobile-back-btn')) return;

        var btn = document.createElement('div');
        btn.id = 'custom-mobile-back-btn';
        btn.innerHTML = '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>';
        btn.setAttribute('style', 'display:flex; align-items:center; justify-content:center; width:42px; height:42px; cursor:pointer; margin-right:6px; color:inherit; border-radius:50%; flex-shrink:0;');
        
        btn.onclick = function(e) {
            e.stopPropagation();
            e.preventDefault();
            var main = document.getElementById('main');
            var side = document.getElementById('side');
            if (main) main.style.display = 'none';
            if (side) side.style.display = 'flex';
            document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true }));
        };

        header.insertBefore(btn, header.firstChild);
    }

    setInterval(fitScreen, 300);
    fitScreen();
})();
