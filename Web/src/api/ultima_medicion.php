<?php
// Archivo: api_ultima_medicion.php
header("Content-Type: application/json; charset=utf-8");
// header("Access-Control-Allow-Origin: *"); // activa si lo necesitas

require_once 'conexion.php';

try {
    $pdo = get_pdo();

    $sql = "SELECT id, tipo_medicion, medicion, numero_medicion, fecha
            FROM mediciones
            ORDER BY fecha DESC, id DESC
            LIMIT 1";
    $stmt = $pdo->query($sql);
    $row = $stmt->fetch();

    if (!$row) {
        echo json_encode(array(
            "ok" => false,
            "error" => "No hay datos en la tabla 'mediciones'."
        ));
        exit;
    }

    echo json_encode(array(
        "ok" => true,
        "id" => (int)$row["id"],
        "tipo_medicion" => (int)$row["tipo_medicion"],
        "medicion" => (float)$row["medicion"],
        "numero_medicion" => (int)$row["numero_medicion"],
        "fecha" => $row["fecha"]
    ));
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        "ok" => false,
        "error" => "Error del servidor: " . $e->getMessage()
    ));
}

