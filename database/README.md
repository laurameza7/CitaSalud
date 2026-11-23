# Base de datos (MongoDB)

CitaSalud usa una base de datos MongoDB llamada `citasalud` con cuatro colecciones. Spring Data MongoDB crea las colecciones y los índices al arrancar.

| Colección | Clase del modelo | Contenido | Índices |
|---|---|---|---|
| `usuarios` | `Usuario` (abstracta) y sus hijas `Paciente`, `Medico`, `Recepcionista`, `Administrador` | Todas las cuentas. El campo `_class` guarda el tipo concreto y `rol` el rol | `correo` único |
| `especialidades` | `Especialidad` | Nombre, descripción, tarifa y si está activa | `nombre` único |
| `citas` | `Cita` | Paciente, médico, especialidad, fecha y hora, estado, valor pagado, referencia de pago, reprogramaciones y notas | `fechaHora` |
| `recordatorios` | `Recordatorio` | Cita recordada, paciente, fecha de la cita, mensaje, fecha de creación y si está leído | `pacienteId` |

## Relaciones

- `Medico.especialidad` → `especialidades` (referencia `@DBRef`).
- `Cita.paciente` y `Cita.medico` → `usuarios` (referencia `@DBRef`).
- `Cita.especialidad` → `especialidades` (referencia `@DBRef`).
- `Recordatorio.cita` → `citas` (referencia `@DBRef`).
- `Medico.horarios` es una lista de `HorarioAtencion` **embebida** dentro del documento del médico (no tiene colección propia).
- `ReporteCitas` y `LineaReporte` no se guardan: se calculan al pedir el reporte.

## Ejemplo de documento de cita

```json
{
  "_id": "ObjectId(...)",
  "paciente": { "$ref": "usuarios", "$id": "ObjectId(...)" },
  "medico": { "$ref": "usuarios", "$id": "ObjectId(...)" },
  "especialidad": { "$ref": "especialidades", "$id": "ObjectId(...)" },
  "fechaHora": "2026-11-30T14:00:00Z",
  "estado": "PROGRAMADA",
  "motivo": "Control general",
  "valorPagado": 47500,
  "referenciaPago": "PAGO-1A2B3C4D",
  "reembolsada": false,
  "reprogramaciones": 0,
  "_class": "com.citasalud.model.Cita"
}
```

## Datos de prueba

Si la variable `DATOS_PRUEBA` es `true` (valor por defecto) y la colección `usuarios` está vacía, `config/DatosIniciales.java` carga 4 especialidades, 5 médicos con horario, 3 pacientes, 1 recepcionista, 1 administrador y algunas citas (una de Ana para las próximas 24 horas, para ver su recordatorio). Las cuentas están en el README principal.

Para empezar de cero en local: `docker compose down -v` (borra el volumen de MongoDB).
