package com.messages.controller;

import com.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwapInDoors implements ControllerMessage {
    private Point secondActorPosition;
}
