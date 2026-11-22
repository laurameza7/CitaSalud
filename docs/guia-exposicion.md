# Guía para la exposición y el cambio en vivo

Esta guía es para que entiendas el código y puedas hacer cambios sin ayuda. Léela con el proyecto abierto en tu editor.

## 1. El recorrido de una petición (lo más importante de entender)

Toma como ejemplo **agendar una cita** (HU_009). Sigue los archivos en este orden:

1. **`controller/CitaController.java`**, método `agendar`. Recibe el JSON, lo convierte en `AgendarCitaRequest` (un DTO) y llama a `citaServicio.agendar(...)`. `@PreAuthorize` dice qué roles pueden entrar. No tiene reglas de negocio: solo delega (GRASP **Controlador**).
2. **`service/CitaServicio.java`**, método `agendar`. Es el que coordina:
   - busca al paciente y al médico;
   - le pide al **médico** que cree la cita: `medico.crearCita(...)` (GRASP **Creador**);
   - revisa que la franja esté libre y que el paciente no tenga cruces;
   - cobra con la **pasarela** (`pasarela.cobrar`) y, si se aprueba, guarda con el **repositorio**.
3. **`model/Medico.java`**, método `crearCita`. Valida que sea futura y que el médico atienda a esa hora (`atiendeEn`), calcula el valor con `especialidad.valorPara(tipoPaciente)` y crea la `Cita`.
4. **`model/Especialidad.java`**, método `valorPara`. Pide a `FabricaTarifas` la estrategia del tipo de paciente y calcula (patrón **Strategy**).
5. **`model/tarifa/EstrategiaTarifa.java`**. Aquí está la fórmula: `tarifa − descuento + CARGO_ADMINISTRATIVO`.
6. **`repository/CitaRepositorio.java`**. Es una interfaz: Spring crea las consultas a MongoDB a partir del nombre del método (`existsByMedicoAndFechaHoraAndEstado`, etc.).
7. Si algo falla, el servicio lanza una excepción (`ConflictoException`, `PagoRechazadoException`...) y **`exception/ManejadorErrores.java`** la convierte en una respuesta HTTP con mensaje en español.

Regla para recordar: **Controller recibe, Service decide, Model sabe, Repository guarda.**

## 2. Cambios en vivo típicos (practícalos varias veces)

Después de cada cambio: corre `mvn test` en `backend/`. Si una prueba falla porque cambiaste un valor, ajusta el número esperado en la prueba (eso también demuestra que sabes probar).

| Te piden… | Archivo | Qué cambias | Pruebas que ajustas |
|---|---|---|---|
| Cambiar el cargo administrativo a $8.000 | `model/tarifa/EstrategiaTarifa.java` | `long CARGO_ADMINISTRATIVO = 8000;` | `TarifaTest`, `MedicoTest`, `ReporteCitasTest`, `CitaServicioTest` y los valores de Postman |
| Cambiar el descuento de Convenio a 20 % | `model/tarifa/TarifaConvenio.java` | `DESCUENTO = 0.20;` | `TarifaTest.convenio`, `CitaServicioTest` |
| Cancelar con 48 horas en vez de 24 | `model/Cita.java` | `HORAS_MINIMAS_PARA_CAMBIOS = 48;` | `CitaTest` si cambia algún caso |
| Permitir 3 reprogramaciones | `model/Cita.java` | `MAXIMO_REPROGRAMACIONES = 3;` | `CitaTest.reprogramarMaximoDos` |
| Citas de 20 minutos | `model/Cita.java` | `DURACION_MINUTOS = 20;` | `MedicoTest.franjasDelDia` (de 08:00 a 10:00 serían 6 franjas) |
| Agregar el tipo de paciente **Adulto mayor** con 25 % | 1) `model/TipoPaciente.java`: agregar `ADULTO_MAYOR`. 2) Crear `model/tarifa/TarifaAdultoMayor.java` copiando `TarifaConvenio` con `DESCUENTO = 0.25`. 3) `model/tarifa/FabricaTarifas.java`: agregar `case ADULTO_MAYOR -> new TarifaAdultoMayor();` | Agregar una prueba en `TarifaTest`. En el diagrama de clases: nuevo valor del enum y nueva clase que realiza `EstrategiaTarifa` |
| Agregar un estado nuevo de la cita | `model/EstadoCita.java` y el `switch` de `LineaReporte.registrar` (Java te avisa si falta un caso) | — |

Ojo: después de cambiar un valor, actualiza también el diagrama de clases (los valores aparecen en `EstrategiaTarifa`, las tarifas y `Cita`). Eso es lo que el profesor llama coherencia.

### Agregar una operación nueva (ejemplo: consultar una especialidad por nombre)

1. Repositorio: `Optional<Especialidad> findByNombreIgnoreCase(String nombre);` en `EspecialidadRepositorio`.
2. Servicio: un método en `EspecialidadServicio` que lo use y lance `RecursoNoEncontradoException` si no existe.
3. Controller: `@GetMapping("/buscar")` con `@RequestParam String nombre` en `EspecialidadController`.
4. Si debe ser público, agregar la ruta en `SecurityConfig` (lista de `permitAll`).
5. Probar en Swagger o Postman.

## 3. Cómo mostrar la demo (2 a 3 minutos)

1. Abre Swagger un rato antes (si está en Render gratis, la primera petición tarda cerca de un minuto en despertar).
2. App: entra como **Ana** (Convenio) → Especialidades → Medicina general → Carlos Ruiz → elige un lunes y una hora → tarjeta `4111111111111111` → muestra el valor y agenda.
3. Muestra el caso de error: misma franja con otra cuenta (409) o tarjeta terminada en `0000` (402).
4. Cancela la cita desde *Mis citas* (reembolso).
5. Entra como **Carlos Ruiz** y muestra la agenda.
6. Swagger: `POST /api/auth/login` con el administrador → copiar el token → *Authorize* → `GET /api/reportes/citas`.

## 4. Guion sugerido (10 a 15 minutos)

| Tiempo | Tema |
|---|---|
| 1 min | Problema: filas y llamadas para pedir citas; qué resuelve CitaSalud y sus actores |
| 1 min | Historias de usuario y épicas; ejemplo de una HU con Gherkin |
| 6 a 8 min | Las 5 vistas: contexto, funcional, lógica (resaltar patrones), desarrollo (capas) y despliegue. En cada una, señalar una HU o un RF y la clase del código que lo implementa |
| 2 a 3 min | Demo |
| Resto | Preguntas y cambio en vivo |

## 5. Preguntas probables y respuestas cortas

- **¿Por qué MongoDB?** Requisito del curso; además los documentos encajan bien: el horario del médico va embebido en su documento y las citas referencian a paciente, médico y especialidad.
- **¿Por qué el Visitante no es una clase?** Porque no tiene cuenta ni datos guardados; solo consulta lo público.
- **¿Por qué `Usuario` es abstracta?** Nunca existe un usuario "genérico": siempre es paciente, médico, recepcionista o administrador. Comparten correo, contraseña y verificación de contraseña.
- **¿Dónde está el patrón Strategy?** `EstrategiaTarifa` con tres implementaciones; `FabricaTarifas` elige cuál según el tipo de paciente. Agregar un tipo no obliga a tocar `Especialidad` ni `CitaServicio`.
- **¿Por qué el médico crea la cita (Creador)?** Porque tiene la información necesaria: su horario y su especialidad (de la que sale la tarifa).
- **¿Cómo evitas que un paciente cancele la cita de otro?** El token JWT trae el id del usuario; `CitaServicio.verificarPuedeModificar` compara ese id con el paciente de la cita y responde 403.
- **¿Qué pasa si la pasarela rechaza el pago?** Se lanza `PagoRechazadoException` antes de guardar, así que la cita no existe y la franja sigue libre (respuesta 402).
- **¿Por qué la pasarela es una interfaz?** Variaciones protegidas: hoy es simulada; para usar una real (Wompi, PayU) se crea otra clase que implemente `PasarelaPagos` y no se toca la lógica de citas.
- **¿Cómo pruebas la regla de 24 horas si depende de la hora actual?** El reloj (`Clock`) se inyecta; en las pruebas se usa un reloj fijo (`Clock.fixed`).
- **¿Qué es un DTO y por qué?** Objetos solo para entrar y salir por la API: así no se expone la contraseña cifrada ni la estructura interna del modelo.
