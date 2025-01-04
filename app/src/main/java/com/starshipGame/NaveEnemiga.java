package com.starshipGame;

import android.graphics.RectF;

import java.util.Random;

// Clase para representar las naves enemigas
public class NaveEnemiga {
    private float velocidadBase; // Velocidad inicial sin el multiplicador
    private float velocidadActual; // Velocidad real considerando el multiplicador

    private final Random random = new Random();
    private float posX, posY;
    private static final float RADIO_NAVE = 65; // Ajusta el tamaño del radio según sea necesario

    // Constructor que inicializa la posición y velocidad de la nave enemiga
    public NaveEnemiga(int ancho, int alto, String dificultad) {
        posY = random.nextInt(alto); // Genera una posición vertical aleatoria
        posX = -RADIO_NAVE; // Inicia desde fuera del lado izquierdo de la pantalla
        ajustarVelocidad(dificultad); // Ajusta la velocidad base según la dificultad
        velocidadActual = velocidadBase; // Inicialmente, velocidad actual igual a la base
    }

    // Método para ajustar la velocidad base de las naves en función de la dificultad
    public void ajustarVelocidad(String dificultad) {
        switch (dificultad) {
            case "Fácil":
                velocidadBase = 2f; // Velocidad más baja para Fácil
                break;
            case "Difícil":
                velocidadBase = 4f; // Velocidad más alta para Difícil
                break;
            default:
                velocidadBase = 3f; // Por defecto, velocidad normal
                break;
        }
        velocidadActual = velocidadBase; // Inicializa la velocidad actual
    }

    // Método para actualizar la velocidad actual aplicando un multiplicador
    public void aplicarMultiplicadorVelocidad(float multiplicador) {
        velocidadActual = velocidadBase * multiplicador; // Calcula la nueva velocidad
    }

    // Método para mover la nave enemiga
    public void mover() {
        posX += velocidadActual; // Cambia el movimiento hacia la derecha

        // La nave vuelve a aparecer en el lado izquierdo cuando se sale del lado derecho
        if (posX - RADIO_NAVE > Juego.ancho) {
            posY = random.nextInt(Juego.alto); // Genera una nueva posición vertical aleatoria
            posX = -RADIO_NAVE; // Reinicia la posición en el lado izquierdo
        }
    }

    // Método para obtener el rectángulo asociado a la nave enemiga
    public RectF getRect() {
        return new RectF((posX - RADIO_NAVE), (posY - RADIO_NAVE), (posX + RADIO_NAVE), (posY + RADIO_NAVE));
    }

    // Método para obtener la posición X actual de la nave enemiga
    public int getX() {
        return (int) posX;
    }

    // Método para obtener la velocidad base
    public float getVelocidadBase() {
        return velocidadBase;
    }

    // Método para establecer una nueva velocidad base
    public void setVelocidadBase(float nuevaVelocidadBase) {
        velocidadBase = nuevaVelocidadBase;
        velocidadActual = velocidadBase; // Actualiza la velocidad actual también
    }
}
