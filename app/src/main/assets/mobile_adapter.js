// Auto-Fit & Mobile Native UI for WhatsApp Web
(function() {
    function fitScreen() {
        // 1. Sembunyikan banner download desktop yang mengganggu
        var banners = document.querySelectorAll('header, ._ak8j, [data-testid="intro-md-beta-logo-light"], div[class*="banner"]');
        for (var i = 0; i < banners.length; i++) {
            banners[i].style.display = 'none';
        }

        // 2. Pastikan kartu QR & Login nomor HP pas 100% di layar HP (tidak terpotong)
        var card = document.querySelector('div[data-ref]') || 
                   document.querySelector('._ak97') || 
                   document.querySelector('._ak96') || 
                   document.querySelector('div[class*="landing-window"]') ||
                   document.querySelector('div[class*="landing-wrapper"]');

        if (card) {
            var screenW = window.innerWidth || document.documentElement.clientWidth;
            var cardW = card.offsetWidth || card.scrollWidth;
            if (screenW > 0 && cardW > screenW) {
                var scale = (screenW - 16) / cardW;
                card.style.transformOrigin = 'top center';
                card.style.transform = 'scale(' + scale + ')';
                card.style.margin = '10px auto';
            }
        }

        // 3. Tampilan Chat 1-Kolom Full Screen Murni
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

    setInterval(fitScreen, 200);
    fitScreen();
})();
