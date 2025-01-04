package com.starshipGame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Juego extends View {

    private static final float RADIO_JUGADOR = 65;
    private static final int VELOCIDAD_MOVIMIENTO_SUAVE = 50;

    public static int ancho;
    public static int alto;
    public int posY;
    private RectF rectNaveJugador;
    private Bitmap bitmapNaveJugador;
    private Bitmap bitmapNaveEnemiga;
    private MediaPlayer musicaFondo;
    private MediaPlayer mediaPlayerDisparo;
    private final Paint puntos = new Paint();
    private final Handler handler = new Handler();
    private final List<NaveEnemiga> navesEnemigas = new ArrayList<>();
    private final List<Disparo> disparos = new ArrayList<>();
    private String nombreJugador = "Jugador";
    private String dificultad = "Normal";
    private boolean juegoEnPausa = true;
    private float multiplicadorVelocidad = 1.0f; // Multiplicador inicial
    private long navesEnemigasDelay = 2000;
    private Integer puntuacion = 0;
    private final Random random = new Random();
    private AlertDialog dialogGameOver;
    private MediaPlayer sonidoColision; // MediaPlayer para el sonido de colisión

    public Juego(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus(); // Solicita el foco para que pueda recibir eventos del teclado
        init();
    }

    public Juego(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus(); // Solicita el foco para que pueda recibir eventos del teclado
        init();
    }

    public Juego(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus(); // Solicita el foco para que pueda recibir eventos del teclado
        init();
    }
    private Bitmap bitmapFondo;


    private void init() {
        // Escalar la imagen de la nave del jugador
        Bitmap originalJugador = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.player);
        bitmapNaveJugador = Bitmap.createScaledBitmap(originalJugador, 150, 150, false);

        // Escalar la imagen de la nave enemiga
        Bitmap originalEnemigo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.enemy);
        bitmapNaveEnemiga = Bitmap.createScaledBitmap(originalEnemigo, 100, 100, false);

        // Cargar el recurso del fondo (sin escalar aún)
        Bitmap originalFondo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.fondoestrellas);
        bitmapFondo = originalFondo;

        if (juegoEnPausa) {
            mostrarDialogoNombreYDificultad();
        } else {
            iniciarJuego();
        }
    }

    private void mostrarDialogoNombreYDificultad() {
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_nombre_dificultad, null);
        final EditText input = viewInflated.findViewById(R.id.editTextNombre);
        final Spinner dificultadSpinner = viewInflated.findViewById(R.id.spinnerDificultad);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.opciones_dificultad,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dificultadSpinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Ingrese su nombre y seleccione la dificultad");
        builder.setView(viewInflated);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            nombreJugador = input.getText().toString();
            dificultad = dificultadSpinner.getSelectedItem().toString();

            if (nombreJugador == null || nombreJugador.trim().isEmpty()) {
                nombreJugador = "Jugador";
            }
            Toast.makeText(getContext(), "¡Bienvenido, " + nombreJugador + "!", Toast.LENGTH_SHORT).show();

            juegoEnPausa = false;
            iniciarJuego();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> ((Activity) getContext()).finish());

        builder.setCancelable(false);
        builder.show();
    }

    private void iniciarJuego() {
        puntos.setTextAlign(Paint.Align.RIGHT);
        puntos.setTextSize(100);
        puntos.setColor(Color.WHITE);

        musicaFondo = MediaPlayer.create(getContext(), R.raw.music);
        musicaFondo.setLooping(true);
        musicaFondo.start();

        mediaPlayerDisparo = MediaPlayer.create(getContext(), R.raw.laser1);

        // Ajustar el número inicial de naves enemigas según la dificultad
        int cantidadNavesIniciales = obtenerCantidadNavesPorDificultad(dificultad);
        for (int i = 0; i < cantidadNavesIniciales; i++) {
            generarNaveEnemiga();
        }

        // Timer para generar naves enemigas
        Timer timerNavesEnemigas = new Timer();
        timerNavesEnemigas.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (!juegoEnPausa) {
                        generarNaveEnemiga();
                    }
                });
            }
        }, 0, navesEnemigasDelay);

        // **Timer para aumentar la velocidad de las naves enemigas en un hilo separado**
        Timer timerIncrementoVelocidad = new Timer();
        timerIncrementoVelocidad.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (!juegoEnPausa) {
                        multiplicadorVelocidad *= 1.5; // Aumenta el multiplicador

                        for (NaveEnemiga nave : navesEnemigas) {
                            float nuevaVelocidad = nave.getVelocidadBase() * multiplicadorVelocidad;

                            // Aplica el límite de velocidad máxima (6 veces la velocidad base)
                            if (nuevaVelocidad > nave.getVelocidadBase() * 6) {
                                nuevaVelocidad = nave.getVelocidadBase() * 6;
                            }

                            nave.aplicarMultiplicadorVelocidad(nuevaVelocidad / nave.getVelocidadBase());
                        }
                    }
                });
            }
        }, 7000, 7000); // Incrementa la velocidad cada 7 segundos
    }

    // Método para obtener la cantidad de naves iniciales según la dificultad
    private int obtenerCantidadNavesPorDificultad(String dificultad) {
        switch (dificultad) {
            case "Fácil":
                return 3; // Menos naves para Fácil
            case "Medio":
                return 5; // Número moderado de naves para Medio
            case "Difícil":
                return 8; // Más naves para Difícil
            default:
                return 5; // Valor por defecto
        }
    }



    private void dibujarNaveJugador(Canvas canvas) {
        float left = ancho - 200 - bitmapNaveJugador.getWidth();
        float top = posY - bitmapNaveJugador.getHeight() / 2;
        float right = ancho - 450;
        float bottom = posY + bitmapNaveJugador.getHeight() / 2;

        rectNaveJugador = new RectF(left, top, right, bottom);

        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        matrix.postTranslate(left, top);

        canvas.drawBitmap(bitmapNaveJugador, matrix, null);
    }

    private void dibujarNavesEnemigas(Canvas canvas) {
        for (NaveEnemiga nave : navesEnemigas) {
            RectF rectNaveEnemiga = nave.getRect();

            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            matrix.postTranslate(rectNaveEnemiga.left, rectNaveEnemiga.top);

            canvas.drawBitmap(bitmapNaveEnemiga, matrix, null);
        }
    }

    private void generarNaveEnemiga() {
        int margenSuperior = 200; // Margen desde el borde superior
        int margenInferior = 200; // Margen desde el borde inferior
        // Generar la posición Y de la nave dentro del área permitida
        int enemigoY = random.nextInt(alto - margenSuperior - margenInferior - bitmapNaveEnemiga.getHeight()) + margenSuperior;

        // Crear una nueva nave enemiga con la posición Y calculada
        NaveEnemiga nuevaNave = new NaveEnemiga(-bitmapNaveEnemiga.getWidth(), enemigoY, dificultad);

        // Añadir la nave enemiga a la lista
        navesEnemigas.add(nuevaNave);
    }


    private void moverDisparos() {
        if (juegoEnPausa) return; // No mover disparos si el juego está en pausa.

        Iterator<Disparo> iterator = disparos.iterator();
        while (iterator.hasNext()) {
            Disparo disparo = iterator.next();
            disparo.mover();

            if (disparo.getX() < 0) {
                iterator.remove();
            }
        }
    }

    private void moverNavesEnemigas() {
        if (juegoEnPausa) return; // No mover naves si el juego está en pausa.

        Iterator<NaveEnemiga> iterator = navesEnemigas.iterator();
        while (iterator.hasNext()) {
            NaveEnemiga nave = iterator.next();
            nave.mover();

            // Elimina la nave enemiga si sale completamente por el lado derecho
            if (nave.getX() > ancho) {
                iterator.remove();
            }
        }
    }

    private void detectarColision() {
        if (juegoEnPausa) return; // Si el juego está en pausa, no detectar colisiones.

        List<NaveEnemiga> navesEliminadas = new ArrayList<>();

        for (NaveEnemiga nave : navesEnemigas) {
            // Detectar colisión con la nave del jugador
            if (rectNaveJugador != null && RectF.intersects(rectNaveJugador, nave.getRect())) {
                juegoEnPausa = true;
                handler.post(this::mostrarGameOver);
                return;
            }

            // Detectar colisión con disparos
            Iterator<Disparo> disparosIterator = disparos.iterator();
            while (disparosIterator.hasNext()) {
                Disparo disparo = disparosIterator.next();
                if (RectF.intersects(disparo.getRect(), nave.getRect())) {
                    puntuacion += 1;
                    disparosIterator.remove();
                    navesEliminadas.add(nave);
                }
            }
        }

        // Eliminar naves colisionadas
        navesEnemigas.removeAll(navesEliminadas);
    }




    private void mostrarGameOver() {
        detenerMusicaFondo();
        // Reproducir el sonido de colisión
        if (sonidoColision != null) {
            sonidoColision.start();
        }

        ((Activity) getContext()).runOnUiThread(() -> {
            if (dialogGameOver != null && dialogGameOver.isShowing()) {
                return;
            }

            Toast.makeText(getContext(), "Game Over - Puntuación: " + puntuacion, Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("GAME OVER");
            builder.setMessage("¿Desea reiniciar el juego?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                dialog.dismiss();
                reiniciarJuego();
            });

            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
                ((Activity) getContext()).finish();
            });

            dialogGameOver = builder.create();
            dialogGameOver.show();
        });

        juegoEnPausa = true;
    }


    private void reiniciarJuego() {
        detenerMusicaFondo();
        juegoEnPausa = true;
        navesEnemigasDelay = 2000;
        puntuacion = 0;
        navesEnemigas.clear();
        disparos.clear();

        mostrarDialogoNombreYDificultad();
    }

    private void detenerMusicaFondo() {
        if (musicaFondo != null) {
            musicaFondo.pause();
            musicaFondo.release();
            musicaFondo = null;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (juegoEnPausa) {
            return true; // No permite realizar acciones si el juego está en pausa
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: // Flecha arriba
                actualizarPosicionNaveSuavemente(posY - 50);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN: // Flecha abajo
                actualizarPosicionNaveSuavemente(posY + 50);
                break;
            case KeyEvent.KEYCODE_ENTER: // Enter para disparar
                disparar();
                break;
            default:
                return super.onKeyDown(keyCode, event); // Mantiene el comportamiento estándar para otras teclas
        }
        return true;
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (bitmapFondo != null) {
            if (bitmapFondo.getWidth() != ancho || bitmapFondo.getHeight() != alto) {
                bitmapFondo = Bitmap.createScaledBitmap(bitmapFondo, ancho, alto, false);
            }
            canvas.drawBitmap(bitmapFondo, 0, 0, null);
        }

        dibujarNaveJugador(canvas);
        dibujarNavesEnemigas(canvas);

        Paint disparoPaint = new Paint();
        disparoPaint.setColor(Color.GREEN);
        for (Disparo disparo : disparos) {
            canvas.drawRect(disparo.getX(), disparo.getY(), disparo.getX() + 50, disparo.getY() + 5, disparoPaint);
        }

        canvas.drawText(puntuacion.toString(), ancho - 50, 100, puntos);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (juegoEnPausa) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actualizarPosicionNaveSuavemente((int) event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                actualizarPosicionNaveSuavemente((int) event.getY());
                break;
            case MotionEvent.ACTION_UP:
                disparar();
                break;
        }
        return true;
    }

    private void actualizarPosicionNaveSuavemente(int nuevaPosY) {
        if (nuevaPosY - bitmapNaveJugador.getHeight() / 2 < 0) {
            posY = bitmapNaveJugador.getHeight() / 2; // Limita el movimiento en la parte superior
        } else if (nuevaPosY + bitmapNaveJugador.getHeight() / 2 > alto) {
            posY = alto - bitmapNaveJugador.getHeight() / 2; // Limita el movimiento en la parte inferior
        } else {
            posY = nuevaPosY;
        }
        invalidate();
    }


    private void disparar() {
        if (mediaPlayerDisparo != null) {
            mediaPlayerDisparo.start();
        }

        float disparoX = ancho - 450 - 20; // Ajustado para salir del morro
        float disparoY = posY - 5; // Centrado en el morro
        Disparo disparo = new Disparo(disparoX, disparoY);
        disparos.add(disparo);
    }

    public void actualizarJuego() {
        moverDisparos();
        moverNavesEnemigas();
        detectarColision();
        invalidate();
    }
}
