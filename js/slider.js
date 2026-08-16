document.addEventListener('DOMContentLoaded', () => {
    let currentSlide=0;
    const slides= document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');
    const totalSlides = slides.length;
    const buttons=document.querySelectorAll('.slider-button');
    let isDragging=false;
    let startX=0;
    let endX=0;
    let slideInterval;



    document.querySelector('.next').addEventListener('click', () => {
        changeSlide(currentSlide+1);
    });
    
    document.querySelector('.prev').addEventListener('click', () => {
        changeSlide(currentSlide-1);
    });
    

    slides.forEach(slide=>{
        slide.addEventListener('mousedown',e=>{
        })
    })



    
    slides.forEach(slide=>{
        slide.addEventListener('touchstart',(e)=>{
            isDragging=true;
            startX = e.touches[0].clientX;
        })
    })

    document.addEventListener('touchmove', (e) => {
        if (isDragging) {
            endX = e.touches[0].clientX;
        }
    });
  
    document.addEventListener('touchend', () => {
        if (isDragging) {
            isDragging = false;
            const distance = startX - endX;
            if (distance > 50 && endX!==0) {
                changeSlide(currentSlide + 1);
                endX=0;
            }
             else if (distance < -50 && endX!==0) {
                changeSlide(currentSlide - 1);
                endX=0;
            }
        }
    });

    dots.forEach((dot,index)=>{
        dot.addEventListener('click',()=>{
            changeSlide(index);
        })
    })
    
    buttons.forEach(button=>{
        button.addEventListener('click',function(){
            const disabled = this.getAttribute('data-disabled');
            if(disabled===null){
                const url= this.getAttribute('data-url');
                window.location.href=url;
            }
        })
    })

    function changeSlide(index){
        if(index>=totalSlides){
            currentSlide=0;
        }
        else if(index<0){
            currentSlide=totalSlides-1;
        }
        else{
            currentSlide=index;
        }
        document.querySelector('.slides').style.transform=`translateX(-${currentSlide*100}%)`;

        slides.forEach((slide,i)=>{
            slide.classList.toggle('active',i===currentSlide);
        })

        dots.forEach((dot,i)=>{
            dot.classList.toggle('active',i===currentSlide);
    })
    resetInterval();
    }

    function resetInterval(){
        clearInterval(slideInterval);
        sliderInterval();
    }
    function sliderInterval(){
        slideInterval=setInterval(()=>{
            changeSlide(currentSlide+1);
        },5000)
    }
  


});