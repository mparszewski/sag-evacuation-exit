package com;

import akka.actor.typed.ActorSystem;
import com.messages.controller.CallAllActors;
import com.messages.controller.ControllerMessage;
import com.messages.controller.CreateActor;
import com.messages.controller.MakeRound;

import static com.infrastructure.Building.getBuilding;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {

        PropertyConfigurator.configure(new File("src/main/resources/log4j.properties").getAbsolutePath());
        Logger logger = Logger.getLogger(Main.class);

        final int NUMBER_OF_ROUNDS = 10;

        logger.info("START");
        final ActorSystem<ControllerMessage> actorSystem = ActorSystem.create(Controller.create(), "Evacution_Simulator");

        getBuilding().getAgents().forEach(agentStartingPoint -> actorSystem.tell(new CreateActor(agentStartingPoint)));

        actorSystem.tell(new CallAllActors()); //TODO: to be removed - just a test for actors existance

        for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
            logger.info("START of round " + (i + 1));
            actorSystem.tell(new MakeRound(i + 1));

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        actorSystem.terminate();
        logger.info("FINISH");
    }

}
