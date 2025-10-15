// Mapea el tipo numérico a nombre legible (ajústalo a tus IDs reales)
const TIPOS = {
    11: "CO₂",
    12: "Temperatura",
    13: "Ruido",
};

const elValor = document.getElementById("valorMedicion");
const elTipo = document.getElementById("tipoMedicion");
const elNum = document.getElementById("numeroMedicion");
const elFecha = document.getElementById("fechaMedicion");
const elEstado = document.getElementById("estado");

async function cargarUltimaMedicion() {
    try {
        elEstado.textContent = "Actualizando…";
        const resp = await fetch("api/ultima_medicion.php", { cache: "no-store" });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const data = await resp.json();

        if (!data || !("medicion" in data)) {
            throw new Error("Respuesta inesperada del servidor.");
        }

        elValor.textContent = String(data.medicion);
        elTipo.textContent = TIPOS[data.tipo_medicion] || `Tipo ${data.tipo_medicion}`;
        elNum.textContent = data.numero_medicion ?? "—";
        // Muestra fecha tal cual la envía PHP; si quieres, formatea en local:
        elFecha.textContent = data.fecha ?? "—";
        elEstado.textContent = "OK";
    } catch (err) {
        console.error(err);
        elEstado.textContent = "Error al cargar la medición.";
    }
}

// Carga inicial y refresco periódico (cada 5 s). Ajusta si quieres.
document.addEventListener("DOMContentLoaded", () => {
    cargarUltimaMedicion();
    setInterval(cargarUltimaMedicion, 5000);
});
