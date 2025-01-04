package com.starshipGame;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import com.starshipGame.R;

public class MainActivity extends AppCompatActivity {

    private Juego juego;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta la barra de acción (ActionBar)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Oculta la barra de estado (barra superior)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Oculta la barra de navegación (barra de botones)
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);

        // Obtiene la referencia al componente de juego en el layout
        juego = findViewById(R.id.Pantalla);

        // Calcula el ancho y alto una vez que se ha pintado el layout
        ViewTreeObserver obs = juego.getViewTreeObserver();
        obs.addOnGlobalLayoutListener(() -> {
            Juego.ancho = juego.getWidth();
            Juego.alto = juego.getHeight();
            juego.posY = Juego.alto / 2;  // Centra la nave del jugador verticalmente
        });

        // Ejecuta la actualización del juego cada 30 milisegundos
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> juego.actualizarJuego());
            }
        }, 0, 30);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Envía los eventos de teclado al juego
        if (juego != null && juego.onKeyDown(event.getKeyCode(), event)) {
            return true; // Manejado por el juego
        }
        return super.dispatchKeyEvent(event);
    }
}
