document.addEventListener('DOMContentLoaded',()=>{


    const buttons=document.querySelectorAll('button');

    buttons.forEach(button=>{
        button.addEventListener('click',function(){
            event.preventDefault();
            console.log("hdjlfsdhlfds")
            const disabled = this.getAttribute('data-disabled');
            if(disabled===null){
                const url= this.getAttribute('data-url');
                window.location.href=url;
            }
        })
    })

})