// Cliente de la API REST. Todas las pantallas llaman al backend a través de esta función.
const URL_API = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export async function api(ruta, { metodo = 'GET', cuerpo, token } = {}) {
  const encabezados = { 'Content-Type': 'application/json' };
  if (token) encabezados.Authorization = `Bearer ${token}`;
  const respuesta = await fetch(URL_API + ruta, {
    method: metodo,
    headers: encabezados,
    body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
  });
  if (respuesta.status === 204) return null;
  const datos = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    const detalle = datos?.detalles?.length ? ` (${datos.detalles.join(', ')})` : '';
    throw new Error((datos?.mensaje || 'Ocurrió un error') + detalle);
  }
  return datos;
}

export const pesos = (valor) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);

export const fechaHoraLegible = (iso) =>
  new Date(iso).toLocaleString('es-CO', { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });

export const hoyISO = () => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().substring(0, 10);
};

export const ESTADOS = {
  PROGRAMADA: 'Programada',
  CANCELADA: 'Cancelada',
  ATENDIDA: 'Atendida',
  NO_ASISTIO: 'No asistió',
};

export const TIPOS_PACIENTE = { PARTICULAR: 'Particular', CONVENIO: 'Convenio', PREPAGADA: 'Prepagada' };

export const DIAS = {
  MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles', THURSDAY: 'Jueves',
  FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo',
};
