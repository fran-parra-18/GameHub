class button {
    constructor(ctx,x,y,width,height,cant){
        this.ctx=ctx;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.cant=cant;
        this.hovered = false;
        this.imageDefault=new Image();
        this.imageDefault.src="Images/Juego/yellowbutton.png"
        this.imageHover=new Image();
        this.imageHover.src="Images/Juego/redbutton.png"
    }


    drawSingleButton() {
        let actualImage;
        if (this.hovered){
            actualImage=this.imageHover;
        }else{
            actualImage=this.imageDefault;
        }
        this.ctx.drawImage(
            actualImage,
            this.x, // X posición para centrar la imagen
            this.y,                                   // Y posición
            this.width,                            // Ancho de la imagen
            this.height                            // Alto de la imagen
        );
               // console.log("drawing..")
            // Dibuja el texto sobre la imagen
            this.ctx.fillStyle = "#000000";                 // Color del texto
            this.ctx.font = "20px 'Dekko', cursive";        // Fuente personalizada
            this.ctx.textAlign = "center";
            this.ctx.textBaseline = "middle";
            this.ctx.fillText(`${this.cant} en Línea`, this.x+ this.width/2, this.y + this.height / 2);
     
    }

    isCursorOver(x,y){
        
        if(x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height){
            return true;
        }else{
            return false;
        }
    }

    getCant(){
        return this.cant;
    }
    getImage(){
        return this.imageDefault;
    }
}