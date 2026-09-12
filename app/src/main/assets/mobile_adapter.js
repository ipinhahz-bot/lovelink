// Mobile Adapter for WhatsApp Web - Native Feel Transformation
(function() {
    console.log("[LoveLink] Initializing Mobile Adapter...");

    // 1. Force mobile responsive viewport
    var meta = document.querySelector('meta[name="viewport"]');
    if (!meta) {
        meta = document.createElement('meta');
        meta.name = 'viewport';
        document.getElementsByTagName('head')[0].appendChild(meta);
    }
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';

    // 2. Ensure back button exists in chat header
    function ensureBackButton() {
        var main = document.getElementById('main');
        if (!main) return;
        var header = main.querySelector('header');
        if (!header || document.getElementById('custom-mobile-back-btn')) return;

        var btn = document.createElement('button');
        btn.id = 'custom-mobile-back-btn';
        btn.innerHTML = '<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round" style="display:block;"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>';
        btn.setAttribute('style', 'background:none; border:none; color:inherit; padding:8px 10px; margin-right:4px; cursor:pointer; display:flex; align-items:center; justify-content:center; border-radius:50%;');
        
        btn.onclick = function(e) {
            e.stopPropagation();
            e.preventDefault();
            document.body.classList.remove('chat-open');
            document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true }));
        };

        header.insertBefore(btn, header.firstChild);
    }

    // 3. Monitor active chat and toggle single-column mobile view
    function checkChatState() {
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

    // Continuous observer
    var observer = new MutationObserver(function() {
        checkChatState();
    });

    observer.observe(document.body, { childList: true, subtree: true });
    setInterval(checkChatState, 500);

    console.log("[LoveLink] Mobile Adapter Loaded Successfully");
})();
