class Ficha{
    constructor(player, x, y, radius) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.startX=x;
        this.startY=y;
        this.radius=radius;
        this.resaltado=false;
    }

    setResaltado(x){
        this.resaltado=x;
    }

    draw(ctx) {
        if (!this.player.image.complete) {
            // Si la imagen aún no está cargada, espera y vuelve a intentar después
            this.player.image.onload = () => this.draw(ctx);
            return;
        }
        ctx.save()
        ctx.beginPath();        
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip()
        ctx.drawImage(this.player.image,this.x-this.radius,this.y-this.radius,this.radius*2,this.radius*2);
        //ctx.fillStyle = this.player.color;        
        ctx.restore();
        
    }

    animateDrop(piece, targetRow, col, board, game) {
        const targetY = (targetRow * board.cellSize + board.marginTop) + board.cellSize / 2;
        const dropSpeed = 10;  // Velocidad de caída ajustable
        let lastTime = 0;
    
        const drop = (time) => {
            if (!lastTime) lastTime = time;
            const delta = time - lastTime;
            lastTime = time;
    
            piece.y += dropSpeed * (delta / 16);
    
            // Comprueba si la ficha está cerca del objetivo
            if (piece.y >= targetY - dropSpeed) {
                piece.y = targetY;  // Ajusta la posición final exacta
                board.checkForWin(piece, targetRow, col);
                game.draw();  // Dibuja la última posición de la ficha
                return;  // Termina la animación
            }
    
            game.draw();  // Redibuja solo si la pieza no ha alcanzado el objetivo
            requestAnimationFrame(drop);
            
        };
    
        requestAnimationFrame(drop);
    }

    returnPieceToStart(draggedPiece, game) {

        draggedPiece.x = draggedPiece.startX;
        draggedPiece.y = draggedPiece.startY;

        game.draw();

    }
    getPlayerName() {
        return this.player.name;
    }
}
