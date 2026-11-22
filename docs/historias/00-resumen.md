# Resumen de historias de usuario

CitaSalud tiene **16 historias de usuario** agrupadas en 5 épicas, con **43 puntos** en total.

| Épica | HU | Título | Actor | Prioridad | Puntos | RF |
|---|---|---|---|---|---|---|
| 1. Cuenta | [HU_001](HU_001.md) | Registrarse como paciente | Visitante | Alta | 3 | RF_001 |
| 1. Cuenta | [HU_002](HU_002.md) | Iniciar sesión | Paciente, Médico, Recepcionista, Administrador | Alta | 3 | RF_002 |
| 2. Administración | [HU_003](HU_003.md) | Gestionar especialidades | Administrador | Alta | 2 | RF_003 |
| 2. Administración | [HU_004](HU_004.md) | Gestionar médicos | Administrador | Alta | 2 | RF_004 |
| 2. Administración | [HU_005](HU_005.md) | Gestionar recepcionistas | Administrador | Media | 2 | RF_005 |
| 2. Administración | [HU_006](HU_006.md) | Configurar horario de atención | Médico, Administrador | Alta | 3 | RF_006 |
| 3. Consulta | [HU_007](HU_007.md) | Consultar especialidades y médicos | Visitante, Paciente | Alta | 2 | RF_007 |
| 3. Consulta | [HU_008](HU_008.md) | Consultar disponibilidad de un médico | Visitante, Paciente, Recepcionista | Alta | 3 | RF_008 |
| 4. Citas | [HU_009](HU_009.md) | Agendar cita | Paciente | Alta | 5 | RF_009, RF_010, RF_011 |
| 4. Citas | [HU_010](HU_010.md) | Consultar mis citas | Paciente | Media | 2 | RF_012 |
| 4. Citas | [HU_011](HU_011.md) | Cancelar cita | Paciente, Recepcionista | Media | 2 | RF_013 |
| 4. Citas | [HU_012](HU_012.md) | Reprogramar cita | Paciente, Recepcionista | Media | 3 | RF_014 |
| 4. Citas | [HU_013](HU_013.md) | Agendar cita para un paciente | Recepcionista | Media | 3 | RF_015 |
| 5. Atención y reportes | [HU_014](HU_014.md) | Consultar agenda del día | Médico | Alta | 2 | RF_016 |
| 5. Atención y reportes | [HU_015](HU_015.md) | Registrar atención o inasistencia | Médico | Alta | 3 | RF_017 |
| 5. Atención y reportes | [HU_016](HU_016.md) | Consultar reporte de citas | Administrador | Media | 3 | RF_018 |

## Reglas de negocio

- **RN_01.** Valor de la cita = tarifa de la especialidad − descuento por tipo de paciente + cargo administrativo.
- **RN_02.** Descuentos por tipo de paciente: Particular 0 %, Convenio 15 %, Prepagada 30 %.
- **RN_03.** Cargo administrativo: $5.000 por cita (configurable).
- **RN_04.** Las citas duran 30 minutos y solo se agendan dentro del horario de atención del médico, en fechas futuras.
- **RN_05.** Un médico no puede tener dos citas programadas en la misma franja.
- **RN_06.** Un paciente no puede tener dos citas a la misma hora ni dos citas programadas en la misma especialidad.
- **RN_07.** Estados de la cita: Programada, Cancelada, Atendida, No asistió.
- **RN_08.** Una cita solo se cancela o reprograma si faltan más de 24 horas.
- **RN_09.** La cita solo queda programada si la pasarela de pagos aprueba el pago.
- **RN_10.** Al cancelar una cita se solicita el reembolso total a la pasarela.
- **RN_11.** Una cita se reprograma máximo 2 veces.
- **RN_12.** Solo el médico de la cita registra la atención o la inasistencia, y solo cuando la hora de la cita ya llegó.

Ejemplo con una tarifa de $60.000: Particular $65.000, Convenio $56.000, Prepagada $47.000.
