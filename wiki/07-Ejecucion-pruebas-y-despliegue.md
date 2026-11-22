# 7. Ejecución, pruebas y despliegue

### Cómo ejecutar

**Opción 1, con Docker** (no necesita Java ni MongoDB instalados):

```bash
docker compose up --build        # MongoDB + backend en http://localhost:8080
```

**Opción 2, sin Docker** (Java 17, Maven y MongoDB en `localhost:27017`):

```bash
cd backend
mvn spring-boot:run              # API en http://localhost:8080
mvn test                         # pruebas JUnit
```

**Frontend:**

```bash
cd frontend
cp .env.example .env
npm install
npm run dev                      # app en http://localhost:5173
```

- Swagger: http://localhost:8080/swagger-ui.html
- Estado: http://localhost:8080/api/estado

#### Variables de entorno

| Variable | Para qué | Valor local por defecto |
|---|---|---|
| `MONGODB_URI` | Conexión a MongoDB | `mongodb://localhost:27017/citasalud` |
| `JWT_SECRET` | Clave para firmar tokens (mínimo 32 caracteres) | Una clave solo para desarrollo |
| `CORS_ORIGENES` | URL del frontend | `http://localhost:5173` |
| `DATOS_PRUEBA` | Cargar datos de prueba si la base está vacía | `true` |

Las credenciales reales **nunca** se suben al repositorio (ver `backend/.env.example`).

### Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@citasalud.com | Admin123* |
| Recepcionista | recepcion@citasalud.com | Recepcion123* |
| Médico (Medicina general) | carlos.ruiz@citasalud.com | Medico123* |
| Médico (Pediatría) | andres.ortega@citasalud.com | Medico123* |
| Médico (Dermatología) | sofia.delgado@citasalud.com | Medico123* |
| Paciente (Convenio) | ana.perez@correo.com | Paciente123* |
| Paciente (Particular) | luis.martinez@correo.com | Paciente123* |
| Paciente (Prepagada) | camila.rosero@correo.com | Paciente123* |

Pasarela simulada: se aprueba cualquier tarjeta de 16 dígitos, salvo las que terminan en `0000`.

### Endpoints REST

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/auth/registro` | Público | HU_001 |
| POST | `/api/auth/login` | Público | HU_002 |
| GET | `/api/auth/yo` | Con sesión | HU_002 |
| GET | `/api/especialidades` | Público | HU_007 |
| GET | `/api/especialidades/{id}/valor?tipoPaciente=` | Público | HU_009 (RF_010) |
| POST / PUT / DELETE | `/api/especialidades[/{id}]` | Administrador | HU_003 |
| GET | `/api/medicos?especialidadId=` | Público | HU_007 |
| GET | `/api/medicos/{id}/disponibilidad?fecha=` | Público | HU_008 |
| POST / PUT / DELETE | `/api/medicos[/{id}]` | Administrador | HU_004 |
| PUT | `/api/medicos/{id}/horarios` | Médico (el suyo) o Administrador | HU_006 |
| GET / POST / DELETE | `/api/recepcionistas[/{id}]` | Administrador | HU_005 |
| GET | `/api/pacientes?documento=` | Recepcionista, Administrador | HU_013 |
| POST | `/api/citas` | Paciente, Recepcionista | HU_009, HU_013 |
| GET | `/api/citas/mias` | Paciente | HU_010 |
| GET | `/api/citas/{id}` | Dueño de la cita, Recepcionista, Administrador | HU_010 |
| PUT | `/api/citas/{id}/cancelar` | Paciente (la suya), Recepcionista | HU_011 |
| PUT | `/api/citas/{id}/reprogramar` | Paciente (la suya), Recepcionista | HU_012 |
| GET | `/api/citas/agenda?fecha=` | Médico | HU_014 |
| PUT | `/api/citas/{id}/atencion` · `/inasistencia` | Médico de la cita | HU_015 |
| GET | `/api/reportes/citas?desde=&hasta=` | Administrador | HU_016 |

Errores con mensaje en español: 400 (datos inválidos), 401 (sin sesión), 402 (pago rechazado), 403 (sin permiso), 404 (no existe), 409 (conflicto: correo repetido, franja ocupada), 422 (regla de negocio).

### Pruebas

```bash
cd backend && mvn test           # 23 pruebas JUnit
newman run postman/CitaSalud.postman_collection.json -e postman/CitaSalud-Local.postman_environment.json
```

La colección está organizada por historia de usuario. Se ejecuta en orden con el *Runner* de Postman. GitHub Actions corre ambas en cada push (`.github/workflows/ci.yml`).

### Despliegue en la nube

1. **MongoDB Atlas:** crear un cluster M0 gratis y copiar la cadena de conexión.
2. **Render:** *New → Blueprint* con este repositorio (usa `render.yaml`). Poner `MONGODB_URI` y `CORS_ORIGENES` (URL de Vercel) en el panel.
3. **Vercel:** importar el repositorio, carpeta raíz `frontend`, variable `VITE_API_URL` con la URL de Render.
4. Cambiar la URL del entorno `postman/CitaSalud-Nube.postman_environment.json`.

