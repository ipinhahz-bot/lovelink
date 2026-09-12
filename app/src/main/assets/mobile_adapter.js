// Mobile Adapter for WhatsApp Web - 100% Pas di Layar HP Android
(function() {
    console.log("[LoveLink] Inisialisasi Tampilan Pas Layar HP...");

    // 1. Set Viewport Pas HP
    function setupViewport() {
        var meta = document.querySelector('meta[name="viewport"]');
        if (!meta) {
            meta = document.createElement('meta');
            meta.name = 'viewport';
            document.head.appendChild(meta);
        }
        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover';
        
        document.documentElement.style.overflowX = 'hidden';
        document.body.style.overflowX = 'hidden';
    }

    // 2. Tombol Kembali (Back Button) di obrolan
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
            closeChat();
        };

        header.insertBefore(btn, header.firstChild);
    }

    function closeChat() {
        document.body.classList.remove('chat-open');
        document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true }));
    }

    // 3. Monitor Obrolan Aktif (Tampilkan 1 Kolom Full Screen)
    function checkState() {
        setupViewport();

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

        // Pastikan tampilan awal (Login / Form Nomor HP) tersusun 1 kolom ke bawah
        var landingFlex = document.querySelectorAll('._ak96, ._ak97, [data-testid="qrcode"]');
        landingFlex.forEach(function(el) {
            el.style.flexDirection = 'column';
            el.style.width = '100%';
            el.style.maxWidth = '100vw';
        });
    }

    // Observer berkala
    var observer = new MutationObserver(function() {
        checkState();
    });

    observer.observe(document.body, { childList: true, subtree: true });
    setInterval(checkState, 600);
    checkState();
})();
