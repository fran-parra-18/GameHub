const btnGradient= document.querySelectorAll('.button-gradient-tracking');

btnGradient.forEach(btn=>{
    btn.addEventListener('mousemove',e =>{
        const rect = e.target.getBoundingClientRect();//Returns the size of an element and its position relative to the viewport
        //A DOMRect object with eight properties:
        //left, top, right, bottom, x, y, width, height
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        btn.style.setProperty('--x', x + 'px');
        btn.style.setProperty('--y', y + 'px');
    })
})


btnGradient.forEach(btn=>
    btn.addEventListener('click',()=>{
        
        let buttonText = btn.querySelector('.button-text');
        if (buttonText && buttonText.textContent.includes('Jugar')){
            buttonText.innerHTML = 'Cargando...';
        }else if(buttonText && buttonText.textContent.includes('Cargando...')){
            buttonText.innerHTML ='Jugar';
        }
    })
)

