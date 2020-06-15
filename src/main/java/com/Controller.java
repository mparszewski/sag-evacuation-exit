package com;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.infrastructure.Fire;
import com.messages.controller.ControllerMessage;
import com.messages.controller.CreateActor;
import com.messages.controller.MakeRound;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.utility.HumanConfigGenerator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Controller extends AbstractBehavior<ControllerMessage> {
    List<ActorRef<HumanActorMessage>> listOfHelloActors = newArrayList();

    public final HumanConfigGenerator humanConfigGenerator = new HumanConfigGenerator();

    public Controller(ActorContext<ControllerMessage> context) {
        super(context);
    }

    public static Behavior<ControllerMessage> create() {
        return Behaviors.setup(Controller::new);
    }

    @Override
    public Receive<ControllerMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateActor.class, this::createHumanActor)
                .onMessage(MakeRound.class, this::makeRound)
                .build();
    }

    public Controller createHumanActor(CreateActor createActor) {
        ActorRef<HumanActorMessage> humanActor = getContext().spawnAnonymous(
                HumanActor.create(humanConfigGenerator.generateHumanConfig(createActor.getStartingPoint()))
        );
        listOfHelloActors.add(humanActor);
        return this;
    }

    public Controller makeRound(MakeRound makeRound) {
        Fire.getFire().spreadRandomly();
        listOfHelloActors.forEach(actorRef -> actorRef.tell(new MakeTurn(makeRound.getNumberOfRound())));
        return this;
    }
}
