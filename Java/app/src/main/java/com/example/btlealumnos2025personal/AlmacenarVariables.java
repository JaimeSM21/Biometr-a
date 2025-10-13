package com.example.btlealumnos2025personal;

public class AlmacenarVariables {

    // Variables donde se guardarán los datos decodificados
    private int medicion_id;
    private int contador;
    private int medicion;

    // Metodo que recibe major y minor y los separa en partes
    public void actualizarDesdeBeacon(int major, int minor) {

        // Los 8 bits altos del major contienen el tipo de medición
        medicion_id = (major >> 8) & 0xFF;

        // Los 8 bits bajos del major contienen el contador
        contador = major & 0xFF;

        // El minor es el valor de la medición (int16 con signo)
        medicion = (short) (minor & 0xFFFF);
    }

    // Getters para acceder a los valores
    public int getMedicionId() {
        return medicion_id;
    }

    public int getContador() {
        return contador;
    }

    public int getMedicion() {
        return medicion;
    }
}

