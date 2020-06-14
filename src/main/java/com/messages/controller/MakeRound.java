package com.messages.controller;

import com.messages.controller.ControllerMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MakeRound implements ControllerMessage {
    private int numberOfRound;
}
