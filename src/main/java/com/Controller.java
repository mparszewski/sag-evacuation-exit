package com;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.messages.*;
import com.utility.HumanConfigGenerator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Controller extends AbstractBehavior<ControllerMessage> {
    List<ActorRef<HelloMessage>> listOfHelloActors = newArrayList();

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
                .onMessage(CallAllActors.class, this::callAllActors)
                .onMessage(MakeRound.class, this::makeRound)
                .build();
    }

    public Controller createHumanActor(CreateActor createActor) {
        ActorRef<HelloMessage> humanActor = getContext().spawnAnonymous(
                HumanActor.create(humanConfigGenerator.generateHumanConfig(createActor.getStartingPoint()))
        );
        listOfHelloActors.add(humanActor);
        return this;
    }

    public Controller makeRound(MakeRound makeRound) {
        // TODO: Implement turn, get numberOfRound from message and pass to makeTurn
        return this;
    }

    public Controller callAllActors(CallAllActors callAllActors) {
        listOfHelloActors.forEach(actor -> actor.tell(new HelloMessage()));
        return this;
    }
}
