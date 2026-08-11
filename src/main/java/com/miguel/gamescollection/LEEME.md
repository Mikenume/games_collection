# Backend games_collection — instrucciones de instalación

## 1. Dónde va cada cosa

Descomprime el ZIP dentro de:

    C:\dev\games_collection\src\main\java\com\miguel\gamescollection\

Deben quedarte estas carpetas al mismo nivel que `GamesCollectionApplication.java`:

    gamescollection/
    ├── GamesCollectionApplication.java   (ya la tienes, no se toca)
    ├── config/
    ├── controller/
    ├── dto/
    ├── exception/
    ├── model/
    ├── repository/
    └── service/

`model/Platform.java`, `model/Genre.java`, `repository/PlatformRepository.java`
y los controllers de Platform y Genre SUSTITUYEN a los que ya tenías.
Acepta sobrescribir.

## 2. application.properties

Añade estas líneas a las que ya tienes:

    # Las relaciones perezosas solo se pueden cargar dentro de la transacción.
    # Con esto en false, si se te escapa una entity al controller falla pronto
    # y de forma clara, en vez de disparar consultas silenciosas.
    spring.jpa.open-in-view=false

    # Formato de fechas ISO-8601 en el JSON (para createdAt)
    spring.jackson.serialization.write-dates-as-timestamps=false

## 3. Arrancar

Shift + F11. Si `validate` se queja de alguna columna, pégame el error:
el mensaje dice tabla, columna y tipo esperado.

## 4. Endpoints

### Plataformas
    GET    /api/platforms
    GET    /api/platforms/{id}
    POST   /api/platforms
    PUT    /api/platforms/{id}
    DELETE /api/platforms/{id}

### Géneros
    GET    /api/genres
    GET    /api/genres/{id}
    POST   /api/genres
    PUT    /api/genres/{id}
    DELETE /api/genres/{id}

### Juegos
    GET    /api/games
    GET    /api/games?title=zelda
    GET    /api/games/{id}
    POST   /api/games
    PUT    /api/games/{id}
    DELETE /api/games/{id}

### Ediciones
    GET    /api/editions
    GET    /api/editions?platformId=3
    GET    /api/editions?gameId=42
    GET    /api/editions?owned=true
    GET    /api/editions/{id}
    POST   /api/editions
    PUT    /api/editions/{id}
    DELETE /api/editions/{id}

## 5. Cuerpos de ejemplo

POST /api/games

    {
      "title": "Shadow of the Colossus",
      "releaseYear": 2005,
      "developer": "Team Ico",
      "publisher": "Sony Computer Entertainment",
      "synopsis": "Un joven recorre tierras prohibidas para abatir dieciséis colosos.",
      "editionType": "original",
      "genreIds": [1, 3]
    }

POST /api/editions

    {
      "gameId": 1,
      "platformId": 2,
      "releaseYear": 2006,
      "region": "PAL",
      "format": "DVD",
      "owned": true,
      "portDeveloper": null,
      "notes": "Edición española"
    }

POST /api/platforms

    {
      "name": "PlayStation 2",
      "abbreviation": "PS2",
      "manufacturer": "Sony",
      "releaseYear": 2000
    }

## 6. Códigos de respuesta

    200  OK
    201  Creado (POST)
    204  Borrado sin contenido (DELETE)
    400  Validación fallida (incluye el detalle por campo)
    404  No existe el recurso
    409  Choque con la BBDD: duplicado, FK en uso o CHECK incumplido
