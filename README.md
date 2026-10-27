# SIGA IPT

Sistema Integrado de Gestión Académica del Instituto Profesional de Tecnología (IPT). Aplicación web monolítica por capas: backend en Spring Boot, autenticación con Keycloak y base de datos MySQL.

Este README explica cómo levantar el entorno de desarrollo.

## Estructura del repositorio

| Carpeta o archivo          | Contenido                                                      |
| -------------------------- | -------------------------------------------------------------- |
| `backend/`                 | API REST (Spring Boot, Java 25, Gradle)                        |
| `keycloak/siga-realm.json` | Realm `siga` de Keycloak: roles, clientes y usuarios de prueba |
| `docker-compose.yml`       | Levanta MySQL, Keycloak y el backend                           |
| `Evaluacion1_v1.pdf`       | Enunciado de la evaluación                                     |

## Cómo levantar el entorno

Todo corre en contenedores con Docker Compose: **MySQL**, **Keycloak** y el **backend**. Ya no se usa un MySQL instalado en el computador.

### Una sola vez: configurar las variables

Crea un archivo `.env` en la raíz del proyecto (está ignorado por git) con este contenido:

```properties
DB_NAME=tingeso1
DB_USER=<usuario de MySQL>
DB_PSW=<contraseña de ese usuario>
DB_ROOT_PSW=<contraseña de root de MySQL>
SIGA_BACKEND_CLIENT_SECRET=siga-backend-dev-secret
SEED_DATA=true
```

Qué significa cada variable:

| Variable | Qué poner |
|---|---|
| `DB_USER` | Usuario de MySQL que quieras (no puede ser `root`) |
| `DB_PSW` | Contraseña de ese usuario |
| `DB_ROOT_PSW` | Contraseña del usuario `root` de MySQL |
| `SIGA_BACKEND_CLIENT_SECRET` | `siga-backend-dev-secret` en desarrollo |
| `SEED_DATA` | `true` para cargar las carreras, planes y asignaturas del Anexo A |

### Cada vez que arranques el computador

1. Enciende el socket de Podman (permite que `docker compose` hable con Podman):

   ```bash
   systemctl --user start podman.socket
   ```

   Para dejarlo siempre encendido (una sola vez): `systemctl --user enable --now podman.socket`.

2. Levanta todo desde la raíz del proyecto:

   ```bash
   docker compose up -d --build
   ```

   La primera vez construye la imagen del backend y tarda unos minutos. Después, `docker compose up -d` basta.

3. Comprueba el estado:

   ```bash
   docker compose ps
   ```

   Deben aparecer `siga-db`, `siga-keycloak` y `siga-backend` en estado `Up`.

### Dónde queda cada cosa

| Servicio | URL desde tu computador | Para qué |
|---|---|---|
| Backend | `http://localhost:8090` | API y Swagger (`/swagger-ui.html`) |
| Keycloak | `http://localhost:8180` | Login y consola de administración |
| MySQL | `localhost:3307` | Para conectar un cliente SQL (por ejemplo DBeaver) |

Los datos persisten en volúmenes de Docker (`db-data` y `keycloak-data`), así que sobreviven a apagar y volver a encender.

### Apagar y borrar

| Comando | Efecto |
|---|---|
| `docker compose stop` | Apaga los contenedores y conserva todo |
| `docker compose down` | Borra los contenedores, pero **conserva los datos** (volúmenes) |
| `docker compose down -v` | Borra también los volúmenes: la base de datos y los usuarios de Keycloak vuelven a empezar de cero |

Si cambias `keycloak/siga-realm.json` (por ejemplo, un cliente nuevo), Keycloak solo lo importa cuando el realm no existe: hay que usar `docker compose down -v` para que tome efecto.

### Datos de ejemplo (Anexo A)

Con `SEED_DATA=true`, la primera vez que arranca el backend carga las 3 carreras del instituto, sus planes de estudio vigentes y las 72 asignaturas con sus prerrequisitos. Los datos están en `backend/src/main/resources/seed/`. Si una carrera ya existe, se omite, así que reiniciar no duplica nada.

### Reconstruir el backend tras cambiar código

```bash
docker compose up -d --build backend
```

### Ejecutar el backend desde IntelliJ en vez de en contenedor

Útil para depurar. Detén el contenedor del backend (`docker compose stop backend`) para liberar el puerto 8090 y ejecuta la app desde IntelliJ o con `./gradlew bootRun` en `backend/`. Se conecta a la base de datos del contenedor (puerto 3307) usando `backend/.env`, que debe tener:

```properties
DB_HOST=localhost
DB_PORT=3307
DB_USER=<el mismo DB_USER del .env de la raíz>
DB_PSW=<el mismo DB_PSW del .env de la raíz>
KEYCLOAK_ADMIN_CLIENT_SECRET=siga-backend-dev-secret
```

### Variables opcionales del backend

Tienen valor por defecto; se pasan como variables de entorno.

| Variable | Por defecto | Para qué |
|---|---|---|
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | `tingeso1` | Nombre de la base de datos |
| `DDL_AUTO` | `update` | Estrategia de Hibernate para las tablas |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/siga` | URL pública del realm (debe coincidir con el `iss` de los tokens) |
| `KEYCLOAK_JWK_SET_URI` | derivada de la anterior | Dónde el backend descarga las claves para validar tokens |
| `KEYCLOAK_ADMIN_URL` | `http://localhost:8180` | Dónde el backend se conecta a Keycloak para crear usuarios |
| `KEYCLOAK_ADMIN_ENABLED` | `true` | Si es `false`, no se crean usuarios en Keycloak |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Orígenes permitidos para el frontend |
| `SEED_DATA` | `false` | Carga los datos de ejemplo al arrancar |

## Usuarios de prueba

Definidos en `keycloak/siga-realm.json`. Solo para desarrollo.

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `Siga1234!` |
| Docente | `teacher` | `Siga1234!` |
| Estudiante | `student` | `Siga1234!` |

Los usuarios `teacher` y `student` solo ven sus propios datos cuando existe una ficha en la base de datos con el mismo correo (`teacher@siga.cl`, `student@siga.cl`).

## Probar la API con Swagger

1. Abre `http://localhost:8090/swagger-ui.html`.
2. Pulsa **Authorize**, arriba a la derecha.
3. En la sección **keycloakLogin**, pulsa su botón **Authorize**. Se abre la pantalla de Keycloak.
4. Escribe el usuario y la contraseña en la pantalla de Keycloak (por ejemplo `admin` y `Siga1234!`). Swagger nunca pide la contraseña.
5. Ya puedes usar **Try it out** en cualquier endpoint.

La sección **bearerAuth** es una alternativa manual: ahí se pega un token ya obtenido (nunca la contraseña).

Para obtener un token a mano:

```bash
curl -s -X POST http://localhost:8180/realms/siga/protocol/openid-connect/token \
  -d grant_type=password \
  -d client_id=siga-frontend \
  -d username=admin \
  -d 'password=Siga1234!' | jq -r .access_token
```

El token dura 5 minutos. Luego hay que pedir otro.

### Códigos de respuesta

| Código | Significado |
|---|---|
| 200 / 201 / 204 | La operación funcionó |
| 400 | Datos inválidos |
| 401 | No estás autorizado o el token expiró |
| 403 | Estás autorizado, pero tu rol no permite esa operación |
| 404 | No existe lo que buscas |
| 409 | La operación choca con una regla de negocio |
| 502 | Keycloak no está disponible |

### Orden para cargar datos

Con `SEED_DATA=true` los pasos 1 a 3 ya vienen cargados (3 carreras, sus planes y 72 asignaturas). Faltan los pasos 4 a 8.

| Paso | Endpoint | Para qué |
|---|---|---|
| 1 | `POST /api/careers` | Crear la carrera |
| 2 | `POST /api/studyplans` | Crear su plan, con `"current": true` |
| 3 | `POST /api/courses` | Crear las asignaturas del plan |
| 4 | `POST /api/academic-periods` | Abrir un periodo |
| 5 | `POST /api/teachers` | Crear un docente |
| 6 | `POST /api/sections` | Crear la sección de una asignatura |
| 7 | `POST /api/students` | Crear un estudiante |
| 8 | `POST /api/tuitions` | Matricular al estudiante en el periodo |

La inscripción (`POST /api/enrollments`) la hace el estudiante y las notas las registra el docente. Para probarlas hay que autorizarse con el usuario de esa persona.

Al crear un estudiante o un docente se envía una contraseña inicial (`password`, mínimo 8 caracteres). El backend crea su usuario en Keycloak con el rol correspondiente. Esa contraseña no se guarda en la base de datos y Keycloak pide cambiarla en el primer ingreso.

## Pruebas

```bash
cd backend
./gradlew test               # pruebas unitarias + reporte de cobertura (JaCoCo)
./gradlew integrationTest    # pruebas de repositorios e integración (requiere Podman/Docker)
```

- Las pruebas unitarias no necesitan MySQL ni Keycloak.
- Las pruebas de integración levantan su propio MySQL en un contenedor (Testcontainers). Necesitan el socket de Podman encendido (paso 2). No usan la base de datos de desarrollo.
- El reporte de cobertura queda en `backend/build/reports/jacoco/test/html/index.html`.

## Advertencias

- **`docker compose down -v` borra todo:** la base de datos y los usuarios de Keycloak. Sin `-v` los datos se conservan.
- **Reconstruir tras cambiar código:** el contenedor del backend usa la imagen que construiste. Si cambias código, ejecuta `docker compose up -d --build backend`.
- **Un solo backend en el puerto 8090:** no ejecutes a la vez el contenedor y la app de IntelliJ.
- **Credenciales de desarrollo:** el realm de Keycloak, la contraseña `admin/admin` de la consola y el secreto `siga-backend-dev-secret` son solo para desarrollo. En producción deben definirse por variables de entorno.

## Si algo falla al arrancar

| Síntoma | Causa probable |
|---|---|
| `docker compose up` dice que no puede conectar con la API de Docker | Falta encender el socket de Podman |
| `docker compose up` falla por variables sin definir | Falta completar el archivo `.env` de la raíz |
| El backend no arranca desde IntelliJ y menciona la base de datos | Falta `DB_PORT=3307` en `backend/.env`, o el contenedor `siga-db` está apagado |
| El puerto 8090 está ocupado | Hay otro backend corriendo (el contenedor o IntelliJ) |
| El login de Swagger da error | Keycloak apagado, o el backend corre con una versión antigua del código |
| Un estudiante o docente nuevo no puede entrar | Todavía no cambia la contraseña inicial en la pantalla de Keycloak, o se borraron los volúmenes con `down -v` |
