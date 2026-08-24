(function () {
    const TOKEN_KEY = 'gamehub.jwt';
    const USER_KEY = 'gamehub.user';
    const configuredBase = window.GAMEHUB_API_BASE_URL || localStorage.getItem('gamehub.apiBaseUrl');
    const baseUrl = (configuredBase || 'http://localhost:8080').replace(/\/$/, '');

    class ApiError extends Error {
        constructor(status, message, fieldErrors) {
            super(message);
            this.name = 'ApiError';
            this.status = status;
            this.fieldErrors = fieldErrors || {};
        }
    }

    async function request(method, path, body) {
        const headers = { Accept: 'application/json' };
        const token = localStorage.getItem(TOKEN_KEY);
        if (token) headers.Authorization = `Bearer ${token}`;
        if (body !== undefined) headers['Content-Type'] = 'application/json';
        let response;
        try {
            response = await fetch(`${baseUrl}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
        } catch (_) {
            throw new ApiError(0, 'No pudimos conectar con GameHub. Verificá que el servidor esté disponible.');
        }
        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await response.json() : null;
        if (!response.ok) {
            const messages = {
                400: 'Revisá los datos ingresados.', 401: 'Necesitás iniciar sesión para continuar.',
                404: 'No encontramos el recurso solicitado.', 409: payload?.message || 'Ese usuario o correo ya está registrado.',
                502: 'El servicio de recomendaciones no está disponible temporalmente.',
                503: 'AI Game Finder no está configurado temporalmente.'
            };
            throw new ApiError(response.status, messages[response.status] || 'Ocurrió un problema al procesar la solicitud.', payload?.fieldErrors);
        }
        return payload;
    }

    window.GameHubApi = {
        baseUrl,
        get: path => request('GET', path), post: (path, body) => request('POST', path, body), delete: path => request('DELETE', path),
        getToken: () => localStorage.getItem(TOKEN_KEY),
        getUser: () => { try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (_) { return null; } },
        setSession: auth => { localStorage.setItem(TOKEN_KEY, auth.token); localStorage.setItem(USER_KEY, JSON.stringify(auth.user)); },
        clearSession: () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); }, ApiError
    };
})();
