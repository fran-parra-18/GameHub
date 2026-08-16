class BotonFicha extends button {
    constructor(ctx, x, y, width, height, cant, radius, image) {
        super(ctx, x, y, width, height, cant);
        this.radius = radius;
        this.imageDefault = image;
        this.clicked = false;
    }

    drawSingleButton() {
        let actualImage = this.imageDefault;
        
        if (this.clicked) {
            // Efecto de resplandor solo si está en estado clicked
            this.ctx.shadowBlur = 25;
            this.ctx.shadowColor = 'rgba(0, 255, 100,1)';
            this.ctx.shadowOffsetX = 0;
            this.ctx.shadowOffsetY = 0;
        } else {
            // Restablece el efecto si no está seleccionado
            this.ctx.shadowBlur = 0;
            this.ctx.shadowColor = 'rgba(0, 0, 0, 0)';
        }

        // Dibuja el botón con la imagen
        this.ctx.drawImage(
            actualImage,
            this.x, 
            this.y, 
            this.width, 
            this.height
        );

        // Restablece las propiedades de sombra después de dibujar
        this.ctx.shadowBlur = 0;
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0)';
    }

    setClicked(boolean) {
        this.clicked = boolean;
    }
}