# 🚀 Starship Game

Starship Game es un emocionante juego arcade en 2D desarrollado para Android. El jugador controla una nave que debe evitar colisiones con naves enemigas y destruirlas con disparos láser. La velocidad y la dificultad aumentan a medida que pasa el tiempo.

---

## 🎮 Funcionalidades

- **Control del jugador:** Mueve la nave con el teclado o pantalla táctil.
- **Disparos láser:** Dispara con precisión para destruir las naves enemigas.
- **Incremento de dificultad:** Las naves enemigas aumentan su velocidad cada cierto tiempo.
- **Dificultad ajustable:** Selección de niveles (`Fácil`, `Normal`, `Difícil`) que determinan el número de naves enemigas y su velocidad inicial.
- **Detección de colisiones:** 
  - 🛑 Colisiones entre la nave del jugador y las naves enemigas terminan el juego.
  - 🎯 Impactos de los disparos en las naves enemigas suman puntos y eliminan las naves.
- **Estilo visual:** Fondo dinámico de estrellas en movimiento para una experiencia envolvente.
- **Sonido:** Efectos de disparos, colisiones y música de fondo.

---

## 🛠️ Tecnologías Usadas

- **Lenguaje de programación:** Java ☕
- **IDE:** Android Studio 🛠️
- **Framework gráfico:** Canvas API 🎨
- **Herramientas multimedia:** 
  - Reproducción de audio con `MediaPlayer` 🎵
  - Gestión de imágenes con `BitmapFactory` 🖼️

---

## 📂 Estructura del Proyecto

- **`MainActivity.java`**: Configura la actividad principal y ejecuta el juego.
- **`Juego.java`**: Controla la lógica del juego, incluyendo colisiones, generación de enemigos y manejo de puntuación.
- **`Disparo.java`**: Representa los disparos del jugador, con posición y velocidad.
- **`NaveEnemiga.java`**: Define el comportamiento de las naves enemigas, incluida su velocidad y movimiento.

---

## 🧠 Detalles Técnicos

### ✨ Colisiones
- Implementadas usando `RectF.intersects()` para detectar si:
  - Un disparo impacta en una nave enemiga.
  - Una nave enemiga colisiona con la nave del jugador.

### 🚀 Incremento de Velocidad
- Las naves enemigas aumentan su velocidad cada 7 segundos.
- Se aplica un límite máximo para mantener el juego desafiante pero manejable.

### 🎮 Niveles de Dificultad
- `Fácil`: Menos naves enemigas y velocidad inicial más baja.
- `Normal`: Configuración estándar de enemigos y velocidad.
- `Difícil`: Más enemigos y mayor velocidad inicial.

---

## 🎨 Diseño y Animaciones

- **Estrellas de fondo:** Simulan un desplazamiento espacial.
- **Naves y disparos:** Imágenes escaladas y rotadas con `Bitmap` y `Matrix`.

---

## 📝 Instalación y Ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/tu_usuario/starship-game.git

![image](https://github.com/user-attachments/assets/78e0eb53-7ad9-4eb4-956c-00a34430771b)
