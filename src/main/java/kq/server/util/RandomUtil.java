package kq.server.util;

import org.apache.log4j.Logger;

import java.util.Random;

public class RandomUtil extends Thread{

    private static final Logger logger = Logger.getLogger(RandomUtil.class);
    private static Random random = new Random();

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(5 * 60 * 1000);
                random = new Random(System.currentTimeMillis());
                logger.info("Random flash");
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static int getNextInt(int range){
        int ran = random.nextInt(range);
        logger.info(String.format("RandomUtil.getNextInt %d", ran));
        return ran;
    }

    public static Random getRandom(){
        return random;
    }
}
