package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.enums.Mobility;
import com.infrastructure.Door;
import com.messages.humanactor.HelloMessage;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;

import java.util.List;

import static com.enums.Mobility.PANIC;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Building.getBuilding;
import static com.utility.RandomUtil.randomCheck;
import static java.util.Objects.isNull;

public class HumanActor extends AbstractBehavior<HumanActorMessage> {
    private HumanConfig config;
    private Point actualPosition;
    private Door strategy = null;
    private List<Door> checkedDoors = newArrayList();

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
        // TODO: Check if you see {EXIT or TRANSITION} door and those doors are not already checked
        // TODO: If we see "good" doors - we go there

        if(isNull(strategy)){
            if(statCheck(config.getNervousness())) {
                setPanic();
            }
            // TODO: decide on strategy again
            // TODO: make movement
        } else {
            // TODO: Check if we keep our strategy
            if(statCheck(config.getInsistence())) {
                // TODO: decide on strategy again
            } else {
                //TODO: make movement
            }
        }
        return this;
    }

    private void setPanic() {
        config.setMobility(PANIC);
        // TODO: add changes to inconsistency, speed ...
        return;
    }

    private boolean statCheck(int stat) {
        return randomCheck(stat);
    }

    public boolean checkMove(Point to) {
        return getBuilding().isPointAvailable(to);
    }
}
