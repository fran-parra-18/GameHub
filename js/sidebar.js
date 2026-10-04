/* Menú lateral compartido: lista de géneros del catálogo y buscador, iguales en todas las páginas.
   Se carga después de api.js. En index.html el filtrado lo hace frontend.js; en el resto, Enter lleva a index.html?q=... */
document.addEventListener('DOMContentLoaded', () => {
    const list = document.querySelector('.sidebar ul');
    if (!list || typeof GameHubApi === 'undefined') return;
    const MIN_GENRE_SIZE = 4;
    const OTHER_TITLE = 'Otros géneros';
    const CACHE_KEY = 'gamehub.sidebarGenres';
    const CACHE_MS = 10 * 60 * 1000;
    const GENRE_ICONS = {
        shooter: 'disparos', mmorpg: 'aventuras', 'action rpg': 'aventuras', arpg: 'aventuras',
        strategy: 'defensa', 'tower defense': 'defensa', racing: 'coches', sports: 'deportes',
        'card game': 'cartas', card: 'cartas', mmo: 'multijugador', moba: 'multijugador',
        'battle royale': 'multijugador', fighting: 'accion', horror: 'terror', survival: 'terror',
        puzzle: 'puzzle', fantasy: 'aventuras', social: 'casual', sandbox: 'minecraft'
    };
    const onIndex = /(^|\/)(index\.html)?$/.test(window.location.pathname);

    const normalize = value => String(value || '').normalize('NFD').replace(/[̀-ͯ]/g, '').trim().toLowerCase();

    initSearch();
    loadGenres();

    function initSearch() {
        const input = document.querySelector('.search-sidebar .search-input');
        if (!input || onIndex) return;
        input.addEventListener('keydown', event => {
            if (event.key !== 'Enter' || !input.value.trim()) return;
            window.location.href = `index.html?q=${encodeURIComponent(input.value.trim())}`;
        });
    }

    async function loadGenres() {
        let genres = readCache();
        if (!genres) {
            try {
                const games = await GameHubApi.get('/api/games');
                genres = buildGenres(games.filter(game => !GameHubApi.isOriginal(game)));
                sessionStorage.setItem(CACHE_KEY, JSON.stringify({ at: Date.now(), genres }));
            } catch (problem) { return; }
        }
        render(genres);
    }

    function readCache() {
        try {
            const cached = JSON.parse(sessionStorage.getItem(CACHE_KEY));
            return cached && Date.now() - cached.at < CACHE_MS ? cached.genres : null;
        } catch (problem) { return null; }
    }

    /** Misma regla que los carruseles del index: géneros con pocos juegos van a "Otros géneros". */
    function buildGenres(games) {
        const groups = new Map();
        games.forEach(game => {
            const name = String(game.genre || '').trim() || 'Sin género';
            const key = normalize(name);
            if (!groups.has(key)) groups.set(key, { name, count: 0 });
            groups.get(key).count++;
        });
        const all = [...groups.values()];
        const big = all.filter(group => group.count >= MIN_GENRE_SIZE)
            .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name))
            .map(group => group.name);
        if (all.some(group => group.count < MIN_GENRE_SIZE)) big.push(OTHER_TITLE);
        return big;
    }

    function render(genres) {
        const separator = list.querySelector('hr');
        if (separator) while (separator.nextSibling) separator.nextSibling.remove();
        genres.forEach(name => {
            const item = document.createElement('li');
            const image = document.createElement('img');
            image.src = `./Iconos/iconos-sidebar/${GENRE_ICONS[normalize(name)] || 'controller'}.svg`;
            image.alt = '';
            const link = document.createElement('a');
            link.href = `index.html#genre-${normalize(name).replace(/[^a-z0-9]+/g, '-')}`;
            link.textContent = name;
            link.addEventListener('click', () => document.querySelector('.sidebar')?.classList.remove('show'));
            item.append(image, link);
            list.append(item);
        });
    }
});
