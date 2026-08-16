document.addEventListener('DOMContentLoaded',()=>{
    document.querySelector('.login-button').addEventListener('click', e=> {
        e.preventDefault();
        const form = document.querySelector('.form-register');
        const successMessage = document.getElementById('successMessage');
      
        // Aplicar la animación de desvanecimiento al formulario
        form.classList.add('fade-out');
      
        // Después de la animación, ocultamos el formulario y mostramos el mensaje
        setTimeout(() => {
          form.classList.add('success-hidden'); // Ocultar el formulario
          successMessage.classList.remove('success-hidden'); // Mostrar mensaje
          successMessage.classList.add('success-visible'); // Activar la animación de entrada
        }, 800); // La duración de la animación 'moveAway'
        setTimeout(()=>{
            const url= document.querySelector('.login-button').getAttribute('data-url');
            window.location.href=url;
        },3000)
      
      });
})
    

