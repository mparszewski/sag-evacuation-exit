package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.messages.HelloMessage;

public class HelloActor extends AbstractBehavior<HelloMessage>
{
    public HelloActor(ActorContext<HelloMessage> context) {
        super(context);
    }

    @Override
    public Receive<HelloMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(HelloMessage.class, this::printMessage)
                .build();
    }

    public static Behavior<HelloMessage> create() {
        return Behaviors.setup(HelloActor::new);
    }

    public HelloActor printMessage(HelloMessage message) {
        System.out.println( "Hello from actor " );
        return this;
    }
}
