// Mobile Adapter - 100% Pas di Layar HP Android (Auto Fit & Full Screen)
(function() {
    function applyMobileFixes() {
        // 1. Viewport pas layar
        var meta = document.querySelector('meta[name="viewport"]');
        if (!meta) {
            meta = document.createElement('meta');
            meta.name = 'viewport';
            document.head.appendChild(meta);
        }
        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover';

        // 2. Hapus banner download desktop
        var banners = document.querySelectorAll('._ak8j, [data-testid="intro-md-beta-logo-light"], div[class*="banner"]');
        banners.forEach(function(b) {
            b.style.display = 'none';
        });

        // 3. Pastikan kotak QR & Login Nomor HP benar-benar pas di tengah layar
        var card = document.querySelector('._ak97') || document.querySelector('._ak96') || document.querySelector('div[data-ref]');
        if (card) {
            var screenWidth = window.innerWidth;
            if (screenWidth > 0) {
                card.style.maxWidth = '92vw';
                card.style.margin = '10px auto';
                card.style.flexDirection = 'column';
                card.style.alignItems = 'center';
                
                // Jika masih lebih lebar dari layar, paksa scale down secara presisi
                if (card.scrollWidth > screenWidth) {
                    var scale = (screenWidth - 20) / card.scrollWidth;
                    card.style.transformOrigin = 'top center';
                    card.style.transform = 'scale(' + scale + ')';
                }
            }
        }

        // 4. Deteksi obrolan aktif untuk tampilan 1-kolom
        var main = document.getElementById('main');
        var hasActiveChat = main && (main.querySelector('footer') || main.querySelector('div[contenteditable="true"]'));

        if (hasActiveChat) {
            if (!document.body.classList.contains('chat-open')) {
                document.body.classList.add('chat-open');
            }
            ensureBackButton();
        } else {
            if (document.body.classList.contains('chat-open')) {
                document.body.classList.remove('chat-open');
            }
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
        btn.setAttribute('style', 'display:flex; align-items:center; justify-content:center; width:40px; height:40px; cursor:pointer; margin-right:6px; color:inherit; border-radius:50%; flex-shrink:0;');
        
        btn.onclick = function(e) {
            e.stopPropagation();
            e.preventDefault();
            document.body.classList.remove('chat-open');
            document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true }));
        };

        header.insertBefore(btn, header.firstChild);
    }

    // Jalankan terus menerus agar React tidak menimpa
    setInterval(applyMobileFixes, 250);
    applyMobileFixes();
})();
