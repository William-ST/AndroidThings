package org.example.androidthings;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int INTERVALO_LED = 1000; // Intervalo parpadeo (ms)
    private static final String LED_PIN = "BCM6"; // Puerto GPIO del LED
    private Handler handler = new Handler(); // Handler para el parpadeo
    private Gpio ledGpio;

    private Gpio botonGpio;
    private boolean interruptFlicker;

    private static final String BOTON_PIN = "BCM21"; // Puerto GPIO del botón

    private static final int PORCENTAGE_LED_PWM = 25; // % encendido
    private static final String LED_PWM_PIN = "PWM0"; // Puerto del LED
    private Pwm ledPwm;

    private static final int INTERVALO_BRIGHTNESS = 1000; // Intervalo parpadeo (ms)
    private final int[] PORCENTAGE_INTERVAL = new int[]{0, 20, 40, 60, 80, 100};

    private static final String LED_PIN_13 = "BCM13", LED_PIN_19 = "BCM19", LED_PIN_26 = "BCM26";
    private Gpio ledGpio13, ledGpio19, ledGpio26;
    private static final int INTERVALO_LED_RGB = 1000;
    private int countRGB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeripheralManager perifericos = PeripheralManager.getInstance();

        ((TextView) findViewById(R.id.tv_message)).setText(perifericos.getGpioList().toString());
        Log.d(TAG, "GPIO: " + perifericos.getGpioList());

        PeripheralManager manager = PeripheralManager.getInstance();
        /*
        try {
            botonGpio = manager.openGpio(BOTON_PIN); // 1. Crea conecxión GPIO
            botonGpio.setDirection(Gpio.DIRECTION_IN);// 2. Es entrada
            botonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);// 3. Habilita eventos de disparo por flanco de bajada
            botonGpio.registerGpioCallback(callback); // 4. Registra callback
        } catch (IOException e) {
            Log.e(TAG, "Error en PeripheralIO API", e);
        }


        try {
            ledGpio = manager.openGpio(LED_PIN); // 1. Crea conexión GPIO
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // 2. Se indica que es de salida
            handler.post(runnable); // 3. Llamamos al handler
        } catch (IOException e) {
            Log.e(TAG, "Error en PeripheralIO API", e);
        }

        try {
            ledPwm = manager.openPwm(LED_PWM_PIN); // 1. Crea conexión GPIO
            handler.post(runnable); // 3. Llamamos al handler
        } catch (IOException e) {
            Log.e(TAG, "Error en al acceder a salida PWM", e);
        }

        */
        try {
            ledGpio13 = manager.openGpio(LED_PIN_13); // 1. Crea conexión GPIO
            ledGpio13.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // 2. Se indica que es de salida
            ledGpio19 = manager.openGpio(LED_PIN_19); // 1. Crea conexión GPIO
            ledGpio19 .setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // 2. Se indica que es de salida
            ledGpio26 = manager.openGpio(LED_PIN_26); // 1. Crea conexión GPIO
            ledGpio26.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW); // 2. Se indica que es de salida
            initializeRGBvalues();
            handler.post(ledRgbRunnable); // 3. Llamamos al handler
        } catch (IOException e) {
            Log.e(TAG, "Error en PeripheralIO API", e);
        }
    }

    /*
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!interruptFlicker) {
                    ledGpio.setValue(!ledGpio.getValue()); // 4. Cambiamos valor LED
                }
                handler.postDelayed(runnable, INTERVALO_LED); // 5. Programamos siguiente llamada dentro de INTERVALO_LED ms
            } catch (IOException e) {
                Log.e(TAG, "Error en PeripheralIO API", e);
            }
        }
    };

    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                interruptFlicker = !gpio.getValue();
                Log.e(TAG, "cambio botón " + Boolean.toString(gpio.getValue()));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            return true; // 5. devolvemos true para mantener callback activo
        }
    };
    */

    private int currentBrightness = 0;

    private void changeBrightness() {
        try {
            ledPwm.setPwmFrequencyHz(120);
            ledPwm.setPwmDutyCycle(PORCENTAGE_INTERVAL[currentBrightness]);
            currentBrightness++;
            if (currentBrightness == PORCENTAGE_INTERVAL.length) {
                currentBrightness = 0;
            }
            ledPwm.setEnabled(true);
        } catch (
                IOException e) {
            Log.e(TAG, "Error en al acceder a salida PWM", e);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            changeBrightness();
            handler.postDelayed(runnable, INTERVALO_BRIGHTNESS);
        }
    };

    private boolean[][] valuesRGB = new boolean[8][3];

    private void initializeRGBvalues() {
        valuesRGB[0][0] = false;
        valuesRGB[0][1] = false;
        valuesRGB[0][2] = false;

        valuesRGB[1][0] = true;
        valuesRGB[1][1] = false;
        valuesRGB[1][2] = false;

        valuesRGB[2][0] = false;
        valuesRGB[2][1] = true;
        valuesRGB[2][2] = false;

        valuesRGB[3][0] = false;
        valuesRGB[3][1] = false;
        valuesRGB[3][2] = true;

        valuesRGB[4][0] = true;
        valuesRGB[4][1] = true;
        valuesRGB[4][2] = false;

        valuesRGB[5][0] = false;
        valuesRGB[5][1] = true;
        valuesRGB[5][2] = true;

        valuesRGB[6][0] = true;
        valuesRGB[6][1] = false;
        valuesRGB[6][2] = true;

        valuesRGB[7][0] = true;
        valuesRGB[7][1] = true;
        valuesRGB[7][2] = true;
    }

    private Runnable ledRgbRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                ledGpio13.setValue(valuesRGB[countRGB][0]);
                ledGpio19.setValue(valuesRGB[countRGB][1]);
                ledGpio26.setValue(valuesRGB[countRGB][2]);

                countRGB++;
                if (countRGB == 8) {
                    countRGB = 0;
                }
                handler.postDelayed(ledRgbRunnable, INTERVALO_LED_RGB);
            } catch (IOException e) {
                Log.e(TAG, "Error en PeripheralIO API", e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        if (botonGpio != null) { // 6. Cerramos recursos
            botonGpio.unregisterGpioCallback(callback);
            try {
                botonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar botonGpio.", e);
            }
        }
        */
        if (ledPwm != null) {// 3. Cerramos recursos
            try {
                ledPwm.close();
                ledPwm = null;
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar PWM", e);
            }
        }
    }
}
