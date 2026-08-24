document.addEventListener('DOMContentLoaded', () => {
    const state = document.getElementById('favoritesState');
    const grid = document.getElementById('favoritesGrid');
    if (!GameHubApi.getToken()) {
        state.innerHTML = 'Iniciá sesión para ver tus favoritos. <a href="login.html">Iniciar sesión</a>';
        return;
    }
    loadFavorites();

    async function loadFavorites() {
        try {
            const favorites = await GameHubApi.get('/api/users/me/favorites');
            grid.replaceChildren(...favorites.map(createCard));
            state.textContent = favorites.length ? `${favorites.length} juego${favorites.length === 1 ? '' : 's'} guardado${favorites.length === 1 ? '' : 's'}` : 'Todavía no guardaste ningún favorito.';
        } catch (problem) {
            state.textContent = problem.status === 401 ? 'Iniciá sesión para ver tus favoritos.' : problem.message;
            state.classList.add('api-error');
        }
    }

    function createCard(game) {
        const card = document.createElement('article');
        card.className = 'card favorite-card';
        card.innerHTML = `<div class="card-content"><a class="api-card-detail"><img loading="lazy" alt=""><p class="card-title"></p></a><div class="api-game-meta"></div><div class="api-card-actions"><a class="api-detail-link">Ver detalle</a><button class="remove-favorite" type="button">Quitar</button></div></div>`;
        card.querySelectorAll('a').forEach(link => { link.href = `game-detail.html?id=${encodeURIComponent(game.id)}`; });
        const image = card.querySelector('img'); image.src = game.thumbnailUrl || 'Images/game-screen.png'; image.alt = game.title;
        image.addEventListener('error', () => { image.src = 'Images/game-screen.png'; }, { once: true });
        card.querySelector('.card-title').textContent = game.title;
        card.querySelector('.api-game-meta').textContent = [game.genre, game.platform].filter(Boolean).join(' · ');
        card.querySelector('.remove-favorite').addEventListener('click', async () => {
            try { await GameHubApi.delete(`/api/games/${game.id}/favorite`); await loadFavorites(); }
            catch (problem) { state.textContent = problem.message; }
        });
        return card;
    }
});
