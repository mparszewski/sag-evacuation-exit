package com.messages;

import com.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateActor implements ControllerMessage {
    private Point startingPoint;
}
