# 1. Introducción

## Propósito
Definir los requisitos, el diseño y la forma de probar CitaSalud, un sistema para que una clínica gestione sus citas médicas.

## Alcance
CitaSalud permite consultar especialidades, médicos y horarios libres; agendar y pagar citas; cancelarlas o reprogramarlas; que los médicos gestionen su horario y registren la atención; que la recepción agende por los pacientes; y que el administrador gestione la clínica y consulte reportes. El pago se hace con una pasarela simulada.

Fuera del alcance: historia clínica completa, fórmulas médicas, facturación electrónica y notificaciones por correo o SMS.

## Definiciones
| Término | Significado |
|---|---|
| Franja | Bloque de 30 minutos en el que puede empezar una cita |
| Tarifa | Valor base de la consulta de una especialidad |
| Tipo de paciente | Particular, Convenio o Prepagada; define el descuento |
| HU / RF / RNF / RN | Historia de usuario / requisito funcional / no funcional / regla de negocio |
