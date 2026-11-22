# Diagramas UML de CitaSalud

Los PNG de esta carpeta son **borradores de referencia** generados desde las fuentes de `fuentes/*.puml` (PlantUML). La versión final de cada vista se dibuja en **Visual Paradigm** siguiendo estos borradores y se exporta con el mismo nombre de archivo para reemplazarlos.

| Vista | Archivo | Tipo de diagrama | Qué cubre |
|---|---|---|---|
| 1. Contexto (nivel 0) | `vista-contexto.png` | Diagrama de contexto: el sistema como un círculo y 6 actores como rectángulos | Todas las HU |
| 2. Funcional | `vista-funcional.png` | Diagrama de componentes con puertos, interfaces provistas y requeridas y «delegate» | Componentes ↔ HU (tabla abajo) |
| 3. Lógica | `vista-logica.png` | Diagrama de clases del dominio con herencia, interfaces, enumeraciones y multiplicidades | Paquete `model` |
| 4. Desarrollo | `vista-desarrollo.png` | Clases por capas: `Presentacion_Controller`, `Negocio_Service`, `Datos_Repository` y `Modelo_de_Dominio` | Paquetes `controller`, `service`, `repository`, `model` |
| 5. Despliegue | `vista-despliegue.png` | Nodos (cubos) en tres capas: navegador, Vercel, Render (Docker) y MongoDB Atlas | RNF_009 |
| Secuencia | `secuencia-agendar-cita.png` | HU_009 con fragmentos alt: fuera de horario (422), franja ocupada (409), pago rechazado (402), éxito (201) | RF_009 a RF_011 |
| Secuencia | `secuencia-cancelar-cita.png` | HU_011 con fragmentos alt: no existe (404), otra persona (403), menos de 24 h (422), éxito (200) | RF_013 |

## Trazabilidad de la vista funcional

| Componente | Clases del código | HU | RF |
|---|---|---|---|
| Autenticación | `AuthController`, `AuthServicio` | HU_001, HU_002 | RF_001, RF_002 |
| Seguridad | `FiltroJwt`, `JwtServicio`, `SecurityConfig` | Todas las que requieren sesión | RNF_004 |
| Especialidades y tarifas | `EspecialidadController`, `EspecialidadServicio`, `model/tarifa` | HU_003, HU_007 | RF_003, RF_007, RF_010 |
| Médicos y horarios | `MedicoController`, `MedicoServicio` | HU_004, HU_006 | RF_004, RF_006 |
| Disponibilidad | `DisponibilidadServicio` | HU_008 | RF_008 |
| Recepción | `RecepcionistaController`, `PacienteController` y sus servicios | HU_005, HU_013 | RF_005, RF_015 |
| Citas | `CitaController`, `CitaServicio` | HU_009 a HU_015 | RF_009 a RF_017 |
| Reportes | `ReporteController`, `ReporteServicio` | HU_016 | RF_018 |
| Integración de pagos | `PasarelaPagos`, `PasarelaPagosSimulada` | HU_009, HU_011 | RF_011, RF_013, RNF_012 |
| Persistencia | `UsuarioRepositorio`, `EspecialidadRepositorio`, `CitaRepositorio` | Todas | RNF_002 |

## Patrones de diseño

**GRASP**
- **Controlador:** cada `*Controller` recibe la petición HTTP y delega en su servicio, sin reglas de negocio.
- **Experto en información:** `Usuario.verificarContrasena`, `Especialidad.valorPara`, `Medico.atiendeEn`, `HorarioAtencion.franjas`, `Cita.puedeCancelarse` / `cancelar` / `reprogramar`.
- **Creador:** `Medico.crearCita()` crea la `Cita` (el médico tiene la agenda); `ReporteCitas` crea sus `LineaReporte`.
- **Polimorfismo:** `EstrategiaTarifa` con `TarifaParticular`, `TarifaConvenio`, `TarifaPrepagada`.
- **Fabricación pura:** `FabricaTarifas`, los repositorios y `DisponibilidadServicio`.
- **Indirección / Variaciones protegidas:** `PasarelaPagos` (cambiar de pasarela no toca `CitaServicio`) y `EstrategiaTarifa`.
- **Bajo acoplamiento y alta cohesión:** capas separadas con DTO e inyección por constructor.

**GoF y otros**
- **Strategy:** las tarifas. **Simple Factory:** `FabricaTarifas.para()`.
- **Repository:** interfaces de Spring Data. **DTO:** `dto/*Request` y `*Response`.
- **Chain of Responsibility:** `FiltroJwt` dentro de la cadena de filtros de Spring Security.
- **Inyección de dependencias:** servicios, repositorios, `PasarelaPagos` y el `Clock` de `RelojConfig`.
- **Manejador global de errores:** `ManejadorErrores` (`@RestControllerAdvice`).

## Cómo pasarlos a Visual Paradigm

Ver la guía paso a paso en [`docs/guia-visual-paradigm.md`](../docs/guia-visual-paradigm.md).

Para regenerar los borradores: `java -jar plantuml.jar -tpng -o .. fuentes/*.puml` (requiere Graphviz).
