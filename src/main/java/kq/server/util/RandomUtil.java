package kq.server.util;

import java.util.Random;

public class RandomUtil extends Thread{
    private static Random random = new Random();

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(5 * 60 * 1000);
                random = new Random(System.currentTimeMillis());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static int getNextInt(int range){
        return random.nextInt(range);
    }

    public static Random getRandom(){
        return random;
    }
}
