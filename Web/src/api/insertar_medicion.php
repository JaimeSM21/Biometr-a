<?php
// Archivo: api_insertar_medicion.php
header("Content-Type: application/json; charset=utf-8");
require_once 'conexion.php';

if ($_SERVER['REQUEST_METHOD'] != 'POST') {
    http_response_code(405);
    echo json_encode(array("ok" => false, "error" => "Método no permitido. Usa POST."));
    exit;
}

try {
    $tipo_medicion = isset($_POST['tipo_medicion']) ? intval($_POST['tipo_medicion']) : null;
    $medicion = isset($_POST['medicion']) ? floatval($_POST['medicion']) : null;
    $numero_medicion = isset($_POST['numero_medicion']) ? intval($_POST['numero_medicion']) : 0;

    if ($tipo_medicion === null || $medicion === null) {
        http_response_code(400);
        echo json_encode(array("ok" => false, "error" => "Faltan campos requeridos."));
        exit;
    }

    $pdo = get_pdo();
    $stmt = $pdo->prepare("INSERT INTO mediciones (tipo_medicion, medicion, numero_medicion)
                           VALUES (:tipo_medicion, :medicion, :numero_medicion)");
    $stmt->execute(array(
        ':tipo_medicion' => $tipo_medicion,
        ':medicion' => $medicion,
        ':numero_medicion' => $numero_medicion
    ));

    echo json_encode(array(
        "ok" => true,
        "id" => (int)$pdo->lastInsertId()
    ));
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        "ok" => false,
        "error" => "Error del servidor: " . $e->getMessage()
    ));
}

