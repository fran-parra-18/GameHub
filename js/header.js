document.addEventListener('DOMContentLoaded',()=>{

    const menuBtn = document.querySelector('.menu-button');
    const sidebar = document.querySelector('.sidebar');
    const closeMenu = document.querySelector('.icon-cruz-menu');
    
    const profileBtn = document.querySelector('.user-icon');
    const profile = document.querySelector('.user-menu');
    const closeProfile = document.querySelector('.icon-cruz-profile');
    
    menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('show');
    });
    
    closeMenu.addEventListener('click', ()=>{
        sidebar.classList.toggle('show');
    });
    
    profileBtn.addEventListener('click', ()=>{
        profile.classList.toggle('show');
    });
    
    closeProfile.addEventListener('click', ()=>{
        profile.classList.toggle('show');
    });
    })