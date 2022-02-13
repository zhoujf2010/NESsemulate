import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;

public class test3
{

    private static final int PIN_BUTTON = 26; // PIN 18 = BCM 24

    private static int pressCount = 0;

    /**
     * This application blinks a led and counts the number the button is pressed. The blink speed increases with each
     * button press, and after 5 presses the application finishes.
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {
       
        Context pi4j = Pi4J.newAutoContext();

        DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("Press button")
                .address(PIN_BUTTON)
                .pull(PullResistance.PULL_UP)
                .debounce(3000L)
                .provider("pigpio-digital-input");
        DigitalInput button = pi4j.create(buttonConfig);
        
        while(true) {
            DigitalState stat = button.state();
            System.out.println(stat.toString());

            Thread.sleep(100);
            
        }
        
//        pi4j.shutdown();
    }
}
