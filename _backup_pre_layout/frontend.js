document.addEventListener('DOMContentLoaded', () => {
    const sections = document.getElementById('catalogSections');
    const state = document.getElementById('catalogState');
    const favoriteIds = new Set();
    const MIN_GENRE_SIZE = 4;
    const OTHER_TITLE = 'Otros géneros';
    const GENRE_ICONS = {
        shooter: 'disparos', mmorpg: 'aventuras', 'action rpg': 'aventuras', arpg: 'aventuras',
        strategy: 'defensa', 'tower defense': 'defensa', racing: 'coches', sports: 'deportes',
        'card game': 'cartas', card: 'cartas', mmo: 'multijugador', moba: 'multijugador',
        'battle royale': 'multijugador', fighting: 'accion', horror: 'terror', survival: 'terror',
        puzzle: 'puzzle', fantasy: 'aventuras', social: 'casual', sandbox: 'minecraft'
    };
    let summaryText = '';
    let syncButton = null;

    loadCatalog();
    initSidebarSearch();

    async function loadCatalog() {
        state.classList.remove('api-error');
        state.textContent = 'Cargando catálogo...';
        try {
            const [games, favorites, original] = await Promise.all([
                GameHubApi.get('/api/games'),
                loadFavorites(),
                GameHubApi.get('/api/games/original').catch(() => null)
            ]);
            favoriteIds.clear();
            favorites.forEach(game => favoriteIds.add(game.id));

            const originalCarousel = createCarousel('GameHub Original', [createOriginalCard(original)]);
            const external = games.filter(game => !GameHubApi.isOriginal(game));
            const carousels = buildGenreCarousels(external);
            sections.replaceChildren(state, originalCarousel, ...carousels);
            renderSidebar(carousels);

            const genreCount = carousels.length;
            summaryText = external.length
                ? `${external.length} juegos gratuitos en ${genreCount} ${genreCount === 1 ? 'categoría' : 'categorías'}`
                : '';
            state.textContent = summaryText;
            if (!external.length) showEmptyCatalog();
            else removeSyncButton();
        } catch (problem) {
            sections.replaceChildren(state, createCarousel('GameHub Original', [createOriginalCard(null)]));
            state.textContent = problem.message;
            state.classList.add('api-error');
        }
    }

    async function loadFavorites() {
        if (!GameHubApi.getToken()) return [];
        try {
            return await GameHubApi.get('/api/users/me/favorites');
        } catch (problem) {
            if (problem.status === 401) GameHubApi.clearSession();
            return [];
        }
    }

    // ---------------------------------------------------------------- catálogo vacío

    function showEmptyCatalog() {
        state.textContent = 'El catálogo de juegos todavía está vacío. Podés importarlo desde FreeToGame (requiere Internet).';
        if (syncButton) return;
        syncButton = document.createElement('button');
        syncButton.type = 'button';
        syncButton.className = 'api-sync-button';
        syncButton.textContent = 'Importar catálogo';
        syncButton.addEventListener('click', async () => {
            syncButton.disabled = true;
            state.classList.remove('api-error');
            state.textContent = 'Importando juegos desde FreeToGame...';
            try {
                await GameHubApi.post('/api/games/sync');
                removeSyncButton();
                await loadCatalog();
            } catch (problem) {
                state.textContent = problem.status === 502
                    ? 'No pudimos conectar con FreeToGame. Probá de nuevo en unos minutos.'
                    : problem.message;
                state.classList.add('api-error');
                syncButton.disabled = false;
            }
        });
        state.after(syncButton);
    }

    function removeSyncButton() {
        if (syncButton) syncButton.remove();
        syncButton = null;
    }

    // ---------------------------------------------------------------- géneros

    function normalizeText(value) {
        return String(value || '').normalize('NFD').replace(/[̀-ͯ]/g, '').trim().toLowerCase();
    }

    /** Agrupa por el género real de cada juego: ningún juego queda fuera de los carruseles. */
    function buildGenreCarousels(games) {
        const groups = new Map();
        games.forEach(game => {
            const name = String(game.genre || '').trim() || 'Sin género';
            const key = normalizeText(name);
            if (!groups.has(key)) groups.set(key, { name, games: [] });
            groups.get(key).games.push(game);
        });
        const byTitle = (a, b) => String(a.title).localeCompare(String(b.title));
        const big = [...groups.values()]
            .filter(group => group.games.length >= MIN_GENRE_SIZE)
            .sort((a, b) => b.games.length - a.games.length || a.name.localeCompare(b.name));
        const small = [...groups.values()]
            .filter(group => group.games.length < MIN_GENRE_SIZE)
            .flatMap(group => group.games)
            .sort(byTitle);
        const carousels = big.map(group => createCarousel(group.name, group.games.sort(byTitle).map(createExternalCard)));
        if (small.length) carousels.push(createCarousel(OTHER_TITLE, small.map(createExternalCard)));
        return carousels;
    }

    function renderSidebar(carousels) {
        const list = document.querySelector('.sidebar ul');
        if (!list) return;
        const separator = list.querySelector('hr');
        if (separator) {
            while (separator.nextSibling) separator.nextSibling.remove();
        }
        carousels.forEach(section => {
            const title = section.querySelector('h2').textContent;
            const icon = GENRE_ICONS[normalizeText(title)] || 'controller';
            const item = document.createElement('li');
            const image = document.createElement('img');
            image.src = `./Iconos/iconos-sidebar/${icon}.svg`;
            image.alt = '';
            const link = document.createElement('a');
            link.href = `#${section.id}`;
            link.textContent = title;
            link.addEventListener('click', () => document.querySelector('.sidebar')?.classList.remove('show'));
            item.append(image, link);
            list.append(item);
        });
    }

    // ---------------------------------------------------------------- buscador del sidebar

    function initSidebarSearch() {
        const input = document.querySelector('.search-sidebar .search-input');
        if (!input) return;
        input.addEventListener('input', () => {
            const term = normalizeText(input.value);
            let visible = 0;
            sections.querySelectorAll('.genre-carousel').forEach(section => {
                let any = false;
                section.querySelectorAll('.api-game-card').forEach(card => {
                    const match = !term || card.dataset.search.includes(term);
                    card.hidden = !match;
                    if (match) { any = true; visible++; }
                });
                section.hidden = !any;
            });
            if (!term) state.textContent = summaryText;
            else state.textContent = visible
                ? `${visible} resultado${visible === 1 ? '' : 's'} para "${input.value.trim()}"`
                : `No encontramos juegos para "${input.value.trim()}".`;
        });
    }

    // ---------------------------------------------------------------- tarjetas

    function createOriginalCard(original) {
        return createCard({
            id: original ? original.id : null,
            title: 'Connect Four',
            genre: 'GameHub Original',
            platform: 'Juego local',
            thumbnailUrl: 'Images/cardDeadpool.jpg'
        }, 'game.html', true);
    }

    function createExternalCard(game) {
        return createCard(game, GameHubApi.detailUrl(game), false);
    }

    function createCard(game, detailUrl, local) {
        const card = document.createElement('article');
        card.className = 'card api-game-card';
        card.innerHTML = `<div class="card-content"><a class="api-card-detail"><img loading="lazy" alt=""><p class="card-title"></p></a><div class="api-game-meta"></div><div class="api-card-actions"><a class="api-detail-link"></a><button class="api-heart" type="button">♡</button></div></div>`;
        card.querySelectorAll('a').forEach(link => { link.href = detailUrl; });
        const image = card.querySelector('img');
        image.src = game.thumbnailUrl || 'Images/game-screen.png';
        image.alt = game.title || 'Juego';
        image.addEventListener('error', () => { image.src = 'Images/game-screen.png'; }, { once: true });
        card.querySelector('.card-title').textContent = game.title || 'Sin título';
        card.querySelector('.api-game-meta').textContent = local
            ? 'GameHub Original · Connect Four'
            : [game.genre, game.platform].filter(Boolean).join(' · ') || 'Juego gratuito';
        card.querySelector('.api-detail-link').textContent = local ? 'Jugar' : 'Ver detalle';
        card.dataset.search = normalizeText(`${game.title} ${game.genre} ${game.platform}`);
        const heart = card.querySelector('.api-heart');
        if (!game.id) {
            heart.remove();
        } else {
            heart.dataset.favoriteId = game.id;
            heart.addEventListener('click', () => toggleFavorite(game, heart));
            syncHeart(heart);
        }
        return card;
    }

    function createCarousel(title, cards) {
        const section = document.createElement('section');
        section.className = 'carousel-container genre-carousel';
        section.id = `genre-${normalizeText(title).replace(/[^a-z0-9]+/g, '-')}`;
        section.innerHTML = `<h2></h2><button class="left-arrow" type="button" aria-label="Ver juegos anteriores"><img src="./Iconos/Carrusel-flecha-izquierda.svg" alt=""></button><div class="carousel"></div><button class="right-arrow" type="button" aria-label="Ver más juegos"><img src="./Iconos/Carrusel-flecha-derecha.svg" alt=""></button>`;
        section.querySelector('h2').textContent = title;
        const carousel = section.querySelector('.carousel');
        carousel.replaceChildren(...cards);
        section.querySelector('.right-arrow').addEventListener('click', () => scrollCarousel(carousel, 1));
        section.querySelector('.left-arrow').addEventListener('click', () => scrollCarousel(carousel, -1));
        return section;
    }

    function scrollCarousel(carousel, direction) {
        const card = carousel.querySelector('.card:not([hidden])');
        if (!card) return;
        const atEnd = carousel.scrollLeft + carousel.clientWidth >= carousel.scrollWidth - 2;
        const atStart = carousel.scrollLeft <= 2;
        if (direction > 0 && atEnd) carousel.scrollTo({ left: 0, behavior: 'smooth' });
        else if (direction < 0 && atStart) carousel.scrollTo({ left: carousel.scrollWidth, behavior: 'smooth' });
        else carousel.scrollBy({ left: (card.offsetWidth + 15) * direction, behavior: 'smooth' });
        carousel.querySelectorAll('.card').forEach(item => item.classList.add('addSkew'));
        window.setTimeout(() => carousel.querySelectorAll('.card').forEach(item => item.classList.remove('addSkew')), 400);
    }

    function syncHeart(heart) {
        const active = favoriteIds.has(Number(heart.dataset.favoriteId));
        heart.textContent = active ? '♥' : '♡';
        heart.classList.toggle('active', active);
        heart.setAttribute('aria-label', active ? 'Quitar de favoritos' : 'Agregar a favoritos');
    }

    async function toggleFavorite(game, heart) {
        if (!GameHubApi.getToken()) {
            state.textContent = 'Iniciá sesión para guardar favoritos.';
            return;
        }
        const active = favoriteIds.has(game.id);
        heart.disabled = true;
        try {
            if (active) await GameHubApi.delete(`/api/games/${game.id}/favorite`);
            else await GameHubApi.post(`/api/games/${game.id}/favorite`);
            active ? favoriteIds.delete(game.id) : favoriteIds.add(game.id);
            syncHeart(heart);
        } catch (problem) {
            state.textContent = problem.message;
        } finally {
            heart.disabled = false;
        }
    }
});
