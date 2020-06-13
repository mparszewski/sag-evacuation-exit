package com.utility;

import com.enums.Mobility;
import com.models.HumanConfig;
import com.models.Point;

public class HumanConfigGenerator {
    private int i = 0;
    public HumanConfig generateHumanConfig(Point startingPoint) {
        return HumanConfig.builder()
                .name(generateName())
                .mobility(Mobility.NORMAL)
                .health(10)
                .startingPoint(startingPoint)
                .speed(getRandomValue(3, 6))
                .reactionTime(6)
                .build();
    }

    private String generateName() {
        i++;
        return "human-" + i;
    }

    private static int getRandomValue(int min, int max){
        return (int)(Math.random() * max + min);
    }

}
