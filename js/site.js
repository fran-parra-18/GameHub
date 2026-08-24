document.addEventListener('DOMContentLoaded', () => {
    initializeSessionUI();
    initializeNavbarAI();

    async function initializeSessionUI() {
        const greeting = document.getElementById('profileGreeting');
        const logout = document.getElementById('logoutLink');
        if (!greeting || !logout) return;
        if (!GameHubApi.getToken()) {
            greeting.textContent = '¡Hola! Iniciá sesión';
            logout.textContent = 'Iniciar sesión';
            logout.href = 'login.html';
            return;
        }
        logout.addEventListener('click', event => {
            event.preventDefault();
            GameHubApi.clearSession();
            window.location.href = 'index.html';
        });
        try {
            const user = await GameHubApi.get('/api/users/me');
            greeting.textContent = `¡Hola, ${user.username}!`;
        } catch (problem) {
            if (problem.status === 401) GameHubApi.clearSession();
            greeting.textContent = 'Tu sesión venció';
        }
    }

    function initializeNavbarAI() {
        const form = document.getElementById('aiNavbarForm');
        const input = document.getElementById('aiNavbarQuery');
        const dropdown = document.getElementById('aiSearchDropdown');
        if (!form || !input || !dropdown) return;

        form.addEventListener('submit', async event => {
            event.preventDefault();
            dropdown.hidden = false;
            dropdown.classList.remove('api-error');
            dropdown.innerHTML = '<p class="ai-dropdown-state">Buscando juegos para vos...</p>';
            try {
                const response = await GameHubApi.post('/api/ai/find', { query: input.value });
                if (!response.recommendations.length) {
                    dropdown.innerHTML = '<p class="ai-dropdown-state">No encontramos coincidencias. Probá otra búsqueda.</p>';
                    return;
                }
                dropdown.replaceChildren(...response.recommendations.map(createAIResult));
            } catch (problem) {
                dropdown.classList.add('api-error');
                dropdown.innerHTML = `<p class="ai-dropdown-state">${problem.status === 502 || problem.status === 503 ? 'AI Game Finder no está disponible en este momento.' : problem.message}</p>`;
            }
        });

        input.addEventListener('keydown', event => {
            if (event.key === 'Enter') {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        document.addEventListener('click', event => {
            if (!form.contains(event.target)) dropdown.hidden = true;
        });
        document.addEventListener('keydown', event => {
            if (event.key === 'Escape') dropdown.hidden = true;
        });
        input.addEventListener('focus', () => {
            if (dropdown.childElementCount) dropdown.hidden = false;
        });
    }

    function createAIResult(recommendation) {
        const game = recommendation.game;
        const link = document.createElement('a');
        link.className = 'ai-dropdown-result';
        link.href = `game-detail.html?id=${encodeURIComponent(game.id)}`;
        const image = document.createElement('img');
        image.src = game.thumbnailUrl || 'Images/game-screen.png';
        image.alt = '';
        image.addEventListener('error', () => { image.src = 'Images/game-screen.png'; }, { once: true });
        const text = document.createElement('span');
        text.innerHTML = '<strong></strong><small class="ai-result-meta"></small><small class="ai-result-reason"></small>';
        text.querySelector('strong').textContent = game.title;
        text.querySelector('.ai-result-meta').textContent = [game.genre, game.platform].filter(Boolean).join(' · ');
        text.querySelector('.ai-result-reason').textContent = recommendation.reason;
        link.append(image, text);
        return link;
    }
});
