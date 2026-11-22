import { useEffect, useState } from 'react';
import { api, pesos } from '../api.js';
import { useSesion } from '../sesion.jsx';
import Mensaje from '../components/Mensaje.jsx';

// HU_003, HU_004, HU_005 y HU_016: panel del administrador.
export default function Admin() {
  const [pestana, setPestana] = useState('especialidades');
  const pestanas = { especialidades: 'Especialidades', medicos: 'Médicos', recepcionistas: 'Recepcionistas', reporte: 'Reporte' };
  return (
    <section>
      <h1>Administración</h1>
      <div className="pestanas">
        {Object.entries(pestanas).map(([k, v]) => (
          <button key={k} className={pestana === k ? 'activa' : ''} onClick={() => setPestana(k)}>{v}</button>
        ))}
      </div>
      {pestana === 'especialidades' && <Especialidades />}
      {pestana === 'medicos' && <MedicosAdmin />}
      {pestana === 'recepcionistas' && <Recepcionistas />}
      {pestana === 'reporte' && <Reporte />}
    </section>
  );
}

function useAccion(alTerminar) {
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const ejecutar = async (promesa, mensaje) => {
    setError(''); setExito('');
    try {
      await promesa;
      setExito(mensaje);
      alTerminar();
      return true;
    } catch (e) {
      setError(e.message);
      return false;
    }
  };
  return { error, exito, ejecutar };
}

function Especialidades() {
  const { token } = useSesion();
  const [lista, setLista] = useState([]);
  const [form, setForm] = useState({ nombre: '', descripcion: '', tarifa: '' });
  const cargar = () => api('/api/especialidades?incluirInactivas=true').then(setLista);
  const { error, exito, ejecutar } = useAccion(cargar);
  useEffect(() => { cargar(); }, []);

  const crear = async (e) => {
    e.preventDefault();
    const ok = await ejecutar(api('/api/especialidades', { metodo: 'POST', token, cuerpo: { ...form, tarifa: Number(form.tarifa) } }), 'Especialidad creada');
    if (ok) setForm({ nombre: '', descripcion: '', tarifa: '' });
  };

  return (
    <>
      <form onSubmit={crear}>
        <input placeholder="Nombre" value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required />
        <input placeholder="Descripción" value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
        <input placeholder="Tarifa" type="number" min="1" value={form.tarifa} onChange={(e) => setForm({ ...form, tarifa: e.target.value })} required />
        <button className="boton">Crear especialidad</button>
      </form>
      <Mensaje error={error} exito={exito} />
      {lista.map((e) => (
        <div className="tarjeta fila" key={e.id}>
          <span><strong>{e.nombre}</strong> · {pesos(e.tarifa)} {!e.activa && <em className="suave">(inactiva)</em>}</span>
          {e.activa && <button className="boton peligro" onClick={() => ejecutar(api(`/api/especialidades/${e.id}`, { metodo: 'DELETE', token }), 'Especialidad desactivada')}>Desactivar</button>}
        </div>
      ))}
    </>
  );
}

function MedicosAdmin() {
  const { token } = useSesion();
  const [lista, setLista] = useState([]);
  const [especialidades, setEspecialidades] = useState([]);
  const vacio = { nombre: '', correo: '', contrasena: '', telefono: '', registroMedico: '', especialidadId: '', consultorio: '' };
  const [form, setForm] = useState(vacio);
  const cargar = () => api('/api/medicos?incluirInactivos=true').then(setLista);
  const { error, exito, ejecutar } = useAccion(cargar);
  useEffect(() => { cargar(); api('/api/especialidades').then(setEspecialidades); }, []);
  const campo = (k, p, t = 'text') => (
    <input placeholder={p} type={t} value={form[k]} onChange={(e) => setForm({ ...form, [k]: e.target.value })} required />
  );

  const crear = async (e) => {
    e.preventDefault();
    if (await ejecutar(api('/api/medicos', { metodo: 'POST', token, cuerpo: form }), 'Médico registrado')) setForm(vacio);
  };

  return (
    <>
      <form onSubmit={crear}>
        {campo('nombre', 'Nombre')}{campo('correo', 'Correo', 'email')}{campo('contrasena', 'Contraseña inicial', 'password')}
        {campo('telefono', 'Teléfono')}{campo('registroMedico', 'Registro médico')}{campo('consultorio', 'Consultorio')}
        <select value={form.especialidadId} onChange={(e) => setForm({ ...form, especialidadId: e.target.value })} required>
          <option value="">Especialidad…</option>
          {especialidades.map((e) => <option key={e.id} value={e.id}>{e.nombre}</option>)}
        </select>
        <button className="boton">Registrar médico</button>
      </form>
      <p className="suave">El médico define su horario al iniciar sesión, en «Mi horario».</p>
      <Mensaje error={error} exito={exito} />
      {lista.map((m) => (
        <div className="tarjeta fila" key={m.id}>
          <span><strong>{m.nombre}</strong> · {m.especialidad} · {m.consultorio} {!m.activo && <em className="suave">(inactivo)</em>}</span>
          {m.activo && <button className="boton peligro" onClick={() => ejecutar(api(`/api/medicos/${m.id}`, { metodo: 'DELETE', token }), 'Médico desactivado')}>Desactivar</button>}
        </div>
      ))}
    </>
  );
}

function Recepcionistas() {
  const { token } = useSesion();
  const [lista, setLista] = useState([]);
  const vacio = { nombre: '', correo: '', contrasena: '', telefono: '' };
  const [form, setForm] = useState(vacio);
  const cargar = () => api('/api/recepcionistas', { token }).then(setLista);
  const { error, exito, ejecutar } = useAccion(cargar);
  useEffect(() => { cargar(); }, []);

  const crear = async (e) => {
    e.preventDefault();
    if (await ejecutar(api('/api/recepcionistas', { metodo: 'POST', token, cuerpo: form }), 'Recepcionista creada')) setForm(vacio);
  };

  return (
    <>
      <form onSubmit={crear}>
        <input placeholder="Nombre" value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required />
        <input placeholder="Correo" type="email" value={form.correo} onChange={(e) => setForm({ ...form, correo: e.target.value })} required />
        <input placeholder="Contraseña inicial" type="password" minLength={8} value={form.contrasena} onChange={(e) => setForm({ ...form, contrasena: e.target.value })} required />
        <input placeholder="Teléfono" value={form.telefono} onChange={(e) => setForm({ ...form, telefono: e.target.value })} required />
        <button className="boton">Crear recepcionista</button>
      </form>
      <Mensaje error={error} exito={exito} />
      {lista.map((r) => (
        <div className="tarjeta fila" key={r.id}>
          <span><strong>{r.nombre}</strong> · {r.correo} {!r.activo && <em className="suave">(inactiva)</em>}</span>
          {r.activo && <button className="boton peligro" onClick={() => ejecutar(api(`/api/recepcionistas/${r.id}`, { metodo: 'DELETE', token }), 'Recepcionista desactivada')}>Desactivar</button>}
        </div>
      ))}
    </>
  );
}

function Reporte() {
  const { token } = useSesion();
  const hoy = new Date();
  const iso = (d) => d.toISOString().substring(0, 10);
  const [desde, setDesde] = useState(iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)));
  const [hasta, setHasta] = useState(iso(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0)));
  const [reporte, setReporte] = useState(null);
  const [error, setError] = useState('');

  const consultar = async (e) => {
    e?.preventDefault();
    setError('');
    try {
      setReporte(await api(`/api/reportes/citas?desde=${desde}&hasta=${hasta}`, { token }));
    } catch (err) {
      setError(err.message);
      setReporte(null);
    }
  };
  useEffect(() => { consultar(); }, []);

  return (
    <>
      <form onSubmit={consultar} className="fila">
        <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
        <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
        <button className="boton">Consultar</button>
      </form>
      <Mensaje error={error} />
      {reporte && (
        <div className="tabla">
          <table>
            <thead>
              <tr><th>Especialidad</th><th>Prog.</th><th>Atend.</th><th>Canc.</th><th>No asist.</th><th>Total</th><th>Recaudado</th></tr>
            </thead>
            <tbody>
              {reporte.lineas.map((l) => (
                <tr key={l.especialidad}>
                  <td>{l.especialidad}</td><td>{l.programadas}</td><td>{l.atendidas}</td><td>{l.canceladas}</td>
                  <td>{l.noAsistio}</td><td>{l.total}</td><td>{pesos(l.recaudado)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr><td>Total</td><td colSpan={4}></td><td>{reporte.totalCitas}</td><td>{pesos(reporte.totalRecaudado)}</td></tr>
            </tfoot>
          </table>
        </div>
      )}
    </>
  );
}
