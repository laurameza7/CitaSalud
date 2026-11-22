# 4. Requisitos

Cada requisito funcional sale de una historia de usuario (columna HU) y está implementado en el backend (columna Endpoint). Las reglas de negocio (RN) están en [historias/00-resumen.md](03-Historias-de-usuario).

## Requisitos funcionales

| ID | Requisito | HU | Prioridad | Endpoint |
|---|---|---|---|---|
| RF_001 | El sistema debe permitir que un visitante se registre como paciente con nombre, documento, correo, teléfono, fecha de nacimiento, tipo de paciente y contraseña, validando que el correo y el documento no estén registrados. | HU_001 | Alta | `POST /api/auth/registro` |
| RF_002 | El sistema debe autenticar a los usuarios con correo y contraseña y entregar un token de sesión con su rol. | HU_002 | Alta | `POST /api/auth/login` |
| RF_003 | El sistema debe permitir al administrador crear, editar, listar y desactivar especialidades con nombre único, descripción y tarifa. | HU_003 | Alta | `/api/especialidades` |
| RF_004 | El sistema debe permitir al administrador registrar, editar, listar y desactivar médicos con especialidad, registro médico, consultorio y cuenta de acceso. | HU_004 | Alta | `/api/medicos` |
| RF_005 | El sistema debe permitir al administrador crear, listar y desactivar cuentas de recepcionista. | HU_005 | Media | `/api/recepcionistas` |
| RF_006 | El sistema debe permitir al médico (o al administrador) definir su horario de atención por día de la semana, validando que la hora de fin sea posterior a la de inicio. | HU_006 | Alta | `PUT /api/medicos/{id}/horarios` |
| RF_007 | El sistema debe mostrar sin iniciar sesión las especialidades activas con su tarifa y los médicos activos de cada especialidad. | HU_007 | Alta | `GET /api/especialidades`, `GET /api/medicos?especialidadId=` |
| RF_008 | El sistema debe calcular las franjas libres de 30 minutos de un médico en una fecha, descontando las citas programadas y las horas ya pasadas. | HU_008 | Alta | `GET /api/medicos/{id}/disponibilidad?fecha=` |
| RF_009 | El sistema debe permitir al paciente agendar una cita en una franja libre, validando las reglas RN_04, RN_05 y RN_06. | HU_009 | Alta | `POST /api/citas` |
| RF_010 | El sistema debe calcular el valor de la cita según la tarifa de la especialidad, el tipo de paciente y el cargo administrativo (RN_01 a RN_03). | HU_009 | Alta | `POST /api/citas`, `GET /api/citas/cotizar` |
| RF_011 | El sistema debe cobrar la cita a través de la pasarela de pagos y solo programarla si el pago es aprobado (RN_09). | HU_009 | Alta | `POST /api/citas` |
| RF_012 | El sistema debe mostrar al paciente sus citas con médico, especialidad, fecha, estado y valor pagado. | HU_010 | Media | `GET /api/citas/mias` |
| RF_013 | El sistema debe permitir cancelar una cita programada con más de 24 horas de anticipación y solicitar el reembolso (RN_08, RN_10). | HU_011 | Media | `PUT /api/citas/{id}/cancelar` |
| RF_014 | El sistema debe permitir reprogramar una cita a otra franja libre del mismo médico, máximo 2 veces y con más de 24 horas de anticipación (RN_08, RN_11). | HU_012 | Media | `PUT /api/citas/{id}/reprogramar` |
| RF_015 | El sistema debe permitir a la recepcionista buscar un paciente por documento y agendarle una cita con las mismas reglas de RF_009 a RF_011. | HU_013 | Media | `GET /api/pacientes?documento=`, `POST /api/citas` |
| RF_016 | El sistema debe mostrar al médico sus citas de una fecha ordenadas por hora. | HU_014 | Alta | `GET /api/citas/agenda?fecha=` |
| RF_017 | El sistema debe permitir al médico de la cita registrar las notas de atención o marcar la inasistencia, solo cuando la hora de la cita ya llegó (RN_12). | HU_015 | Alta | `PUT /api/citas/{id}/atencion`, `PUT /api/citas/{id}/inasistencia` |
| RF_018 | El sistema debe generar un reporte por especialidad con la cantidad de citas por estado y el total recaudado en un rango de fechas. | HU_016 | Media | `GET /api/reportes/citas?desde=&hasta=` |

## Requisitos no funcionales

| ID | Tipo | Requisito | Cómo se cumple |
|---|---|---|---|
| RNF_001 | Tecnología | El backend se construye con Java 17 y Spring Boot 3. | `backend/pom.xml` |
| RNF_002 | Persistencia | Los datos se guardan en MongoDB (base de datos no relacional). | Spring Data MongoDB, colecciones en `database/README.md` |
| RNF_003 | Arquitectura | Arquitectura por capas Controller → Service → Repository → Model, con DTO entre la API y el dominio. Los controllers no usan repositorios. | Paquetes `controller`, `service`, `repository`, `model`, `dto` |
| RNF_004 | Seguridad | Contraseñas cifradas con BCrypt, autenticación con JWT, autorización por rol y ninguna credencial dentro del repositorio. | `security/`, `SecurityConfig`, variables de entorno |
| RNF_005 | Mantenibilidad | Los valores de negocio (descuentos, cargo administrativo, duración de la cita, horas mínimas para cancelar, máximo de reprogramaciones) están en constantes fáciles de encontrar y cambiar. | `model/tarifa/`, `Cita`, `Medico` |
| RNF_006 | Usabilidad | Los errores se devuelven con un código HTTP adecuado y un mensaje en español. | `ManejadorErrores` |
| RNF_007 | Rendimiento | La consulta de disponibilidad responde en menos de 2 segundos con los datos de prueba. | Consulta por médico y rango de fecha |
| RNF_008 | Calidad | Toda funcionalidad tiene pruebas unitarias JUnit y peticiones de Postman. | `backend/src/test`, `postman/` |
| RNF_009 | Portabilidad | El backend corre en cualquier máquina con Docker y se puede desplegar en la nube. | `Dockerfile`, `docker-compose.yml` |
| RNF_010 | Documentación | La API se documenta con Swagger y el proyecto con README y wiki. | `/swagger-ui.html`, `README.md`, `wiki/` |
| RNF_011 | Plataforma | La aplicación se usa desde el navegador del computador o del celular (diseño adaptable). | `frontend/` |
| RNF_012 | Integración | La pasarela de pagos se usa detrás de una interfaz propia para poder cambiarla sin tocar la lógica de citas. | `integracion/PasarelaPagos` |

## Matriz de trazabilidad HU → RF → componentes

| HU | RF | Controller | Service | Modelo |
|---|---|---|---|---|
| HU_001 | RF_001 | `AuthController` | `AuthServicio` | `Paciente` |
| HU_002 | RF_002 | `AuthController` | `AuthServicio`, `JwtServicio` | `Usuario` |
| HU_003 | RF_003 | `EspecialidadController` | `EspecialidadServicio` | `Especialidad` |
| HU_004 | RF_004 | `MedicoController` | `MedicoServicio` | `Medico` |
| HU_005 | RF_005 | `RecepcionistaController` | `RecepcionistaServicio` | `Recepcionista` |
| HU_006 | RF_006 | `MedicoController` | `MedicoServicio` | `Medico`, `HorarioAtencion` |
| HU_007 | RF_007 | `EspecialidadController`, `MedicoController` | `EspecialidadServicio`, `MedicoServicio` | `Especialidad`, `Medico` |
| HU_008 | RF_008 | `MedicoController` | `DisponibilidadServicio` | `Medico`, `HorarioAtencion`, `Cita` |
| HU_009 | RF_009, RF_010, RF_011 | `CitaController` | `CitaServicio`, `DisponibilidadServicio` | `Cita`, `EstrategiaTarifa`, `PasarelaPagos` |
| HU_010 | RF_012 | `CitaController` | `CitaServicio` | `Cita` |
| HU_011 | RF_013 | `CitaController` | `CitaServicio` | `Cita`, `PasarelaPagos` |
| HU_012 | RF_014 | `CitaController` | `CitaServicio` | `Cita` |
| HU_013 | RF_015 | `PacienteController`, `CitaController` | `PacienteServicio`, `CitaServicio` | `Paciente`, `Cita` |
| HU_014 | RF_016 | `CitaController` | `CitaServicio` | `Cita` |
| HU_015 | RF_017 | `CitaController` | `CitaServicio` | `Cita` |
| HU_016 | RF_018 | `ReporteController` | `ReporteServicio` | `ReporteCitas`, `LineaReporte` |
