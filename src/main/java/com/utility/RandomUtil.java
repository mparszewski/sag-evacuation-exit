package com.utility;

public class RandomUtil {
    public static boolean randomCheck(int statValue) {
        return statValue > getRandomValue(1, 10);
    }

    public static int getRandomValue(int min, int max){
        return (int)(Math.random() * max + min);
    }

}
