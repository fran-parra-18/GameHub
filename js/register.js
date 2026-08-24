document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.form-register');
    const submit = form.querySelector('.login-button');
    const error = document.getElementById('formError');
    form.addEventListener('submit', async event => {
        event.preventDefault(); error.textContent = '';
        const password = document.getElementById('registerPassword').value;
        if (password !== document.getElementById('registerPasswordRepeat').value) { error.textContent = 'Las contraseñas no coinciden.'; return; }
        submit.disabled = true;
        try {
            const auth = await GameHubApi.post('/api/users/register', {
                username: document.getElementById('registerUsername').value.trim(),
                email: document.getElementById('registerEmail').value.trim(), password
            });
            GameHubApi.setSession(auth); form.classList.add('success-hidden');
            document.getElementById('successMessage').classList.remove('success-hidden');
            setTimeout(() => { window.location.href = 'index.html'; }, 900);
        } catch (problem) { error.textContent = problem.fieldErrors ? Object.values(problem.fieldErrors)[0] : problem.message; submit.disabled = false; }
    });
});
    
