
document.addEventListener('DOMContentLoaded', async () => {
    let canvas = document.getElementById('gameCanvas');
    let ctx = canvas.getContext('2d');

    let juego = new Juego(canvas,ctx);  // Inicializa el juego con el ID del canvas

    await juego.loadSources();

    
    juego.canvas.addEventListener('mousedown', (event) => {
        if (juego.xEnLinea != 0) {
            let rect = juego.canvas.getBoundingClientRect();
            let x = event.clientX - rect.left;
            let y = event.clientY - rect.top;
    
            let currentPieces = juego.currentPlayer === 0 ? juego.piecePlayer1 : juego.piecePlayer2;
            
            for (let i = 0; i < currentPieces.length; i++) {
                let piece = currentPieces[i];
                let distance = Math.sqrt((x - piece.x) ** 2 + (y - piece.y) ** 2);
    
                if (distance < juego.radius) {
                    juego.draggedPiece = piece; // Asigna la ficha seleccionada para ser arrastrada
                    juego.draw(); // Redibuja para actualizar la vista sin la ficha
                    break;
                }
            }
        }
    });

   juego.canvas.addEventListener('mousemove', (event) => {
        
        if (juego.draggedPiece) {
                let rect = canvas.getBoundingClientRect();
                juego.draggedPiece.x = event.clientX - rect.left;
                juego.draggedPiece.y = event.clientY - rect.top;

                juego.updateActiveColumn(juego.draggedPiece.x, juego.draggedPiece.y);

                juego.draw(); // Redibujar el canvas con la ficha moviéndose
        }
    });

    juego.canvas.addEventListener('mouseup', (event) => {
        if (juego.draggedPiece) {
            let rect = juego.canvas.getBoundingClientRect();
            let x = event.clientX - rect.left;
            let y = event.clientY - rect.top;

            let pieceDropped = false;
    
            juego.activeColumn=null;
            // Verificar si la ficha está sobre una columna
            for (let col = 0; col < juego.board.cols; col++) {
                let hintX = juego.board.marginLeft + col * juego.board.cellSize;
                if (x > hintX && x < hintX + juego.board.cellSize && (y < juego.board.marginTop && y > 35)) {
                    // Colocar la ficha en la columna
                    if (juego.board.dropDisc(col, juego.players[juego.currentPlayer])) {
                        
                        let currentPieces = juego.currentPlayer === 0 ? juego.piecePlayer1 : juego.piecePlayer2;
                        let index = currentPieces.indexOf(juego.draggedPiece);
                        if (index > -1) {
                            currentPieces.splice(index, 1);

                            currentPieces.forEach((piece, i) => {
                                piece.y = 130 + i * 20; // Ajusta 130 y 20 a la posición inicial y el espacio entre fichas
                                piece.startY=piece.y
                                
                            });
                        }
                        
                        
                        juego.switchTurns();
                        pieceDropped = true;
                    }
                    break;
                }
            }
            
            // Si no se ha colocado la ficha, devolverla a su lugar y al array
            if (!pieceDropped) {          
                juego.draggedPiece.returnPieceToStart(juego.draggedPiece, juego);  // Volver a la posición original
            }
            
            juego.draggedPiece = null; // Reseteamos el arrastre
            juego.draw(); // Redibuja el tablero
            
        }
    });


    juego.canvas.addEventListener('click', function(event) {
        let rect = juego.canvas.getBoundingClientRect();
        let x = event.clientX - rect.left;
        let y = event.clientY - rect.top;
        
        let startY = 50;
        let margin = 20;
        // Coordenadas del botón de "Jugar 4 en línea"
        let buttonX = juego.canvas.width / 2 - juego.buttonWidth / 2;
        
        if(juego.xEnLinea!=0){ 
            // Coordenadas del botón de "Reiniciar"
            let restartButtonX = canvas.width - juego.buttonWidth - 20;
            let restartButtonY = canvas.height - juego.buttonHeight - 20;
            if (x >= restartButtonX && x <= restartButtonX + juego.buttonWidth && y >= restartButtonY && y <= restartButtonY + juego.buttonHeight) {
                juego.timeLeft=300;
                juego.startGame(); // Reiniciar juego
            }

            // Coordenadas del botón de "Volver"
            let backButtonX = 20;
            let backButtonY = 20;
            if (x >= backButtonX && x <= backButtonX + juego.buttonBackSize && y >= backButtonY && y <= backButtonY + juego.buttonBackSize) {
                juego.xEnLinea=0;
                clearInterval(juego.timerInterval); // Limpia el intervalo anterior
                juego.draw() // Función que maneja el volver atrás
            }
        }
    });

});