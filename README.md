# GameHub

GameHub es una aplicación web full stack para descubrir juegos gratuitos, guardar favoritos, publicar comentarios y obtener recomendaciones mediante inteligencia artificial. El catálogo externo se obtiene desde FreeToGame y se presenta en carruseles organizados por género. Además, el proyecto incluye **Connect Four** como juego local original.

## Funcionalidades

- Catálogo de juegos gratuitos sincronizado con FreeToGame.
- Carruseles dinámicos organizados por género.
- Filtros de catálogo por categoría y plataforma.
- Registro e inicio de sesión con JWT.
- Contraseñas almacenadas mediante BCrypt.
- Perfil del usuario autenticado.
- Favoritos persistentes por usuario.
- Comentarios persistentes asociados al usuario autenticado y al juego.
- Buscador AI Game Finder integrado con Gemini.
- Connect Four como juego local de GameHub.
- Frontend responsive realizado con HTML, CSS y JavaScript sin frameworks.
- H2 para desarrollo local y soporte para PostgreSQL mediante un perfil de Spring.

## Tecnologías

### Backend

- Java 21
- Spring Boot 3.5.4
- Spring Web
- Spring Data JPA
- Jakarta Validation
- JJWT 0.12.6
- BCrypt
- H2
- PostgreSQL
- Maven

### Frontend

- HTML5
- CSS3
- JavaScript
- Fetch API

### Servicios externos

- [FreeToGame API](https://www.freetogame.com/api-doc)
- Google Gemini API

## Estructura del proyecto

```text
GamesHub/
├── backend/
│   ├── src/main/java/com/gamehub/
│   │   ├── config/          # Configuración, CORS e inicialización opcional
│   │   ├── controller/      # Endpoints REST
│   │   ├── dto/             # Contratos seguros de entrada y salida
│   │   ├── entity/          # Entidades JPA
│   │   ├── exception/       # Manejo centralizado de errores
│   │   ├── integration/     # Clientes de FreeToGame y Gemini
│   │   ├── repository/      # Repositorios Spring Data
│   │   ├── security/        # JWT e identidad del usuario actual
│   │   └── service/         # Lógica de negocio
│   ├── src/main/resources/  # Configuración H2 y PostgreSQL
│   └── src/test/            # Pruebas automatizadas
├── css/                     # Estilos compartidos
├── fonts/                   # Fuentes locales
├── Iconos/                  # Recursos gráficos
├── Images/                  # Imágenes y recursos del juego local
├── js/                      # Integración frontend y Connect Four
├── index.html               # Catálogo principal
├── game-detail.html         # Detalle de un juego externo
├── game.html                # Connect Four
├── favorites.html           # Favoritos del usuario
├── login.html               # Inicio de sesión
└── register.html            # Registro
```

## Requisitos

- JDK 21 o superior.
- Maven 3.9 o superior.
- Un servidor HTTP estático para servir el frontend.
- PostgreSQL únicamente si se utiliza el perfil `postgres`.
- Acceso a Internet para sincronizar FreeToGame o utilizar Gemini.

## Configuración

El backend utiliza H2 en memoria de manera predeterminada, por lo que no requiere instalar una base de datos para desarrollo local. Los datos se pierden cuando se detiene la aplicación.

Variables de entorno disponibles:

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `JWT_SECRET` | Clave utilizada para firmar los JWT | Clave local de desarrollo |
| `JWT_EXPIRATION_MS` | Duración del JWT en milisegundos | `86400000` (24 horas) |
| `GEMINI_API_KEY` | Clave de acceso a Gemini | Vacía |
| `GEMINI_MODEL` | Modelo utilizado por AI Game Finder | `gemini-3.6-flash` |
| `CORS_ALLOWED_ORIGINS` | Orígenes autorizados para consumir la API | Puertos locales 8080 y 5500 |
| `DATABASE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gamehub` |
| `DATABASE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `postgres` |

Para ambientes reales se debe definir un `JWT_SECRET` largo y privado. Las claves y contraseñas no deben incorporarse al repositorio.

## Ejecución local

### 1. Iniciar el backend

Desde la raíz del proyecto:

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

La consola de H2 está disponible en `http://localhost:8080/h2-console` con estos datos:

```text
JDBC URL: jdbc:h2:mem:gamehub
Usuario:  sa
Password: (vacío)
```

### 2. Iniciar el frontend

En otra terminal, desde la raíz del proyecto, se puede utilizar cualquier servidor estático. Por ejemplo:

```bash
python -m http.server 5500
```

Luego abrir `http://localhost:5500/index.html`.

No se recomienda abrir los HTML directamente mediante `file://`, porque el navegador puede bloquear las solicitudes a la API por sus políticas de origen.

### 3. Cargar el catálogo

La sincronización automática está desactivada de forma predeterminada. Para importar o actualizar el catálogo manualmente:

```bash
curl -X POST http://localhost:8080/api/games/sync
```

La sincronización realiza un *upsert* utilizando el identificador externo de FreeToGame, por lo que repetirla no debería crear duplicados. Si FreeToGame no está disponible, el backend continúa funcionando y conserva el catálogo existente.

## PostgreSQL

Para ejecutar el backend con PostgreSQL:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Antes de iniciarlo se deben configurar `DATABASE_URL`, `DATABASE_USERNAME` y `DATABASE_PASSWORD` según el entorno.

## API REST

### Usuarios y autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/users/register` | Público | Registra un usuario y devuelve JWT + usuario seguro |
| `POST` | `/api/users/login` | Público | Inicia sesión con username o email y contraseña |
| `GET` | `/api/users/me` | JWT | Devuelve el usuario autenticado |

Ejemplo de registro:

```json
{
  "username": "player1",
  "email": "player1@example.com",
  "password": "secret123"
}
```

El login utiliza el campo `email` para recibir tanto el email como el nombre de usuario:

```json
{
  "email": "player1",
  "password": "secret123"
}
```

Los endpoints protegidos esperan el encabezado:

```http
Authorization: Bearer <token>
```

### Juegos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/games` | Público | Lista el catálogo |
| `GET` | `/api/games?category=Shooter&platform=PC` | Público | Filtra por género y plataforma |
| `GET` | `/api/games/{id}` | Público | Devuelve el detalle de un juego |
| `POST` | `/api/games/sync` | Público en el MVP | Sincroniza manualmente FreeToGame |

### Comentarios

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/games/{gameId}/comments` | Público | Lista comentarios del juego |
| `POST` | `/api/games/{gameId}/comments` | JWT | Publica un comentario como el usuario autenticado |

Ejemplo:

```json
{
  "content": "Muy buen juego para jugar con amigos."
}
```

La identidad se obtiene exclusivamente del JWT; la API no acepta un `userId` para crear comentarios.

### Favoritos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/games/{gameId}/favorite` | JWT | Agrega un favorito de forma idempotente |
| `DELETE` | `/api/games/{gameId}/favorite` | JWT | Elimina un favorito de forma idempotente |
| `GET` | `/api/users/me/favorites` | JWT | Lista los juegos favoritos del usuario actual |

### AI Game Finder

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/ai/find` | Público | Recomienda juegos del catálogo según una consulta |

Ejemplo:

```json
{
  "query": "Quiero un juego de estrategia para partidas cortas"
}
```

AI Game Finder requiere `GEMINI_API_KEY`. Si Gemini no está configurado o no está disponible, la API devuelve un error controlado sin afectar el resto de GameHub.

## Pruebas

Las pruebas automatizadas no dependen de las APIs reales de FreeToGame o Gemini; los límites externos se simulan para que la suite sea reproducible.

```bash
cd backend
mvn clean test
```

Para compilar y empaquetar la aplicación:

```bash
mvn clean package
```

El JAR resultante se genera dentro de `backend/target/`.

## Seguridad

- Las contraseñas se almacenan como hashes BCrypt y nunca se incluyen en DTOs o tokens.
- Los JWT contienen la identidad necesaria, están firmados y tienen vencimiento.
- La autenticación es stateless.
- Los comentarios y favoritos siempre utilizan el usuario obtenido del JWT.
- Los errores de autenticación y de servicios externos se devuelven sin exponer trazas internas.
- El proyecto no implementa roles, refresh tokens ni logout del lado del servidor en esta versión.

## Estado y alcance

Esta versión corresponde al MVP funcional de GameHub. No incluye compras, carrito, roles administrativos, Swagger/OpenAPI, Docker ni despliegue automatizado.

El catálogo externo, las recomendaciones de Gemini y las imágenes remotas dependen de servicios de terceros. La aplicación puede iniciar y utilizar sus funciones locales con H2 aunque esos servicios no estén disponibles.
