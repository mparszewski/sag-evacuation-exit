package com.messages.humanactor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MakeTurn implements HumanActorMessage {
    private int numberOfRound;
}