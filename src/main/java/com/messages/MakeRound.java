package com.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MakeRound implements ControllerMessage {
    private int numberOfRound;
}
