class Juego{
    constructor(canvas,ctx) {

        this.canvas = canvas;
        this.ctx = ctx;
        this.totalFichas= 0;
        this.board = null;
       


        this.totalFichas = 0;
        //Fondo juego

        this.fondoImage = new Image();
        this.fondoImage.src = "Images/Juego/fondofox.png" 

        this.testBg1Image = new Image();
        this.testBg1Image.src = "Images/Juego/test-bg.png" 

        this.testBg2Image = new Image();
        this.testBg2Image.src = "Images/Juego/test-bg2.png" 

        //Turnos

        this.turnoWolverine = new Image();
        this.turnoWolverine.src = "Images/Juego/turnoWolverine.png"

        this.turnoDeadpool = new Image();
        this.turnoDeadpool.src = "Images/Juego/turnoDeadpool.png"

        this.redButton= new Image();
        this.redButton.src="Images/Juego/redbutton.png";

        this.yellowButton= new Image();
        this.yellowButton.src="Images/Juego/yellowbutton.png";


        this.imageDeadpool= new Image();
        this.imageDeadpool.src="Images/Juego/deadpoolficha1.png"

        this.imageWolverine= new Image();
        this.imageWolverine.src="Images/Juego/fichawolverine1.png"

        //imagenes texto

        this.imageElegirFichas=new Image();
        this.imageElegirFichas.src="Images/Juego/eligefichas.png"

        this.imageDeadpoolText=new Image();
        this.imageDeadpoolText.src="Images/Juego/deadpooltext.png"

        this.imageWolverineText=new Image();
        this.imageWolverineText.src="Images/Juego/wolverinetext.png"

        this.imageSeleccionFicha=new Image();
        this.imageSeleccionFicha.src="Images/Juego/seleccionaficha.png"

        
        //efecto
        this.effectImageD = new Image();  
        this.effectImageD.src = "Images/Juego/efectod.png";

        this.effectImageW= new Image();  
        this.effectImageW.src = "Images/Juego/efectoW.png";

        this.activeColumn = null; // Columna actual sobre la que está la ficha


        this.players = [new Jugador('Deadpool',this.imageDeadpool,this.effectImageD), new Jugador('Wolverine',this.imageWolverine, this.effectImageW)]; 

        this.currentPlayer = 0;

        this.xEnLinea =0;

        this.draggedPiece = null; // Ficha que se está arrastrando

        this.radius = null;// Tamaño de las fichas que se mostrarán arriba

        //botones
        this.isHovering = false; // Estado del hover
 
        this.buttons=[]
        this.initButtons();

        this.botonesFicha1=[];
        this.initBotonesFichaP1();
     
        this.botonesFicha2=[]
        this.initBotonesFichaP2()
  
        this.buttonWidth = 120;
        this.buttonHeight = 60;

        this.buttonBackSize = 30;

        this.timeLeft = 300;
        this.timerInterval = null;

        this.tiempoagotado = new Image();
        this.tiempoagotado.src = "Images/Juego/tiempoagotado.png"

        this.ficha1Seleccionada = false;
        this.ficha2Seleccionada = false;

        this.imagesToLoad = [
            this.fondoImage, this.testBg1Image, this.testBg2Image,
            this.turnoWolverine, this.turnoDeadpool,
            this.yellowButton, this.redButton,
            this.imageDeadpool, this.imageWolverine, this.effectImageD,this.effectImageW,this.imageDeadpoolText,this.imageWolverineText,this.imageElegirFichas,this.imageSeleccionFicha,
        this.tiempoagotado
        ];
        this.loadedImagesCount = 0;
        this.setupImages();

        this.canvas.addEventListener("mousemove", (event) => this.handleHover(event));

        this.canvas.addEventListener("click", (event) => this.handleClick(event));
        

    }

    async loadSources() {
        await this.setupImages();
        await this.setupFonts();
        console.log("All resources loaded. Starting the game...");
        this.startGame(); // Aquí puedes iniciar tu juego
    }

    setupImages() {
        return new Promise((resolve, reject) => {
            this.imagesToLoad.forEach(image => {
                image.onload = () => {
                    this.loadedImagesCount++;
                    if (this.loadedImagesCount === this.imagesToLoad.length) {
                        console.log("All images loaded");
                        resolve(); // Resuelve la promesa cuando todas las imágenes estén cargadas
                    }
                };
                image.onerror = () => {
                    console.error("Failed to load image:", image.src);
                    reject(new Error("Image loading failed"));
                };
                image.src = image.src; // Asigna la fuente para iniciar la carga
            });
        });
    }

    async setupFonts() {
        const font = new FontFace('Luckiest Guy', 'url(./fonts/LuckiestGuy-Regular.ttf)'); // Asegúrate de que la ruta y el nombre del archivo sean correctos
        await font.load();
        document.fonts.add(font);
        console.log("Font loaded");
    }
    

    // Dibuja el tablero y las fichas
    draw() {
        

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
            
            this.ctx.drawImage(
                this.fondoImage,        // Imagen del tablero
                0, 0,             // Posición X e Y de la imagen en el canvas
                this.canvas.width,        // Ancho del tablero (ajustado al tamaño del canvas)
                this.canvas.height+30     // Alto del tablero (ajustado al tamaño del canvas)
            );

        if(this.xEnLinea!=0){

            this.ctx.drawImage(
                this.testBg1Image,        // Imagen del tablero
                235, 320,             // Posición X e Y de la imagen en el canvas
                136,        // Ancho del tablero (ajustado al tamaño del canvas)
                415      // Alto del tablero (ajustado al tamaño del canvas)
            );
                
            this.ctx.drawImage(
                this.testBg2Image,        // Imagen del tablero
                ((this.board.cellSize*this.board.cols)+this.board.marginLeft)-37, 320,             // Posición X e Y de la imagen en el canvas
                136,        // Ancho del tablero (ajustado al tamaño del canvas)
                415    // Alto del tablero (ajustado al tamaño del canvas)
            );
            
            
            this.board.draw(this.ctx);
            this.drawPlayerPieces();
            this.displayTurn(); 

            this.drawEffect();


        // Dibuja el boton de reiniciar 
        let restartButtonX = this.canvas.width - this.buttonWidth - 20;
        let restartButtonY = this.canvas.height - this.buttonHeight - 20;

        // Sombras
        this.ctx.shadowColor = "rgba(0, 0, 0, 0.8)";
        this.ctx.shadowOffsetX = 6;
        this.ctx.shadowOffsetY = 6;
        this.ctx.shadowBlur = 10;

        // Esquinas redondeadas
        this.ctx.fillStyle = "#E70000"; // Rojo para reiniciar
        this.ctx.beginPath();
        this.ctx.moveTo(restartButtonX + 10, restartButtonY); // esquina superior izquierda
        this.ctx.lineTo(restartButtonX + this.buttonWidth - 10, restartButtonY); // cima
        this.ctx.quadraticCurveTo(restartButtonX + this.buttonWidth, restartButtonY, restartButtonX + this.buttonWidth, restartButtonY + 10); // esquina superior derecha
        this.ctx.lineTo(restartButtonX + this.buttonWidth, restartButtonY + this.buttonHeight - 10); // lado derecho
        this.ctx.quadraticCurveTo(restartButtonX + this.buttonWidth, restartButtonY + this.buttonHeight, restartButtonX + this.buttonWidth - 10, restartButtonY + this.buttonHeight); // esquina inferior derecha
        this.ctx.lineTo(restartButtonX + 10, restartButtonY + this.buttonHeight); // abajo
        this.ctx.quadraticCurveTo(restartButtonX, restartButtonY + this.buttonHeight, restartButtonX, restartButtonY + this.buttonHeight - 10); // esquina inferior izquierda
        this.ctx.lineTo(restartButtonX, restartButtonY + 10); // lado izquierdo
        this.ctx.quadraticCurveTo(restartButtonX, restartButtonY, restartButtonX + 10, restartButtonY); // cima
        this.ctx.closePath();
        this.ctx.fill();

        this.ctx.fillStyle = "#FFFFFF";
        this.ctx.font = "bold 18px Arial"; // Cambia la fuente y tamaño según tu preferencia
        this.ctx.textAlign = "center";
        this.ctx.textBaseline = "middle";
        this.ctx.fillText("Reiniciar", restartButtonX + this.buttonWidth / 2, restartButtonY + this.buttonHeight / 2);
        this.ctx.fillStyle = "#FFFFFF"; 
        

        // Dibuja el boton back
        let backButtonX = 20;
        let backButtonY = 20;
        let buttonWidth = this.buttonBackSize;
        let buttonHeight = this.buttonBackSize;

        // Sombras
        this.ctx.shadowColor = "rgba(0, 0, 0, 0.8)"; 
        this.ctx.shadowOffsetX = 4; 
        this.ctx.shadowOffsetY = 4; 
        this.ctx.shadowBlur = 5; 

        // Esquinas redondeadas
        this.ctx.fillStyle = "#E70000"; 
        this.ctx.beginPath();
        this.ctx.moveTo(backButtonX + 10, backButtonY); // esquina superior izquierda
        this.ctx.lineTo(backButtonX + buttonWidth - 10, backButtonY); // cima
        this.ctx.quadraticCurveTo(backButtonX + buttonWidth, backButtonY, backButtonX + buttonWidth, backButtonY + 10); // esquina superior derecha
        this.ctx.lineTo(backButtonX + buttonWidth, backButtonY + buttonHeight - 10); // lado derecho
        this.ctx.quadraticCurveTo(backButtonX + buttonWidth, backButtonY + buttonHeight, backButtonX + buttonWidth - 10, backButtonY + buttonHeight); // esquina inferior derecha
        this.ctx.lineTo(backButtonX + 10, backButtonY + buttonHeight); // abajo
        this.ctx.quadraticCurveTo(backButtonX, backButtonY + buttonHeight, backButtonX, backButtonY + buttonHeight - 10); // esquina inferior izquierda
        this.ctx.lineTo(backButtonX, backButtonY + 10); // lado izquierdo
        this.ctx.quadraticCurveTo(backButtonX, backButtonY, backButtonX + 10, backButtonY); // cima
        this.ctx.closePath();
        this.ctx.fill();

        // Texto estilizado
        this.ctx.fillStyle = "#FFFFFF";
        this.ctx.font = "bold 20px Arial"; 
        this.ctx.textAlign = "center";
        this.ctx.textBaseline = "middle";
        this.ctx.fillText("X", backButtonX + buttonWidth / 2, backButtonY + buttonHeight / 2);

            if (this.draggedPiece) {
                this.draggedPiece.draw(this.ctx);
            }


            this.drawTimer();
        }else{

            this.drawButtons();  
            this.initBotonesFichaP1();    
            this.initBotonesFichaP2();

            if (!this.ficha1Seleccionada || !this.ficha2Seleccionada) {
                this.ctx.drawImage(
                    this.imageSeleccionFicha,415,630,500,100
                );
            }

        }
        
        if(this.board.finishedBoard){
            this.board.finishedBoard=false;
            this.startGame();
        }
    }

    
    startGame() {
        this.canvas.style.cursor = "default";
        this.board = new Tablero(
            this.ctx,          // Contexto del canvas
            this.canvas,
            this.xEnLinea,     // Configuración de x en línea
            110,               // marginTop
            75,                // marginBottom
            0,                 // marginRight
            this.canvas.width / 4, // marginLeft
            this
        );
        
        this.currentPlayer = 0;  // Reiniciamos el turno al primer jugador
        this.radius = this.board.getCellSize() / 2 - 9;
        this.generetePlayerPieces(); 
        this.draw();            // Redibujar el tablero con las nuevas dimensiones
        
        if(this.xEnLinea!=0){
            this.startTimer()
        }
    }

    
    drawPlayerPieces() {
        this.ctx.clearRect(0,0,this.width,this.height);
        this.piecePlayer1.forEach(piece => piece.draw(this.ctx, this.board.cellSize));
        this.piecePlayer2.forEach(piece => piece.draw(this.ctx, this.board.cellSize));
    }

    generetePlayerPieces() {
        this.piecePlayer1 = []; 
        this.piecePlayer2 = [];
        const spacingY = 20;

        this.totalFichas= (this.board.cols * this.board.rows)/2;

        this.players.forEach((player, index) => {
            let x = index === 0 ? 200 : 1150;
            let y = 130; 

            for (let i = 0; i < this.totalFichas; i++) {

                let piece = new Ficha(player, x, y, this.radius);
                if (index === 0) {
                    this.piecePlayer1.push(piece);
                } else {
                    this.piecePlayer2.push(piece);
                }
                y += spacingY; 
            }
        });
        
    }
    

    

    

    switchTurns() {
        this.currentPlayer = (this.currentPlayer + 1) % 2;
    }

    displayTurn() {
        let posX = 460;     // Posición horizontal centrada
        let posY = 0; // Posición vertical entre el tablero y las fichas
        let turno=null;

        if(this.currentPlayer==0){
            turno = this.turnoDeadpool
        }else{
            turno = this.turnoWolverine
        }
        
        this.ctx.drawImage(
            turno,        
            posX, posY,             
            391,        
            70    
        );
    }





    drawButtons() {

        
        this.ctx.drawImage(
            this.imageElegirFichas,
            300, // X posición para centrar la imagen
            -10,                                   // Y posición
            700,                            // Ancho de la imagen
            125                            // Alto de la imagen
        );

        this.ctx.drawImage(
            this.imageDeadpoolText,
            200, // X posición para centrar la imagen
            150,                                   // Y posición
            200,                            // Ancho de la imagen
            50                            // Alto de la imagen
        );

        this.ctx.drawImage(
            this.imageWolverineText,
            900, // X posición para centrar la imagen
            150,                                   // Y posición
            200,                            // Ancho de la imagen
            50                            // Alto de la imagen
        );
  



        // Configuraciones básicas de los botones  

        for(const button of this.botonesFicha1){
            button.drawSingleButton();
        }
        for(const button of this.botonesFicha2){
            button.drawSingleButton();
        }
    
    
        for (const btn of this.buttons) {
            btn.drawSingleButton(); // Dibuja el botón
        }

        // Colores y estilos
        this.ctx.fillStyle = "#4CAF50"; // Verde para el botón
        this.ctx.font = "20px Arial";
        this.ctx.textAlign = "center";
        this.ctx.textBaseline = "middle";
    }

    drawSingleButton(yPosition,xPosition, buttonText,isHovered=false) {
        if(isHovered==true){

            this.ctx.drawImage(
                this.redButton,
                xPosition, // X posición para centrar la imagen
                yPosition,                                   // Y posición
                this.buttonWidth,                            // Ancho de la imagen
                this.buttonHeight                            // Alto de la imagen
            );
        }else{
            this.ctx.drawImage(
                this.yellowButton,
                xPosition, // X posición para centrar la imagen
                yPosition,                                   // Y posición
                this.buttonWidth,                            // Ancho de la imagen
                this.buttonHeight                            // Alto de la imagen
            );
        }

    
          // Dibuja el texto sobre la imagen
          this.ctx.fillStyle = "#000000";                 // Color del texto
          this.ctx.font = "20px 'Dekko', cursive";        // Fuente personalizada
          this.ctx.textAlign = "center";
          this.ctx.textBaseline = "middle";
            this.ctx.fillText(buttonText, xPosition+this.buttonWidth/2, yPosition + this.buttonHeight / 2);
    }

    drawEffect() {
        for (let col = 0; col < this.board.cols; col++) {
            
            if(col === this.activeColumn){
                this.ctx.drawImage(
                    this.players[this.currentPlayer].effect, 
                    col * this.board.cellSize + this.board.marginLeft+5,
                    this.board.marginTop-this.board.cellSize,
                    this.board.cellSize-10,
                    this.board.cellSize);
            }
        }
    }

    updateActiveColumn(mouseX,mouseY) {
        
        // Calcula la columna en función de la posición del mouse y el tamaño de la celda
        this.activeColumn = Math.floor((mouseX - this.board.marginLeft) / this.board.cellSize);
    
        // Asegura que la columna esté dentro de los límites del tablero
        if (this.activeColumn < 0 || this.activeColumn >= this.board.cols 
            || mouseY > this.board.marginTop || mouseY < 35 ) {
            this.activeColumn = null; // Fuera de los límites
        }
        // Redibuja el canvas para actualizar las flechas
        this.draw();

    }

    // Temporizador
    drawTimer() {
        this.ctx.save(); // Guarda el estado actual del contexto

        this.ctx.font = "45px Luckiest Guy";// Establecer estilo, tamaño y fuente
        this.ctx.fillStyle = "#FF0000";

        // Configuración de la sombra
        this.ctx.shadowColor = "black";
        this.ctx.shadowOffsetX = 6; // Ajusta para más profundidad
        this.ctx.shadowOffsetY = 6;
        this.ctx.shadowBlur = 8;

        this.ctx.fillText(this.timeLeft, this.canvas.width-90, 50); 
        

        this.ctx.restore(); // Restaura el estado original del contexto
    }

    startTimer() {
        clearInterval(this.timerInterval); // Limpia el intervalo anterior
        this.timeLeft = 300; // Resetea el tiempo a 60 segundos
        
        this.timerInterval = setInterval(() => {
            this.timeLeft--;
            this.draw(); // Redibuja el canvas, incluyendo el temporizador actualizado

            if (this.timeLeft <= 0) {
                clearInterval(this.timerInterval);
                this.board.showWinnerAnimation(this.tiempoagotado);
                this.startGame();
            }
        }, 1000);
    }


    handleHover(event) {
        if (this.xEnLinea === 0) {
            let rect = this.canvas.getBoundingClientRect();
            let x = event.clientX - rect.left;
            let y = event.clientY - rect.top;
    
            this.isHovering = false;
            let hoveredButton = null;

            for (const btn of this.botonesFicha1) {
                if (btn.isCursorOver(x, y)) {
                    btn.hovered = true;
                    this.isHovering = true;
                    hoveredButton = btn;
                } else {
                    btn.hovered = false;   
                }
            }
            
            for (const btn of this.botonesFicha2) {
                if (btn.isCursorOver(x, y)) {
                    btn.hovered = true;
                    this.isHovering = true;
                    hoveredButton = btn;
                } else {
                    btn.hovered = false;
                }
            }

            for (const btn of this.buttons) {
                if (btn.isCursorOver(x, y)) {
                    btn.hovered = true;
                    this.isHovering = true;
                    hoveredButton = btn;
                } else {
                    btn.hovered = false;
                    
                }
            }

            if (this.isHovering) {
                this.canvas.style.cursor = "pointer";
            } else {
                this.canvas.style.cursor = "default";
            }
            
            this.draw(); // Redraw the canvas with updated button colors

        }
    }

    
    handleClick(event){
        
        if (this.xEnLinea === 0) {
            let rect = this.canvas.getBoundingClientRect();
            let x = event.clientX - rect.left;
            let y = event.clientY - rect.top;

            

            for (const button of this.botonesFicha1) {
               
                if (button.isCursorOver(x,y)) {
                    this.botonesFicha1.forEach(button => button.setClicked(false));
                    this.players[0].setImage(button.getImage());
                    this.ficha1Seleccionada=true;
                    button.setClicked(true);
                    // You might want to reset the state after some time or perform other actions here
                }
            }

            for (const button of this.botonesFicha2) {
               
                if (button.isCursorOver(x,y)) {
                    this.botonesFicha2.forEach(button => button.setClicked(false));
                    this.ficha2Seleccionada=true;
                    this.players[1].setImage(button.getImage())
                    button.setClicked(true);

                    // You might want to reset the state after some time or perform other actions here

                } 
            }

            
                for (const btn of this.buttons) {
                    if (btn.isCursorOver(x, y)&&this.ficha1Seleccionada&&this.ficha2Seleccionada) {
                            this.xEnLinea = btn.getCant();
                            this.startGame();
                    }          
                } 
                
                this.draw(); // Redraw the canvas with updated button colors
                
            }
       }


    




    initButtons(){
        let width =300; // Ajusta el tamaño del botón
        let height = 60;

            for (let i = 0; i < 4; i++) {
                const x = 60 +(i*width*1.03); // Posición x
                const y = 550 ; // Posición y
                const btn=new button(this.ctx,x, y,width,height,i+4);
                this.buttons.push(btn);
            }
    }

    initBotonesFichaP1(){

        let x= 100
        let y=225
        let width =120; // Ajusta el tamaño del botón
        let height = 120;

        let ficha1=new Image()
        ficha1.src="Images/Juego/deadpoolficha1.png"
        let ficha2=new Image()
        ficha2.src="Images/Juego/deadpoolficha2.png"
        let ficha3=new Image()
        ficha3.src="Images/Juego/deadpoolficha3.png"

        let btn1=new BotonFicha(this.ctx,x, y,width,height,4,10,ficha1,true)
        this.botonesFicha1.push(btn1);

        let btn2=new BotonFicha(this.ctx,x+150, y,width,height,4,10,ficha2,false)
        this.botonesFicha1.push(btn2);

        let btn3=new BotonFicha(this.ctx,x+300, y,width,height,4,10,ficha3,false)
        this.botonesFicha1.push(btn3);
    }

    initBotonesFichaP2(){
        let x= 800
        let y=225
        let width =120; // Ajusta el tamaño del botón
        let height = 120;

        let ficha1=new Image()
        ficha1.src="Images/Juego/fichawolverine1.png"
        let ficha2=new Image()
        ficha2.src="Images/Juego/fichawolverine2.png"
        let ficha3=new Image()
        ficha3.src="Images/Juego/fichawlv3.png"

        let btn1=new BotonFicha(this.ctx,x, y,width,height,4,10,ficha1,true);
        this.botonesFicha2.push(btn1);
        let btn2=new BotonFicha(this.ctx,x+150, y,width,height,4,10,ficha2,false);
        this.botonesFicha2.push(btn2);
        let btn3=new BotonFicha(this.ctx,x+300, y,width,height,4,10,ficha3,false);
        this.botonesFicha2.push(btn3);
    }

}



