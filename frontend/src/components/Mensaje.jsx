// Muestra un mensaje de error o de éxito.
export default function Mensaje({ error, exito }) {
  if (error) return <p className="mensaje error">{error}</p>;
  if (exito) return <p className="mensaje exito">{exito}</p>;
  return null;
}
