// Comentarios y favorito reales para la pagina del juego local (Connect Four).
document.addEventListener('DOMContentLoaded', () => {
    const list = document.getElementById('commentsList');
    const state = document.getElementById('commentState');
    const count = document.getElementById('commentCount');
    const input = document.getElementById('commentContent');
    const submit = document.getElementById('commentSubmit');
    const heartEmpty = document.querySelector('.icon-heart-empty');
    const heartFull = document.querySelector('.icon-heart-full');
    if (!list || !state || !input || !submit) return;

    let game = null;
    let favorite = false;

    init();

    async function init() {
        try {
            game = await GameHubApi.get('/api/games/original');
        } catch (problem) {
            state.textContent = problem.message;
            state.classList.add('api-error');
            return;
        }
        await Promise.all([loadComments(), loadFavorite()]);
    }

    // ------------------------------------------------------------ comentarios

    async function loadComments() {
        try {
            const comments = await GameHubApi.get(`/api/games/${game.id}/comments`);
            count.textContent = `${comments.length} comentario${comments.length === 1 ? '' : 's'}`;
            state.textContent = comments.length ? '' : 'Todavía no hay comentarios. ¡Sé el primero!';
            list.replaceChildren(...comments.map(createComment));
        } catch (problem) {
            state.textContent = problem.message;
            state.classList.add('api-error');
        }
    }

    function createComment(comment) {
        const item = document.createElement('article');
        item.className = 'container-comment';
        item.innerHTML = '<div class="comment-header"><div class="user-info"><img src="./Iconos/Avatar con imagen.svg" alt="Avatar" class="avatar"><span class="username"></span></div><span class="date-comment"></span></div><div class="comment-body"><p></p></div>';
        item.querySelector('.username').textContent = comment.username;
        item.querySelector('.date-comment').textContent = new Date(comment.createdAt).toLocaleDateString('es-AR');
        item.querySelector('.comment-body p').textContent = comment.content;
        return item;
    }

    async function sendComment() {
        if (!game) return;
        const content = input.value.trim();
        if (!content) return;
        if (!GameHubApi.getToken()) {
            state.classList.add('api-error');
            state.innerHTML = 'Iniciá sesión para comentar. <a href="login.html">Iniciar sesión</a>';
            return;
        }
        submit.disabled = true;
        try {
            await GameHubApi.post(`/api/games/${game.id}/comments`, { content });
            input.value = '';
            state.classList.remove('api-error');
            await loadComments();
        } catch (problem) {
            state.classList.add('api-error');
            state.textContent = problem.message;
        } finally {
            submit.disabled = false;
        }
    }

    submit.addEventListener('click', sendComment);
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendComment();
        }
    });

    // ------------------------------------------------------------ favorito

    function renderFavorite() {
        if (!heartEmpty || !heartFull) return;
        heartEmpty.classList.toggle('show', favorite);
        heartFull.classList.toggle('show', favorite);
    }

    async function loadFavorite() {
        if (!GameHubApi.getToken()) return;
        try {
            const favorites = await GameHubApi.get('/api/users/me/favorites');
            favorite = favorites.some(item => item.id === game.id);
            renderFavorite();
        } catch (problem) {
            if (problem.status === 401) GameHubApi.clearSession();
        }
    }

    async function toggleFavorite() {
        if (!game) return;
        if (!GameHubApi.getToken()) {
            state.classList.add('api-error');
            state.innerHTML = 'Iniciá sesión para guardar favoritos. <a href="login.html">Iniciar sesión</a>';
            return;
        }
        try {
            if (favorite) await GameHubApi.delete(`/api/games/${game.id}/favorite`);
            else await GameHubApi.post(`/api/games/${game.id}/favorite`);
            favorite = !favorite;
            renderFavorite();
        } catch (problem) {
            state.classList.add('api-error');
            state.textContent = problem.message;
        }
    }

    [heartEmpty, heartFull].forEach(icon => icon && icon.addEventListener('click', toggleFavorite));
});
