/* Selector de diseño. Para volver al diseño anterior para siempre: poné DEFAULT_THEME = 'classic'
   (o quitá la línea <script src="js/theme.js"> de los HTML). El botón flotante también alterna. */
(function () {
    const DEFAULT_THEME = 'rebrand';
    const KEY = 'gamehub-theme';
    let theme = DEFAULT_THEME;
    try {
        const fromUrl = new URLSearchParams(location.search).get('theme');
        if (fromUrl === 'classic' || fromUrl === 'rebrand') localStorage.setItem(KEY, fromUrl);
        theme = localStorage.getItem(KEY) || DEFAULT_THEME;
    } catch (e) { /* sin storage: usa el default */ }
    document.documentElement.dataset.theme = theme;
    if (theme === 'rebrand') {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.id = 'rebrand-css';
        link.href = document.currentScript.src.replace(/js\/theme\.js.*$/, 'css/rebrand.css?v=1');
        document.head.appendChild(link);
    }
    document.addEventListener('DOMContentLoaded', () => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'theme-toggle';
        button.style.cssText = 'position:fixed;right:1rem;bottom:1rem;z-index:50;padding:.55rem 1rem;border:1px solid rgba(255,255,255,.25);border-radius:999px;background:rgba(18,18,24,.92);color:#fff;font:500 .8rem sans-serif;cursor:pointer';
        button.textContent = theme === 'rebrand' ? 'Ver diseño clásico' : 'Probar nuevo diseño';
        button.addEventListener('click', () => {
            try { localStorage.setItem(KEY, theme === 'rebrand' ? 'classic' : 'rebrand'); } catch (e) { /* noop */ }
            location.reload();
        });
        document.body.appendChild(button);
    });
})();
