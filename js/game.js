document.addEventListener('DOMContentLoaded',()=>{

    const btnControl = document.querySelector('.control-icon');
    const controlMenu = document.querySelector('.controls-menu');
    const closeControl = document.querySelector('.cruz-control');
    
    const btnShare = document.querySelector('.share-icon');
    const shareMenu = document.querySelector('.share-menu');
    const closeShare = document.querySelector('.cruz-share');
    
    const btnCorazonVacio = document.querySelector('.icon-heart-empty');
    const btnCorazonLleno = document.querySelector('.icon-heart-full');
    
    btnControl.addEventListener('click', () => {
        controlMenu.classList.toggle('show');
    })
    
    btnShare.addEventListener('click', () => {
        shareMenu.classList.toggle('show');
    })
    
    closeControl.addEventListener('click', ()=>{
        controlMenu.classList.toggle('show');
    });
    
    closeShare.addEventListener('click', ()=>{
        shareMenu.classList.toggle('show');
    });
    
    btnCorazonVacio.addEventListener('click', ()=>{
        btnCorazonVacio.classList.toggle('show');
        btnCorazonLleno.classList.toggle('show');
    });
    
    btnCorazonLleno.addEventListener('click', ()=>{
        btnCorazonVacio.classList.toggle('show');
        btnCorazonLleno.classList.toggle('show');
    })
    
    })