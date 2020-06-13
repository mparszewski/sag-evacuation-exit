package com;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.messages.CallAllActors;
import com.messages.ControllerMessage;
import com.messages.CreateActor;
import com.messages.HelloMessage;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Controller extends AbstractBehavior<ControllerMessage> {
    List<ActorRef<HelloMessage>> listOfHelloActors = newArrayList();

    public Controller(ActorContext<ControllerMessage> context) {
        super(context);
    }

    public static Behavior<ControllerMessage> create() {
        return Behaviors.setup(Controller::new);
    }

    @Override
    public Receive<ControllerMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateActor.class, this::createHelloActor)
                .onMessage(CallAllActors.class, this::callAllActors)
                .build();
    }

    public Controller createHelloActor(CreateActor createActor) {
        ActorRef<HelloMessage> newHelloActor = getContext().spawnAnonymous(
                HelloActor.create()
        );
        listOfHelloActors.add(newHelloActor);
        return this;
    }

    public Controller callAllActors(CallAllActors callAllActors) {
        listOfHelloActors.forEach(actor -> actor.tell(new HelloMessage()));
        return this;
    }
}
