# Diagramas en Visual Paradigm

Los 7 diagramas ya están hechos en un solo proyecto de Visual Paradigm: **`diagramas/CitaSalud.vpp`** (Visual Paradigm Community 18.1). No hay que dibujarlos de nuevo.

| Diagrama en el .vpp | Imagen exportada |
|---|---|
| Vista de contexto (nivel 0) | `diagramas/visual-paradigm/vp-contexto.png` |
| Vista funcional (componentes) | `diagramas/visual-paradigm/vp-funcional.png` |
| Vista logica - CitaSalud | `diagramas/visual-paradigm/vp-logica.png` |
| Vista de desarrollo (clases por capas) | `diagramas/visual-paradigm/vp-desarrollo.png` |
| Vista de despliegue | `diagramas/visual-paradigm/vp-despliegue.png` |
| Secuencia - Agendar cita (HU_009) | `diagramas/visual-paradigm/vp-secuencia-agendar-cita.png` |
| Secuencia - Cancelar cita (HU_011) | `diagramas/visual-paradigm/vp-secuencia-cancelar-cita.png` |

## Abrir el modelo

1. Instala Visual Paradigm Community Edition (gratuita para uso no comercial), versión 18.1 o superior.
2. *Project → Open* y elige `diagramas/CitaSalud.vpp`.
3. Los diagramas aparecen en el *Diagram Navigator* y los elementos en el *Model Explorer*.

## Exportar las imágenes

- Un diagrama: ábrelo y usa *Project → Export → Active Diagram as Image…* (PNG).
- Todos a la vez: *Project → Export → Diagrams as Image…*, marca los 7 diagramas y exporta en PNG.
- Guarda los PNG en `diagramas/visual-paradigm/` con los nombres de la tabla (sin espacios ni tildes).

Cierra Visual Paradigm antes de hacer commit: mientras el proyecto está abierto crea archivos temporales (`.CitaSalud.vpp.lck`, `CitaSalud.vpp.bak_*`) que están en `.gitignore`.

## Qué contiene cada vista (referencia)

Lo que sigue es la lista de elementos que debe tener cada diagrama. Sirve para revisarlos o modificarlos. Los borradores en PlantUML (`diagramas/fuentes/*.puml` y los PNG `diagramas/vista-*.png`) tienen el mismo contenido.

Consejo general: activa *conectores rectilíneos* (clic derecho en el fondo → Connectors → Rectilinear) y usa *Layout → Auto Layout* al final para ordenar.

### 1. Vista de contexto (nivel 0)

1. *Diagram → New → Data Flow Diagram* (o *Block Definition*). Sirve cualquiera que permita óvalos y rectángulos.
2. Dibuja **un solo óvalo/círculo** en el centro: «CitaSalud».
3. Dibuja **6 rectángulos**: Visitante, Paciente, Médico, Recepcionista, Administrador, Pasarela de pagos (sistema externo).
4. Por cada actor, dos flechas: una **hacia** el círculo con la **acción** y otra **desde** el círculo con los **datos** que devuelve. Copia los textos del PNG (incluyen las HU).
5. MongoDB **no** va como actor: está dentro del sistema.

### 2. Vista funcional (componentes)

1. *Diagram → New → Component Diagram*.
2. Crea un componente grande con estereotipo «subsystem» llamado *CitaSalud API*.
3. Dentro, crea los 10 componentes de la tabla de trazabilidad de `diagramas/README.md` (Autenticación, Seguridad, Especialidades y tarifas, Médicos y horarios, Disponibilidad, Recepción, Citas, Reportes, Integración de pagos, Persistencia). En cada uno escribe las clases y las HU (puedes ponerlo como nota o en la documentación del elemento).
4. **Puertos**: en el borde del subsistema agrega 3 puertos (botón *Port*): `pRest` (izquierda), `pPago` y `pBD` (derecha).
5. **Interfaces**: usa *Provided Interface* (lollipop) en el componente que la ofrece y *Required Interface* (socket) en el que la usa. Las principales:
   - Especialidades provee `IEspecialidades` → la requiere Médicos.
   - Médicos provee `IMedicos` → la requieren Disponibilidad y Citas.
   - Disponibilidad provee `IDisponibilidad` → la requiere Citas.
   - Recepción provee `IPacientes` → la requiere Citas.
   - Autenticación provee `IAutenticacion` → la requiere Seguridad.
   - Integración de pagos provee `IPagos` → la requiere Citas.
   - Persistencia provee `IRepositorios` → la requieren todos los de negocio.
6. **Delegate**: conectores punteados con estereotipo «delegate» desde `pRest` a los componentes que atienden peticiones, y desde Integración de pagos a `pPago` y Persistencia a `pBD`.
7. Fuera del subsistema: el componente *App web/móvil (React)* (requiere `API REST`), los 4 actores humanos conectados a la app, *Pasarela de pagos* (provee `Cobro y reembolso`) y la base *MongoDB* (provee `Driver MongoDB`).

### 3. Vista lógica (clases)

1. *Diagram → New → Class Diagram*.
2. Opción rápida: *Tools → Code → Instant Reverse…*, elige *Java source*, apunta a `backend/src/main/java/com/citasalud/model` y arrastra las clases al diagrama. Luego borra lo que sobre y ajusta según el PNG.
3. Revisa contra `vista-logica.png`:
   - `Usuario` **abstracta** (nombre en cursiva) con un solo triángulo de herencia hacia Paciente, Medico, Recepcionista y Administrador.
   - `EstrategiaTarifa` y `PasarelaPagos` como «interface», con realización (línea punteada y triángulo hueco).
   - Enumeraciones `Rol`, `TipoPaciente`, `EstadoCita`.
   - Multiplicidades en **ambos extremos**: Paciente 1 — 0..* Cita, Medico 1 — 0..* Cita, Medico 0..* → 1 Especialidad, Medico 1 ◆— 0..* HorarioAtencion (composición), ReporteCitas 1 ◆— 0..* LineaReporte.
   - Constructor, getters/setters y métodos de negocio en cada clase.
4. Agrega una **nota** con los patrones GRASP y GoF (cópiala de la leyenda del PNG) y pinta de otro color las clases Experto/Creador.

### 4. Vista de desarrollo (clases por capas)

1. *Class Diagram* con 4 paquetes: `Presentacion_Controller`, `Negocio_Service`, `Datos_Repository` y `Modelo_de_Dominio` (este último no es una capa).
2. Relaciones (como en el material del profesor):
   - Controller → Service: línea continua «delega».
   - Service → Repository: línea continua «guarda y consulta» o «consulta».
   - Service → dominio: punteada «crea y devuelve».
   - Repository → dominio: punteada «persiste».
   - Repositorios como «interface» que heredan de `MongoRepository`. MongoDB no se dibuja como clase.

### 5. Vista de despliegue

1. *Diagram → New → Deployment Diagram*.
2. Nodos (cubos): Dispositivo del usuario (con el entorno Navegador), Vercel, Render (con el entorno Contenedor Docker y el artefacto `citasalud-backend-1.0.0.jar`), MongoDB Atlas y GitHub.
3. Conexiones con el protocolo: HTTPS, HTTPS/JSON + JWT, TCP 27017/TLS.

### Secuencias

*Diagram → New → Sequence Diagram*. Participantes: actor, Controller, Service, Repository/clases del dominio, PasarelaPagos y MongoDB. Usa *Combined Fragment → alt* para los casos alternativos tal como en los PNG.
