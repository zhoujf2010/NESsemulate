import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.registry.impl.DefaultRegistry;
import com.pi4j.registry.impl.RuntimeRegistry;

public class test3
{

    private static final int PIN_BUTTON = 26; // PIN 18 = BCM 24

    private static int pressCount = 0;

    /**
     * This application blinks a led and counts the number the button is
     * pressed. The blink speed increases with each
     * button press, and after 5 presses the application finishes.
     *
     * @param args
     *            an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception
     *             if any.
     */
    public static void main(String[] args) throws Exception {

        Thread thread = new Thread()
        {
            public void run() {
                new test3().ScanKey();
            }
        };

        thread.start();

        while (true) {
            try {
                Thread.sleep(1);
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        //        pi4j.shutdown();
    }

    public void ScanKey() {
        Context pi4j = Pi4J.newAutoContext();

        DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j).id("button").name("Press button")
                .address(PIN_BUTTON).pull(PullResistance.PULL_UP).debounce(3000L).provider("pigpio-digital-input");
        DigitalInput button = pi4j.create(buttonConfig);

        //        while(true) {
        //            DigitalState stat = button.state();
        //            System.out.println(stat.toString());
        //
        //            Thread.sleep(100);
        //            
        //        }

        //        button.addListener(e -> {
        //            System.out.println(e.state());
        //        });


        int pinADC_CS = 21;
        int pinADC_CLK = 16;
        DigitalOutputConfigBuilder outcfg1 = DigitalOutput.newConfigBuilder(pi4j).id("ADC_CS").name("LED Flasher")
                .address(pinADC_CS).shutdown(DigitalState.LOW).initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        
        DigitalOutput ADC_CS = pi4j.create(outcfg1);
        DigitalOutputConfigBuilder outcfg2 = DigitalOutput.newConfigBuilder(pi4j).id("ADC_CLK").name("LED Flasher")
                .address(pinADC_CLK).shutdown(DigitalState.LOW).initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        DigitalOutput ADC_CLK = pi4j.create(outcfg2);
        
        DigitalState oldstate = button.state();
        while (true) {
            try {
//                if (oldstate != button.state()) {
//                    System.out.println(button.state());
//                    oldstate = button.state();
//                }
                Thread.sleep(10);
                long startTime=System.currentTimeMillis();   //获取开始时间  

                int x = getADC0832(pi4j,0,ADC_CS,ADC_CLK);
                int y = getADC0832(pi4j,1,ADC_CS,ADC_CLK);
                long endTime=System.currentTimeMillis(); //获取结束时间  

                System.out.println(x + "  " + y + "  use "+ (endTime-startTime)+"ms");
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public int getADC0832(Context pi4j, int channel, DigitalOutput ADC_CS, DigitalOutput ADC_CLK) throws InterruptedException {
        //        cs=21,clk=16,dio=20
        int pinADC_DIO = 20;


        long startTime=System.currentTimeMillis();   //获取开始时间  
        DigitalOutputConfigBuilder outcfg3 = DigitalOutput.newConfigBuilder(pi4j).id("ADC_DIO").name("LED Flasher")
                .address(pinADC_DIO).shutdown(DigitalState.LOW).initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        DigitalOutput ADC_DIO = pi4j.create(outcfg3);
        long endTime=System.currentTimeMillis(); //获取结束时间  
        System.out.println("Step1 "+ (endTime-startTime)+"ms");
        startTime = endTime;

        ADC_CS.low();
        ADC_CLK.low();

        ADC_DIO.high();
        Thread.sleep(1);
        ADC_CLK.high();
        Thread.sleep(1);
        ADC_CLK.low();

        ADC_DIO.high();
        Thread.sleep(1);
        ADC_CLK.high();
        Thread.sleep(1);
        ADC_CLK.low();

        if (channel == 0)
            ADC_DIO.low();
        else
            ADC_DIO.high();
        Thread.sleep(1);

        ADC_CLK.high();
        ADC_DIO.high();
        Thread.sleep(1);
        ADC_CLK.low();
        ADC_DIO.high();
        Thread.sleep(1);


        endTime=System.currentTimeMillis(); //获取结束时间  
        System.out.println("Step2 "+ (endTime-startTime)+"ms");
        startTime = endTime;
        
        ADC_DIO.shutdown(pi4j);
        ((DefaultRegistry)pi4j.registry()).Remove("ADC_DIO");
        
//        RuntimeRegistry rr = pi4j.registry().get("ADC_DIO");
        
        
//        ((RuntimeRegistry)pi4j.registry()).remove("ADC_DIO");
       
        DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("ADC_DIO_In")
                .name("Press button")
                .address(pinADC_DIO)
                .pull(PullResistance.PULL_UP)
                .debounce(3000L)
                .provider("pigpio-digital-input");
        DigitalInput ADC_DIO_in = pi4j.create(buttonConfig);
        
        endTime=System.currentTimeMillis(); //获取结束时间  
        System.out.println("Step3 "+ (endTime-startTime)+"ms");
        startTime = endTime;
        
        int dat1 = 0;
        for (int i = 0; i < 8; i++) {
            ADC_CLK.high();
            Thread.sleep(1);
            ADC_CLK.low();
            Thread.sleep(1);
            dat1 = dat1 << 1 | (ADC_DIO_in.state()==DigitalState.LOW?0:1);
        }

        int dat2 = 0;
        for (int i = 0; i < 8; i++) {
            dat2 = dat2 | (ADC_DIO_in.state()==DigitalState.LOW?0:1) << i;
            ADC_CLK.high();
            Thread.sleep(1);
            ADC_CLK.low();
            Thread.sleep(1);
        }

        ADC_CS.high();
        
        endTime=System.currentTimeMillis(); //获取结束时间  
        System.out.println("Step4 "+ (endTime-startTime)+"ms");
        startTime = endTime;
        
//        ADC_CS.shutdown(pi4j);
//        ((DefaultRegistry)pi4j.registry()).Remove("ADC_CS");
//
//        ADC_CLK.shutdown(pi4j);
//        ((DefaultRegistry)pi4j.registry()).Remove("ADC_CLK");
        
        ADC_DIO_in.shutdown(pi4j);
        ((DefaultRegistry)pi4j.registry()).Remove("ADC_DIO_In");
        endTime=System.currentTimeMillis(); //获取结束时间  
        
        System.out.println("Step5 "+ (endTime-startTime)+"ms");
        startTime = endTime;

        //        GPIO.setup(ADC_DIO, GPIO.OUT)

        if (dat1 == dat2)
            return dat1;
        else
            return 0;

    }
}
