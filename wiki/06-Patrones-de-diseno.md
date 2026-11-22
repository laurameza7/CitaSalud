# 6. Patrones de diseño


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

