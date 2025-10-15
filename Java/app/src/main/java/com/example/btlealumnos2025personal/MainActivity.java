package com.example.btlealumnos2025personal;
// ------------------------------------------------------------------
// ------------------------------------------------------------------

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // Para evitar reenvíos duplicados por valor (cualquier tipo)
    private int ultimoValorEnviado = Integer.MIN_VALUE;




    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado ) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        // --- Decodificar ---
        int major = Utilidades.bytesToInt(tib.getMajor());
        int minor = Utilidades.bytesToInt(tib.getMinor());

        int tipo_medicion = (major >> 8) & 0xFF;     // p.ej. 11=CO2, 12=Temperatura...
        int contador      =  major        & 0xFF;
        int medicionInt16 = (short) (minor & 0xFFFF);
        double medicion   = (double) medicionInt16;

        // ------- ANTIRREBOTE POR VALOR ÚNICO -------
        // Solo se envía si el valor cambia del último enviado
        if (medicionInt16 == ultimoValorEnviado) {
            Log.d(ETIQUETA_LOG, "valor repetido (" + medicionInt16 + "), omito esta vez.");
            return;
        }

        // Actualiza el último valor y envía
        ultimoValorEnviado = medicionInt16;

        Log.d(ETIQUETA_LOG, "ENVIAR -> tipo=" + tipo_medicion +
                " medicion=" + medicion + " numero=" + contador);
        enviarMedicionAlServidor(tipo_medicion, medicion, contador);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): empieza");

        // Detén un escaneo previo si hubiera
        detenerBusquedaDispositivosBTLE();

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                mostrarInformacionDispositivoBTLE(resultado);
            }
        };

        ScanFilter sf = new ScanFilter.Builder()
                .setDeviceName(dispositivoBuscado) // "prueba21"
                .build();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        Log.d(ETIQUETA_LOG, "buscando por nombre: " + dispositivoBuscado);
        this.elEscanner.startScan(
                Arrays.asList(sf),   // filtro
                settings,
                this.callbackDelEscaneo
        );
    } //()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE( "EPSG-GTI-PROY-3A" );
        this.buscarEsteDispositivoBTLE( "prueba21" );

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // ()

    private void enviarMedicionAlServidor(final int tipoMedicion, final double medicion, final int numeroMedicion) {
        new Thread(() -> {
            java.net.HttpURLConnection conn = null;
            try {
                // Usa https
                String endpoint = "https://jsanmar6.upv.edu.es/src/api/insertar_medicion.php"; // enlace al archivo en web
                String token = ""; // de momento no me hace falta token, ya veré a futuro

                String urlConToken = endpoint + (token != null && !token.isEmpty()
                        ? "?token=" + java.net.URLEncoder.encode(token, "UTF-8")
                        : "");

                String body =
                        "tipo_medicion=" + java.net.URLEncoder.encode(String.valueOf(tipoMedicion), "UTF-8") +
                                "&medicion=" + java.net.URLEncoder.encode(String.valueOf(medicion), "UTF-8") +
                                "&numero_medicion=" + java.net.URLEncoder.encode(String.valueOf(numeroMedicion), "UTF-8");

                Log.d(ETIQUETA_LOG, "POST -> " + urlConToken + "   BODY: " + body);

                java.net.URL url = new java.net.URL(urlConToken);
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                java.io.InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder sb = new StringBuilder();
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                Log.d(ETIQUETA_LOG, "RESPUESTA HTTP " + code + " -> " + sb.toString());

            } catch (Exception e) {
                Log.e(ETIQUETA_LOG, "Error enviando medicion: " + e.getMessage(), e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    } //()



    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------


