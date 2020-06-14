package com;

import akka.actor.typed.ActorSystem;
import com.messages.controller.CallAllActors;
import com.messages.controller.ControllerMessage;
import com.messages.controller.CreateActor;
import com.messages.controller.MakeRound;

import static com.infrastructure.Building.getBuilding;

public class Main {
    public static void main(String[] args) {
        final int NUMBER_OF_ROUNDS = 10;

        final ActorSystem<ControllerMessage> actorSystem = ActorSystem.create(Controller.create(), "Evacution_Simulator");

        getBuilding().getAgents().forEach(agentStartingPoint -> actorSystem.tell(new CreateActor(agentStartingPoint)));

        actorSystem.tell(new CallAllActors()); //TODO: to be removed - just a test for actors existance

        for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
            actorSystem.tell(new MakeRound(i + 1));

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        actorSystem.terminate();
    }

}
