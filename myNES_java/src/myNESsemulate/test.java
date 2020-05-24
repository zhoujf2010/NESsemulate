package myNESsemulate;

public class test
{

    public static void main(String[] args) {

        long s1 = System.currentTimeMillis();

        long sum = 0;
       //for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 2000000000; i++) {
                sum += i;
            }
       // }

        long s2 = System.currentTimeMillis();

        System.out.println((s2 - s1) / 1000.0 + "s");

    }

}
