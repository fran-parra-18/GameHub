document.addEventListener('DOMContentLoaded', () => {
    const gameId = Number(new URLSearchParams(window.location.search).get('id'));
    const detailState = document.getElementById('detailState');
    const favoriteState = document.getElementById('favoriteState');
    const favoriteButton = document.getElementById('detailFavorite');
    let game;
    let favorite = false;

    if (!Number.isSafeInteger(gameId) || gameId <= 0) {
        detailState.textContent = 'El enlace del juego no es válido.';
        detailState.classList.add('api-error');
        return;
    }
    loadPage();

    async function loadPage() {
        try {
            game = await GameHubApi.get(`/api/games/${gameId}`);
            renderGame();
            await Promise.all([loadComments(), loadFavoriteState()]);
        } catch (problem) {
            detailState.textContent = problem.message;
            detailState.classList.add('api-error');
        }
    }

    function renderGame() {
        document.title = `${game.title} · GameHub`;
        const image = document.getElementById('detailImage');
        image.src = game.thumbnailUrl || 'Images/game-screen.png';
        image.alt = game.title;
        image.addEventListener('error', () => { image.src = 'Images/game-screen.png'; }, { once: true });
        document.getElementById('detailTitle').textContent = game.title;
        document.getElementById('detailMeta').textContent = [game.genre, game.platform].filter(Boolean).join(' · ') || 'Juego gratuito';
        document.getElementById('detailDeveloper').textContent = game.developer || 'No informado';
        document.getElementById('detailPublisher').textContent = game.publisher || 'No informado';
        document.getElementById('detailDescription').textContent = game.description || 'Este juego todavía no tiene descripción.';
        const play = document.getElementById('detailPlay');
        play.href = game.gameUrl || '#';
        if (!game.gameUrl) { play.textContent = 'Enlace no disponible'; play.removeAttribute('target'); }
        else play.textContent = 'Jugar';
        detailState.textContent = '';
        document.getElementById('gameDetail').hidden = false;
        document.getElementById('detailComments').hidden = false;
        document.getElementById('detailCommentBox').hidden = false;
    }

    async function loadFavoriteState() {
        if (!GameHubApi.getToken()) { renderFavorite(); return; }
        try {
            const favorites = await GameHubApi.get('/api/users/me/favorites');
            favorite = favorites.some(item => item.id === gameId);
            renderFavorite();
        } catch (problem) { favoriteState.textContent = problem.message; }
    }

    function renderFavorite() {
        favoriteButton.textContent = favorite ? '♥ Quitar de favoritos' : '♡ Agregar a favoritos';
        favoriteButton.classList.toggle('active', favorite);
    }

    favoriteButton.addEventListener('click', async () => {
        if (!GameHubApi.getToken()) { favoriteState.textContent = 'Iniciá sesión para guardar favoritos.'; return; }
        favoriteButton.disabled = true;
        try {
            if (favorite) await GameHubApi.delete(`/api/games/${gameId}/favorite`);
            else await GameHubApi.post(`/api/games/${gameId}/favorite`);
            favorite = !favorite; renderFavorite(); favoriteState.textContent = favorite ? 'Agregado a favoritos.' : 'Eliminado de favoritos.';
        } catch (problem) { favoriteState.textContent = problem.message; }
        finally { favoriteButton.disabled = false; }
    });

    async function loadComments() {
        const list = document.getElementById('commentsList');
        const state = document.getElementById('commentState');
        list.innerHTML = ''; state.textContent = 'Cargando comentarios...';
        try {
            const comments = await GameHubApi.get(`/api/games/${gameId}/comments`);
            document.getElementById('commentCount').textContent = `${comments.length} comentario${comments.length === 1 ? '' : 's'}`;
            state.textContent = comments.length ? '' : 'Todavía no hay comentarios.';
            comments.forEach(comment => list.append(createComment(comment)));
        } catch (problem) { state.textContent = problem.message; }
    }

    function createComment(comment) {
        const item = document.createElement('article');
        item.className = 'container-comment';
        item.innerHTML = `<div class="comment-header"><div class="user-info"><img src="./Iconos/Avatar con imagen.svg" alt="Avatar" class="avatar"><span class="username"></span></div><span class="date-comment"></span></div><div class="comment-body"><p></p></div>`;
        item.querySelector('.username').textContent = comment.username;
        item.querySelector('.date-comment').textContent = new Date(comment.createdAt).toLocaleDateString('es-AR');
        item.querySelector('.comment-body p').textContent = comment.content;
        return item;
    }

    document.getElementById('commentForm').addEventListener('submit', async event => {
        event.preventDefault();
        const state = document.getElementById('commentState');
        if (!GameHubApi.getToken()) { state.textContent = 'Iniciá sesión para comentar.'; return; }
        const input = document.getElementById('commentContent');
        try {
            await GameHubApi.post(`/api/games/${gameId}/comments`, { content: input.value });
            input.value = ''; await loadComments();
        } catch (problem) { state.textContent = problem.message; }
    });
});
