package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.messages.HelloMessage;
import com.models.HumanConfig;
import com.models.Point;

import static com.infrastructure.Building.getBuilding;

public class HumanActor extends AbstractBehavior<HelloMessage> {
    private HumanConfig config;
    private Point actualPosition;

    public HumanActor(ActorContext<HelloMessage> context, HumanConfig humanConfig) {
        super(context);
        this.config = humanConfig;
    }

    @Override
    public Receive<HelloMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(HelloMessage.class, this::printMessage)
                .build();
    }

    public static Behavior<HelloMessage> create(HumanConfig humanConfig) {
        return Behaviors.setup((ctx) -> new HumanActor(ctx, humanConfig));
    }

    public HumanActor printMessage(HelloMessage message) {
        System.out.println("Hello from actor " + config.getName());
        return this;
    }

    public boolean checkMove(Point to) {
        return getBuilding().isPointAvailable(to);
    }
}
