document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.form-login');
    const submit = form.querySelector('.login-button');
    const error = document.getElementById('formError');
    form.addEventListener('submit', async event => {
        event.preventDefault(); error.textContent = ''; submit.disabled = true;
        try {
            const auth = await GameHubApi.post('/api/users/login', {
                email: document.getElementById('loginIdentifier').value.trim(),
                password: document.getElementById('loginPassword').value
            });
            GameHubApi.setSession(auth); window.location.href = 'index.html';
        } catch (problem) { error.textContent = problem.message; }
        finally { submit.disabled = false; }
    });
});
