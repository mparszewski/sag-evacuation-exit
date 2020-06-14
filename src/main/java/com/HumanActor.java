package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.messages.humanactor.HelloMessage;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;

import static com.infrastructure.Building.getBuilding;

public class HumanActor extends AbstractBehavior<HumanActorMessage> {
    private HumanConfig config;
    private Point actualPosition;

    public static Behavior<HumanActorMessage> create(HumanConfig humanConfig) {
        return Behaviors.setup((ctx) -> new HumanActor(ctx, humanConfig));
    }

    public HumanActor(ActorContext<HumanActorMessage> context, HumanConfig humanConfig) {
        super(context);
        this.config = humanConfig;
    }

    @Override
    public Receive<HumanActorMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(HelloMessage.class, this::helloMessage)
                .onMessage(MakeTurn.class, this::makeTurn)
                .build();
    }

    public HumanActor helloMessage(HelloMessage message) {
        System.out.println("Hello from actor " + config.getName());
        return this;
    }

    public HumanActor makeTurn(MakeTurn makeTurn) {
        // TODO: implement human actor turn making
        return this;
    }

    public boolean checkMove(Point to) {
        return getBuilding().isPointAvailable(to);
    }
}
