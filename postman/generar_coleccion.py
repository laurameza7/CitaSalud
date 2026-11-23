"""Genera la colección de Postman de CitaSalud (CitaSalud.postman_collection.json).

Uso: python postman/generar_coleccion.py
Cada carpeta corresponde a una historia de usuario y cada petición tiene pruebas automáticas.
"""
import json
import os

AQUI = os.path.dirname(os.path.abspath(__file__))


def peticion(nombre, metodo, ruta, token=None, cuerpo=None, estado=200, pruebas="", previo=""):
    encabezados = [{"key": "Content-Type", "value": "application/json"}]
    if token:
        encabezados.append({"key": "Authorization", "value": "Bearer {{" + token + "}}"})
    ruta_sin_query, _, query = ruta.partition("?")
    url = {
        "raw": "{{baseUrl}}" + ruta,
        "host": ["{{baseUrl}}"],
        "path": [p for p in ruta_sin_query.split("/") if p],
    }
    if query:
        url["query"] = [{"key": q.split("=")[0], "value": q.split("=", 1)[1]} for q in query.split("&")]
    req = {"method": metodo, "header": encabezados, "url": url}
    if cuerpo is not None:
        req["body"] = {"mode": "raw", "raw": json.dumps(cuerpo, ensure_ascii=False, indent=2),
                       "options": {"raw": {"language": "json"}}}
    script = [f"pm.test('Responde {estado}', () => pm.response.to.have.status({estado}));"]
    if pruebas:
        script += pruebas.strip().split("\n")
    eventos = [{"listen": "test", "script": {"type": "text/javascript", "exec": script}}]
    if previo:
        eventos.append({"listen": "prerequest", "script": {"type": "text/javascript", "exec": previo.strip().split("\n")}})
    return {"name": nombre, "event": eventos, "request": req}


def carpeta(nombre, items, descripcion=""):
    return {"name": nombre, "description": descripcion, "item": items}


PREVIO_COLECCION = r"""
// Fechas dinámicas: el próximo lunes con al menos 2 días de anticipación (para cumplir la regla de 24 horas).
const hoy = new Date();
const lunes = new Date(hoy);
lunes.setDate(hoy.getDate() + 2);
while (lunes.getDay() !== 1) { lunes.setDate(lunes.getDate() + 1); }
const domingo = new Date(lunes); domingo.setDate(lunes.getDate() - 1);
const f = d => d.toISOString().substring(0, 10);
pm.collectionVariables.set('fechaLunes', f(lunes));
pm.collectionVariables.set('fechaDomingo', f(domingo));
pm.collectionVariables.set('inicioMes', f(new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1)));
pm.collectionVariables.set('finRango', f(new Date(lunes.getFullYear(), lunes.getMonth() + 1, 0)));
"""

guardar = lambda var, expr: f"pm.collectionVariables.set('{var}', {expr});"

items = [
    carpeta("00. Estado de la API", [
        peticion("Estado", "GET", "/api/estado", pruebas="pm.test('Funciona', () => pm.expect(pm.response.json().estado).to.eql('funcionando'));"),
    ]),
    carpeta("HU_002 Iniciar sesión (cuentas de prueba)", [
        peticion("Login administrador", "POST", "/api/auth/login", cuerpo={"correo": "admin@citasalud.com", "contrasena": "Admin123*"},
                 pruebas=guardar("tokenAdmin", "pm.response.json().token") + "\npm.test('Rol administrador', () => pm.expect(pm.response.json().usuario.rol).to.eql('ADMINISTRADOR'));"),
        peticion("Login recepcionista", "POST", "/api/auth/login", cuerpo={"correo": "recepcion@citasalud.com", "contrasena": "Recepcion123*"},
                 pruebas=guardar("tokenRecepcion", "pm.response.json().token")),
        peticion("Login médico (Carlos Ruiz)", "POST", "/api/auth/login", cuerpo={"correo": "carlos.ruiz@citasalud.com", "contrasena": "Medico123*"},
                 pruebas=guardar("tokenMedico", "pm.response.json().token") + "\n" + guardar("medicoCarlosId", "pm.response.json().usuario.id")),
        peticion("Login paciente (Ana Pérez)", "POST", "/api/auth/login", cuerpo={"correo": "ana.perez@correo.com", "contrasena": "Paciente123*"},
                 pruebas=guardar("tokenAna", "pm.response.json().token")),
        peticion("Login con contraseña incorrecta", "POST", "/api/auth/login", cuerpo={"correo": "ana.perez@correo.com", "contrasena": "equivocada"}, estado=401),
    ]),
    carpeta("HU_001 Registrarse como paciente", [
        peticion("Registro exitoso", "POST", "/api/auth/registro", estado=201,
                 previo="const n = Date.now();\npm.collectionVariables.set('correoNuevo', 'paciente' + n + '@correo.com');\npm.collectionVariables.set('documentoNuevo', String(n).substring(3));",
                 cuerpo={"nombre": "Paciente de Prueba", "documento": "{{documentoNuevo}}", "correo": "{{correoNuevo}}", "telefono": "3200000000",
                         "fechaNacimiento": "1995-05-20", "tipoPaciente": "CONVENIO", "contrasena": "Prueba123*"},
                 pruebas="pm.test('Queda como paciente', () => pm.expect(pm.response.json().rol).to.eql('PACIENTE'));\npm.test('No devuelve la contraseña', () => pm.expect(pm.response.json()).to.not.have.property('contrasenaHash'));"),
        peticion("Correo ya registrado", "POST", "/api/auth/registro", estado=409,
                 cuerpo={"nombre": "Otra", "documento": "999999", "correo": "{{correoNuevo}}", "telefono": "3200000000",
                         "fechaNacimiento": "1995-05-20", "tipoPaciente": "PARTICULAR", "contrasena": "Prueba123*"}),
        peticion("Datos incompletos", "POST", "/api/auth/registro", estado=400,
                 cuerpo={"nombre": "Sin datos", "correo": "malo", "contrasena": "123"},
                 pruebas="pm.test('Explica los campos', () => pm.expect(pm.response.json().detalles.length).to.be.above(0));"),
        peticion("Login del paciente nuevo", "POST", "/api/auth/login", cuerpo={"correo": "{{correoNuevo}}", "contrasena": "Prueba123*"},
                 pruebas=guardar("tokenPaciente", "pm.response.json().token") + "\n" + guardar("pacienteNuevoId", "pm.response.json().usuario.id")),
        peticion("Mi perfil", "GET", "/api/auth/yo", token="tokenPaciente",
                 pruebas="pm.test('Es el paciente nuevo', () => pm.expect(pm.response.json().correo).to.eql(pm.collectionVariables.get('correoNuevo')));"),
    ]),
    carpeta("HU_003 Gestionar especialidades", [
        peticion("Crear especialidad", "POST", "/api/especialidades", token="tokenAdmin", estado=201,
                 previo="pm.collectionVariables.set('nombreEspecialidad', 'Neurología ' + Date.now());",
                 cuerpo={"nombre": "{{nombreEspecialidad}}", "descripcion": "Sistema nervioso", "tarifa": 140000},
                 pruebas=guardar("especialidadNuevaId", "pm.response.json().id")),
        peticion("Nombre repetido", "POST", "/api/especialidades", token="tokenAdmin", estado=409,
                 cuerpo={"nombre": "{{nombreEspecialidad}}", "descripcion": "x", "tarifa": 1000}),
        peticion("Sin iniciar sesión", "POST", "/api/especialidades", estado=401,
                 cuerpo={"nombre": "X", "descripcion": "x", "tarifa": 1000}),
        peticion("Un paciente no puede crear", "POST", "/api/especialidades", token="tokenAna", estado=403,
                 cuerpo={"nombre": "X", "descripcion": "x", "tarifa": 1000}),
        peticion("Editar especialidad", "PUT", "/api/especialidades/{{especialidadNuevaId}}", token="tokenAdmin",
                 cuerpo={"nombre": "{{nombreEspecialidad}}", "descripcion": "Cerebro y nervios", "tarifa": 150000},
                 pruebas="pm.test('Nueva tarifa', () => pm.expect(pm.response.json().tarifa).to.eql(150000));"),
    ]),
    carpeta("HU_004 Gestionar médicos", [
        peticion("Registrar médico", "POST", "/api/medicos", token="tokenAdmin", estado=201,
                 previo="pm.collectionVariables.set('correoMedico', 'medico' + Date.now() + '@citasalud.com');",
                 cuerpo={"nombre": "Valentina Muñoz", "correo": "{{correoMedico}}", "contrasena": "Medico123*", "telefono": "3011112233",
                         "registroMedico": "RM-55555", "especialidadId": "{{especialidadNuevaId}}", "consultorio": "Consultorio 501"},
                 pruebas=guardar("medicoNuevoId", "pm.response.json().id")),
        peticion("Especialidad inexistente", "POST", "/api/medicos", token="tokenAdmin", estado=404,
                 cuerpo={"nombre": "X", "correo": "x" + "@citasalud.com", "contrasena": "Medico123*", "telefono": "1",
                         "registroMedico": "RM", "especialidadId": "000000000000000000000000", "consultorio": "1"}),
        peticion("Médicos de la especialidad nueva", "GET", "/api/medicos?especialidadId={{especialidadNuevaId}}",
                 pruebas="pm.test('Aparece el médico', () => pm.expect(pm.response.json().map(m => m.id)).to.include(pm.collectionVariables.get('medicoNuevoId')));"),
    ]),
    carpeta("HU_005 Gestionar recepcionistas", [
        peticion("Crear recepcionista", "POST", "/api/recepcionistas", token="tokenAdmin", estado=201,
                 previo="pm.collectionVariables.set('correoRecep', 'recep' + Date.now() + '@citasalud.com');",
                 cuerpo={"nombre": "Pedro Bravo", "correo": "{{correoRecep}}", "contrasena": "Recepcion123*", "telefono": "3005556677"},
                 pruebas=guardar("recepcionistaNuevaId", "pm.response.json().id")),
        peticion("Correo repetido", "POST", "/api/recepcionistas", token="tokenAdmin", estado=409,
                 cuerpo={"nombre": "Otra", "correo": "{{correoRecep}}", "contrasena": "Recepcion123*", "telefono": "1"}),
        peticion("Listar recepcionistas", "GET", "/api/recepcionistas", token="tokenAdmin"),
    ]),
    carpeta("HU_006 Configurar horario de atención", [
        peticion("Definir horario (administrador)", "PUT", "/api/medicos/{{medicoNuevoId}}/horarios", token="tokenAdmin",
                 cuerpo=[{"diaSemana": "MONDAY", "horaInicio": "08:00", "horaFin": "12:00"},
                         {"diaSemana": "THURSDAY", "horaInicio": "14:00", "horaFin": "16:00"}],
                 pruebas="pm.test('Dos bloques', () => pm.expect(pm.response.json().horarios.length).to.eql(2));"),
        peticion("Horario inválido", "PUT", "/api/medicos/{{medicoNuevoId}}/horarios", token="tokenAdmin", estado=422,
                 cuerpo=[{"diaSemana": "MONDAY", "horaInicio": "12:00", "horaFin": "08:00"}]),
        peticion("Un médico no cambia el horario de otro", "PUT", "/api/medicos/{{medicoNuevoId}}/horarios", token="tokenMedico", estado=403,
                 cuerpo=[{"diaSemana": "MONDAY", "horaInicio": "08:00", "horaFin": "09:00"}]),
    ]),
    carpeta("HU_007 Consultar especialidades y médicos", [
        peticion("Especialidades (sin iniciar sesión)", "GET", "/api/especialidades",
                 pruebas="pm.test('Hay especialidades', () => pm.expect(pm.response.json().length).to.be.above(0));"),
        peticion("Médicos (sin iniciar sesión)", "GET", "/api/medicos",
                 pruebas="pm.test('Hay médicos', () => pm.expect(pm.response.json().length).to.be.above(0));"),
    ]),
    carpeta("HU_008 Consultar disponibilidad", [
        peticion("Franjas libres de Carlos Ruiz el lunes", "GET", "/api/medicos/{{medicoCarlosId}}/disponibilidad?fecha={{fechaLunes}}",
                 pruebas="pm.test('Incluye las 09:00', () => pm.expect(pm.response.json().franjasLibres).to.include('09:00:00'));"),
        peticion("Domingo sin atención", "GET", "/api/medicos/{{medicoCarlosId}}/disponibilidad?fecha={{fechaDomingo}}",
                 pruebas="pm.test('Sin franjas', () => pm.expect(pm.response.json().franjasLibres.length).to.eql(0));"),
    ]),
    carpeta("HU_009 Agendar cita", [
        peticion("Valor para paciente de prepagada", "GET", "/api/especialidades/{{especialidadNuevaId}}/valor?tipoPaciente=PREPAGADA",
                 pruebas="pm.test('150.000 - 30 % + 5.000 = 110.000', () => pm.expect(pm.response.json().total).to.eql(110000));"),
        peticion("Agendar cita (pago aprobado)", "POST", "/api/citas", token="tokenPaciente", estado=201,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T09:00", "motivo": "Control general", "numeroTarjeta": "4111111111111111"},
                 pruebas=guardar("citaId", "pm.response.json().id") +
                 "\npm.test('Queda programada', () => pm.expect(pm.response.json().estado).to.eql('PROGRAMADA'));"
                 "\npm.test('Convenio paga 47.500 (50.000 - 15 % + 5.000)', () => pm.expect(pm.response.json().valorPagado).to.eql(47500));"),
        peticion("Franja ocupada", "POST", "/api/citas", token="tokenAna", estado=409,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T09:00", "motivo": "x", "numeroTarjeta": "4111111111111111"}),
        peticion("Pago rechazado (tarjeta termina en 0000)", "POST", "/api/citas", token="tokenAna", estado=402,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T09:30", "motivo": "x", "numeroTarjeta": "4111111111110000"}),
        peticion("Ya tiene cita en la misma especialidad", "POST", "/api/citas", token="tokenPaciente", estado=409,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T10:00", "motivo": "x", "numeroTarjeta": "4111111111111111"}),
        peticion("Fuera del horario del médico", "POST", "/api/citas", token="tokenAna", estado=422,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T15:00", "motivo": "x", "numeroTarjeta": "4111111111111111"}),
    ]),
    carpeta("HU_010 Consultar mis citas", [
        peticion("Mis citas", "GET", "/api/citas/mias", token="tokenPaciente",
                 pruebas="pm.test('Aparece la cita agendada', () => pm.expect(pm.response.json().map(c => c.id)).to.include(pm.collectionVariables.get('citaId')));"),
        peticion("Un paciente no ve la cita de otro", "GET", "/api/citas/{{citaId}}", token="tokenAna", estado=403),
    ]),
    carpeta("HU_012 Reprogramar cita", [
        peticion("Reprogramar a las 10:30", "PUT", "/api/citas/{{citaId}}/reprogramar", token="tokenPaciente",
                 cuerpo={"nuevaFechaHora": "{{fechaLunes}}T10:30"},
                 pruebas="pm.test('Una reprogramación', () => pm.expect(pm.response.json().reprogramaciones).to.eql(1));"),
        peticion("Otro paciente no puede reprogramarla", "PUT", "/api/citas/{{citaId}}/reprogramar", token="tokenAna", estado=403,
                 cuerpo={"nuevaFechaHora": "{{fechaLunes}}T11:00"}),
    ]),
    carpeta("HU_013 Agendar cita para un paciente", [
        peticion("Buscar paciente por documento", "GET", "/api/pacientes?documento=1085000333", token="tokenRecepcion",
                 pruebas=guardar("pacienteCamilaId", "pm.response.json().id")),
        peticion("Documento inexistente", "GET", "/api/pacientes?documento=000", token="tokenRecepcion", estado=404),
        peticion("Agendar para Camila", "POST", "/api/citas", token="tokenRecepcion", estado=201,
                 cuerpo={"medicoId": "{{medicoCarlosId}}", "fechaHora": "{{fechaLunes}}T11:00", "motivo": "Llamó por teléfono",
                         "numeroTarjeta": "4111111111111111", "pacienteId": "{{pacienteCamilaId}}"},
                 pruebas=guardar("citaRecepcionId", "pm.response.json().id") +
                 "\npm.test('Prepagada paga 40.000', () => pm.expect(pm.response.json().valorPagado).to.eql(40000));"),
    ]),
    carpeta("HU_014 Consultar agenda del día", [
        peticion("Agenda de Carlos el lunes", "GET", "/api/citas/agenda?fecha={{fechaLunes}}", token="tokenMedico",
                 pruebas="pm.test('Incluye la cita reprogramada', () => pm.expect(pm.response.json().map(c => c.id)).to.include(pm.collectionVariables.get('citaId')));"),
        peticion("Un paciente no tiene agenda", "GET", "/api/citas/agenda?fecha={{fechaLunes}}", token="tokenPaciente", estado=403),
    ]),
    carpeta("HU_015 Registrar atención o inasistencia", [
        peticion("No se atiende una cita futura", "PUT", "/api/citas/{{citaId}}/atencion", token="tokenMedico", estado=422,
                 cuerpo={"notas": "Paciente estable"}),
        peticion("Notas obligatorias", "PUT", "/api/citas/{{citaId}}/atencion", token="tokenMedico", estado=400,
                 cuerpo={"notas": ""}),
    ]),
    carpeta("HU_011 Cancelar cita", [
        peticion("Cancelar con reembolso", "PUT", "/api/citas/{{citaId}}/cancelar", token="tokenPaciente",
                 pruebas="pm.test('Cancelada', () => pm.expect(pm.response.json().estado).to.eql('CANCELADA'));\npm.test('Reembolsada', () => pm.expect(pm.response.json().reembolsada).to.be.true);"),
        peticion("No se cancela dos veces", "PUT", "/api/citas/{{citaId}}/cancelar", token="tokenPaciente", estado=422),
        peticion("Recepcionista cancela la cita de Camila", "PUT", "/api/citas/{{citaRecepcionId}}/cancelar", token="tokenRecepcion"),
    ]),
    carpeta("HU_016 Consultar reporte de citas", [
        peticion("Reporte", "GET", "/api/reportes/citas?desde={{inicioMes}}&hasta={{finRango}}", token="tokenAdmin",
                 pruebas="pm.test('Tiene líneas', () => pm.expect(pm.response.json().lineas.length).to.be.above(0));"),
        peticion("Rango invertido", "GET", "/api/reportes/citas?desde={{finRango}}&hasta={{inicioMes}}", token="tokenAdmin", estado=422),
        peticion("Solo el administrador", "GET", "/api/reportes/citas?desde={{inicioMes}}&hasta={{finRango}}", token="tokenAna", estado=403),
    ]),
    carpeta("HU_017 Recibir recordatorios de citas", [
        peticion("Generar recordatorios ahora (recepción)", "POST", "/api/recordatorios/generar", token="tokenRecepcion",
                 pruebas="pm.test('Devuelve cuántos creó', () => pm.expect(pm.response.json().recordatoriosCreados).to.be.at.least(0));"),
        peticion("Mis recordatorios (Ana tiene una cita mañana)", "GET", "/api/recordatorios/mios", token="tokenAna",
                 pruebas="pm.test('Es una lista', () => pm.expect(pm.response.json()).to.be.an('array'));\n"
                 "const lista = pm.response.json();\n"
                 "pm.collectionVariables.set('recordatorioId', lista.length ? lista[0].id : '');"),
        peticion("Marcar recordatorio como leído", "PUT", "/api/recordatorios/{{recordatorioId}}/leido", token="tokenAna",
                 previo="if (!pm.collectionVariables.get('recordatorioId')) { pm.execution.skipRequest(); }",
                 pruebas="pm.test('Queda leído', () => pm.expect(pm.response.json().leido).to.be.true);"),
        peticion("Otro paciente no puede marcarlo", "PUT", "/api/recordatorios/{{recordatorioId}}/leido", token="tokenPaciente", estado=403,
                 previo="if (!pm.collectionVariables.get('recordatorioId')) { pm.execution.skipRequest(); }"),
        peticion("Un médico no tiene recordatorios", "GET", "/api/recordatorios/mios", token="tokenMedico", estado=403),
        peticion("Un paciente no puede generarlos", "POST", "/api/recordatorios/generar", token="tokenAna", estado=403),
    ]),
    carpeta("99. Limpieza de datos de prueba", [
        peticion("Desactivar médico de prueba", "DELETE", "/api/medicos/{{medicoNuevoId}}", token="tokenAdmin", estado=204),
        peticion("Desactivar especialidad de prueba", "DELETE", "/api/especialidades/{{especialidadNuevaId}}", token="tokenAdmin", estado=204),
        peticion("Desactivar recepcionista de prueba", "DELETE", "/api/recepcionistas/{{recepcionistaNuevaId}}", token="tokenAdmin", estado=204),
    ]),
]

coleccion = {
    "info": {
        "name": "CitaSalud",
        "description": "Pruebas de la API de CitaSalud organizadas por historia de usuario. Ejecutar en orden con el Runner.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "event": [{"listen": "prerequest", "script": {"type": "text/javascript", "exec": PREVIO_COLECCION.strip().split("\n")}}],
    "variable": [],
    "item": items,
}

for nombre, url in [("Local", "http://localhost:8080"), ("Nube", "https://CAMBIAR-POR-TU-BACKEND.onrender.com")]:
    entorno = {"name": f"CitaSalud - {nombre}", "values": [{"key": "baseUrl", "value": url, "enabled": True}]}
    with open(os.path.join(AQUI, f"CitaSalud-{nombre}.postman_environment.json"), "w", encoding="utf-8") as f:
        json.dump(entorno, f, ensure_ascii=False, indent=2)

with open(os.path.join(AQUI, "CitaSalud.postman_collection.json"), "w", encoding="utf-8") as f:
    json.dump(coleccion, f, ensure_ascii=False, indent=2)

total = sum(len(c["item"]) for c in items)
print(f"Colección generada con {total} peticiones")
