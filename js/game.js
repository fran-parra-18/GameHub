document.addEventListener('DOMContentLoaded',()=>{

    const btnControl = document.querySelector('.control-icon');
    const controlMenu = document.querySelector('.controls-menu');
    const closeControl = document.querySelector('.cruz-control');
    
    const btnShare = document.querySelector('.share-icon');
    const shareMenu = document.querySelector('.share-menu');
    const closeShare = document.querySelector('.cruz-share');
    
    
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
    
    })