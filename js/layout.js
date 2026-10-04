/* Layout compartido: header, menú lateral y menú de usuario.
   Cada página solo tiene <div id="site-layout"></div> ; el menú lateral es el mismo en todas las páginas (géneros: js/sidebar.js).
   Debe cargarse ANTES de header.js, site.js y api.js. */
(function () {
    const mount = document.getElementById('site-layout');
    if (!mount) return;
    mount.outerHTML = `
    <header><nav class="navbar">
        <div class="navbar-left">
            <img src="./Iconos/Hamburguesa.svg" alt="Abrir menú" class="menu-button">
            <a href="index.html" class="logo"><img src="./Iconos/tituloLogo.svg" alt="GameHub" class="logo-img"></a>
        </div>
        <form class="search-bar-nav ai-navbar-search" id="aiNavbarForm">
            <input type="search" maxlength="500" required placeholder="¿Qué tenés ganas de jugar?" class="search-input" id="aiNavbarQuery" autocomplete="off">
            <button type="submit" class="search-icon-button" aria-label="Buscar con AI"><img src="./Iconos/Lupa.svg" alt=""></button>
            <div class="ai-search-dropdown" id="aiSearchDropdown" hidden aria-live="polite"></div>
        </form>
        <div class="navbar-right">
            <a href="favorites.html" class="corazon icon" aria-label="Mis favoritos"></a>
            <div class="campana icon" aria-label="Notificaciones"></div>
            <img src="./Iconos/Avatar con imagen.svg" alt="Perfil" class="user-icon">
        </div>
    </nav></header>
    <aside class="sidebar">
        <img src="./Iconos/icono-cruz.svg" alt="Cerrar" class="icon-cruz-menu">
        <div class="search-sidebar"><input type="text" placeholder="Buscar..." class="search-input"><img src="./Iconos/Lupa.svg" alt="Buscar" class="search-lupa"></div>
        <ul>
            <li><img src="./Iconos/iconos-sidebar/home.svg" alt=""><a href="index.html">Inicio</a></li>
            <li><img src="./Iconos/iconos-sidebar/controller.svg" alt=""><a href="game.html">GameHub Original</a></li>
            <li><img src="./Iconos/Corazon.svg" alt=""><a href="favorites.html">Favoritos</a></li>
            <hr>
        </ul>
    </aside>
    <div class="user-menu">
        <img src="./Iconos/Avatar con imagen.svg" alt="Avatar" class="user-avatar">
        <p id="profileGreeting">¡Hola!</p>
        <ul>
            <li><a href="#">Mi perfil</a></li>
            <li><a href="#">Configuración de cuenta</a></li>
            <li><a href="#">Soporte</a></li>
            <li><a href="login.html" id="logoutLink">Cerrar sesión</a></li>
        </ul>
        <img src="./Iconos/icono-cruz.svg" alt="Cerrar" class="icon-cruz-profile">
    </div>`;
})();
