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
                .speed(RandomUtil.getRandomValue(3, 6))
                .reactionTime(6)
                .build();
    }

    private String generateName() {
        i++;
        return "human-" + i;
    }


}
