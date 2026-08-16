document.addEventListener('DOMContentLoaded',()=>{

// animación de carga de página
var percent = document.querySelector('.percent');
var progress = document.querySelector('.progress');
var count = 4;
var per = 16;
var loading = setInterval(animate, 43);

function animate() {
    if (count >= 100 && per >= 195) { 
        count = 100; // Aseguramos que count no pase de 100
        per = 195; // Aseguramos que per no pase de 195
        progress.style.width = per + 'px';
        percent.textContent = count + '%';
        text.classList.add("add");
        clearInterval(loading);
    } else {
        per = per + 1.8;
        count = count + 1;
        if (count > 100) count = 100; // Limitar count a 100
        progress.style.width = per + 'px';
        percent.textContent = count + '%';
    }
}

setTimeout(function() {
    document.querySelector('.loader-container').style.display = 'none';
    document.querySelector('.page-container').style.display = 'block';
    clearInterval(loading);
    // Aquí se ejecuta el segundo script
    initializeCarousel();

}, 5000); // 5000 ms = 5 segundos

// Función para inicializar el carrusel
function initializeCarousel() {
    const carouselContainers = document.querySelectorAll('.carousel-container');
    const cardWidth = document.querySelector('.card').offsetWidth + 15; // Ancho de la card + margen

    carouselContainers.forEach(container => {
        const carousel = container.querySelector('.carousel');
        container.querySelector('.right-arrow').addEventListener('click', () => {
            const maxScrollLeft = carousel.scrollWidth - carousel.clientWidth;

            if (carousel.scrollLeft >= maxScrollLeft) {
                addSkew();
                carousel.scrollLeft = 0;
            } else {
                addSkew();
                carousel.scrollLeft += cardWidth;
            }
        });

        container.querySelector('.left-arrow').addEventListener('click', () => {
            if (carousel.scrollLeft <= 0) {
                addSkew();
                carousel.scrollLeft = carousel.scrollWidth - carousel.clientWidth;
            } else {
                carousel.scrollLeft -= cardWidth;
                addSkew();
            }
        });

        function addSkew() {
            carousel.querySelectorAll('.card').forEach(card => {
                card.classList.add("addSkew");
            });

            setTimeout(() => {
                carousel.querySelectorAll('.card').forEach(card => {
                    card.classList.add("addSkewEnd");
                });
            }, 300);

            setTimeout(() => {
                carousel.querySelectorAll('.card').forEach(card => {
                    card.classList.remove("addSkewEnd");
                    card.classList.remove("addSkew");
                });
            }, 600);
        }
    });
}

    btnDeadpool=document.getElementById('deadpool-button');

    btnDeadpool.addEventListener('click',function(){
        const disabled = this.getAttribute('data-disabled');
            const url= this.getAttribute('data-url');
            window.location.href=url;
    })

    btnNumberBlocks=document.getElementById('number-blocks');

    btnNumberBlocks.addEventListener('click',function(){
            const url= this.getAttribute('data-url');
            window.location.href=url;
    })
})
