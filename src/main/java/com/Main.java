package com;

import akka.actor.typed.ActorSystem;
import com.messages.CallAllActors;
import com.messages.ControllerMessage;
import com.messages.CreateActor;
import com.models.Point;

public class Main {
    public static void main(String[] args) {
        final int NUMBER_OF_ACTORS = 2;
        final ActorSystem<ControllerMessage> actorSystem = ActorSystem.create(Controller.create(), "Evacution_Simulator");

        for (int i = 0; i < NUMBER_OF_ACTORS; i++) {
            actorSystem.tell(new CreateActor(new Point(0,0)));
        }

        actorSystem.tell(new CallAllActors());

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }


        actorSystem.terminate();
    }

}
