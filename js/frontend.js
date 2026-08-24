document.addEventListener('DOMContentLoaded', () => {
    const catalog = document.getElementById('apiCatalog');
    const state = document.getElementById('catalogState');
    const favoriteIds = new Set();
    loadCatalog();

    async function loadCatalog() {
        try {
            const favoritesPromise = GameHubApi.getToken() ? GameHubApi.get('/api/users/me/favorites') : Promise.resolve([]);
            const [games, favorites] = await Promise.all([GameHubApi.get('/api/games'), favoritesPromise]);
            favorites.forEach(game => favoriteIds.add(game.id));
            catalog.replaceChildren(createLocalCard(), ...games.map(createGameCard));
            state.textContent = games.length ? `${games.length} juegos disponibles` : 'El catálogo todavía está vacío.';
        } catch (problem) {
            catalog.replaceChildren(createLocalCard());
            state.textContent = problem.message;
            state.classList.add('api-error');
        }
    }

    function createLocalCard() {
        const card = document.createElement('a');
        card.className = 'card api-game-card-link';
        card.href = 'game.html';
        card.innerHTML = `<div class="card-content"><img src="Images/cardDeadpool.jpg" alt="Deadpool vs Wolverine"><p class="card-title">Deadpool vs Wolverine</p><div class="api-game-meta">GameHub Original · Connect Four</div></div>`;
        return card;
    }

    function createGameCard(game) {
        const card = document.createElement('article');
        card.className = 'card api-game-card';
        const detail = `game-detail.html?id=${encodeURIComponent(game.id)}`;
        card.innerHTML = `<div class="card-content"><a class="api-card-detail"><img loading="lazy" alt=""><p class="card-title"></p></a><div class="api-game-meta"></div><div class="api-card-actions"><a class="api-detail-link">Ver detalle</a><button class="api-heart" type="button">♡</button></div></div>`;
        const detailLinks = card.querySelectorAll('a');
        detailLinks.forEach(link => { link.href = detail; });
        const image = card.querySelector('img');
        image.src = game.thumbnailUrl || 'Images/game-screen.png';
        image.alt = game.title || 'Juego';
        image.addEventListener('error', () => { image.src = 'Images/game-screen.png'; }, { once: true });
        card.querySelector('.card-title').textContent = game.title || 'Sin título';
        card.querySelector('.api-game-meta').textContent = [game.genre, game.platform].filter(Boolean).join(' · ') || 'Juego gratuito';
        const heart = card.querySelector('.api-heart');
        heart.dataset.favoriteId = game.id;
        heart.addEventListener('click', () => toggleFavorite(game, heart));
        syncHeart(heart);
        return card;
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
