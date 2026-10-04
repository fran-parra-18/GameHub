# GameHub

GameHub es una aplicación web full stack para descubrir juegos gratuitos, guardar favoritos, publicar comentarios y obtener recomendaciones mediante inteligencia artificial. El catálogo externo se obtiene desde FreeToGame y se presenta en carruseles organizados por género. Además, el proyecto incluye **Connect Four** como juego local original.

## Funcionalidades

- Catálogo de juegos gratuitos sincronizado con FreeToGame (automático al iniciar si el catálogo está vacío).
- Carruseles dinámicos: uno por cada género real del catálogo, más "Otros géneros" para los menos frecuentes; el menú lateral y su buscador se arman con esos géneros.
- Filtros de catálogo por categoría y plataforma.
- Registro e inicio de sesión con JWT.
- Contraseñas almacenadas mediante BCrypt.
- Perfil del usuario autenticado.
- Favoritos persistentes por usuario.
- Comentarios persistentes asociados al usuario autenticado y al juego.
- Buscador AI Game Finder integrado con Gemini.
- Connect Four como juego local de GameHub.
- Frontend responsive realizado con HTML, CSS y JavaScript sin frameworks.
- H2 en archivo para desarrollo local (los datos persisten) y soporte para PostgreSQL mediante un perfil de Spring.
- Un solo servidor: el backend sirve también el frontend en `http://localhost:8080`.
- Comentarios y favorito reales también en la página de Connect Four.

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
- PostgreSQL únicamente si se utiliza el perfil `postgres`.
- Acceso a Internet para sincronizar FreeToGame o utilizar Gemini.

## Configuración

El backend usa H2 en un archivo local (`backend/data/gamehub`), por lo que no requiere instalar una base de datos y los usuarios, favoritos, comentarios y el catálogo se conservan entre reinicios. Para empezar de cero basta con borrar la carpeta `backend/data`.

Variables de entorno disponibles:

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `JWT_SECRET` | Clave utilizada para firmar los JWT | Clave local de desarrollo |
| `JWT_EXPIRATION_MS` | Duración del JWT en milisegundos | `86400000` (24 horas) |
| `GEMINI_API_KEY` | Clave de acceso a Gemini | Vacía (se usa la búsqueda local) |
| `GEMINI_MODEL` | Modelo utilizado por AI Game Finder | `gemini-flash-latest` |
| `CATALOG_SYNC_ON_STARTUP` | Importa FreeToGame al iniciar si el catálogo está vacío | `true` |
| `H2_URL` | URL JDBC de H2 (por ejemplo `jdbc:h2:mem:gamehub` para una base en memoria) | `jdbc:h2:file:./data/gamehub` |
| `CORS_ALLOWED_ORIGINS` | Orígenes autorizados para consumir la API | Puertos locales 8080 y 5500 |
| `DATABASE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gamehub` |
| `DATABASE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `postgres` |

Para ambientes reales se debe definir un `JWT_SECRET` largo y privado. Las claves y contraseñas no deben incorporarse al repositorio.

## Ejecución local

Un solo comando levanta todo: el backend también sirve el frontend.

```bash
cd backend
mvn spring-boot:run
```

Luego abrir `http://localhost:8080`.

- Maven copia el frontend (HTML, CSS, JS e imágenes de la raíz del proyecto) dentro de la aplicación en cada arranque. Si modificás archivos del frontend, reiniciá el backend para verlos.
- Al iniciar, si el catálogo está vacío, se importa automáticamente desde FreeToGame (requiere Internet). Si falló o no hay conexión, la home muestra el botón **Importar catálogo**; también se puede hacer a mano:

```bash
curl -X POST http://localhost:8080/api/games/sync
```

  La sincronización realiza un *upsert* por el identificador externo de FreeToGame, por lo que repetirla no crea duplicados. Si FreeToGame no está disponible, el backend sigue funcionando y conserva el catálogo existente.
- La consola de H2 está en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/gamehub`, usuario `sa`, contraseña vacía; apagá el backend antes de abrir el archivo desde otra herramienta).
- Alternativa: servir el frontend con otro servidor estático (por ejemplo `python -m http.server 5500`) y abrir `http://localhost:5500`; la API se sigue consumiendo desde `http://localhost:8080`. No se recomienda abrir los HTML con `file://`.

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
| `GET` | `/api/games/original` | Público | Devuelve el juego local Connect Four (se crea la primera vez); admite comentarios y favoritos como cualquier otro |
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

Con `GEMINI_API_KEY` configurada, se envía a Gemini un subconjunto relevante del catálogo (hasta 60 juegos) y cada id devuelto se valida contra la base antes de responder. Si Gemini no está configurado o falla, se usa una búsqueda local por palabras clave (con equivalencias español → inglés, como "disparos" → Shooter); solo si tampoco hay coincidencias locales la API devuelve un error controlado (503 o 502) sin afectar el resto de GameHub.

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
