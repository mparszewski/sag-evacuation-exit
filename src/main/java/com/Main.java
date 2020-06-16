package com;

import akka.actor.typed.ActorSystem;
import com.messages.controller.ControllerMessage;
import com.messages.controller.CreateActor;
import com.messages.controller.MakeRound;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

import java.io.IOException;

import static com.HumanActor.DELIMITER;
import static com.infrastructure.Building.getBuilding;
import static com.infrastructure.Fire.getFire;

public class Main {
    public static void main(String[] args) {

        PropertyConfigurator.configure(new File("src/main/resources/log4j.properties").getAbsolutePath());
        Logger logger = Logger.getLogger(Main.class);

        final int NUMBER_OF_ROUNDS = 40;

        final ActorSystem<ControllerMessage> actorSystem = ActorSystem.create(Controller.create(), "Evacution_Simulator");

        int initialAgentsPool = getBuilding().getAgents().size();
        getBuilding().getAgents().forEach(agentStartingPoint -> actorSystem.tell(new CreateActor(agentStartingPoint)));

        logger.info("START");

        for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
            actorSystem.tell(new MakeRound(i + 1));

            logger.info("ROUND" + DELIMITER + (i+1) + "\n" + getFire().getStringRepresentation());
            try {
                Thread.sleep(250);
            } catch (Exception e) {
            }
        }

        actorSystem.terminate();
        logger.info("FINISH");

        LogParser logParser = new LogParser();
        try {
            logParser.parse(initialAgentsPool, new File("src/main/resources/visualisation.csv").getAbsolutePath());
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        }
    }

}
