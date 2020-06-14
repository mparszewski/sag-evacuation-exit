package com.models;

import com.enums.Mobility;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HumanConfig {
    private String name;
    private int health;
    private Mobility mobility;
    private int speed; // ranging from 0 to 7 m/s
    private int vision;
    private int reactionTime; // in seconds
    private int collaboration;
    private int insistence; // higher value means higher chance to change current strategy
    private int knowledge;
    private int nervousness;
    private Point startingPoint;
}
