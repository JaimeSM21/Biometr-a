<?php
// Archivo: db.php
// Conexión genérica a la base de datos MySQL usando PDO (compatible con PHP 5)

function get_pdo() {
    $DB_HOST = 'localhost';
    $DB_NAME = 'jsanmar6_Biometria';
    $DB_USER = 'jsanmar6';
    $DB_PASS = 'ProyectoBio';
    $DB_CHARSET = 'utf8mb4';

    try {
        $dsn = "mysql:host=$DB_HOST;dbname=$DB_NAME;charset=$DB_CHARSET";
        $pdo = new PDO($dsn, $DB_USER, $DB_PASS);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        return $pdo;
    } catch (PDOException $e) {
        die('Error de conexión: ' . $e->getMessage());
    }
}
