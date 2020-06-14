package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.infrastructure.Building;
import com.infrastructure.Door;
import com.messages.humanactor.HelloMessage;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.enums.Mobility.PANIC;
import static com.enums.TransferType.DEADEND;
import static com.enums.TransferType.EXIT_SIGNED;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Building.DoorDistance;
import static com.infrastructure.Building.getBuilding;
import static com.utility.RandomUtil.randomCheck;
import static java.util.Comparator.*;
import static java.util.Objects.isNull;

public class HumanActor extends AbstractBehavior<HumanActorMessage> {
    private HumanConfig config;
    private Point actualPosition;
    private Door strategy = null;
    private List<Door> checkedDoors = newArrayList();

    Predicate<DoorDistance> SIGNED_EXIT_PREDICATE = doorDistance ->
            getBuilding().getDoorTransferInGivenRoom(doorDistance.getDoor(),
                    getBuilding().getRoomByPoint(actualPosition).getId()).getType() == EXIT_SIGNED;

    Predicate<DoorDistance> DOORS_NOT_VISITED_YET = doorDistance ->
            !checkedDoors.contains(doorDistance.getDoor());

    Predicate<DoorDistance> KNOWS_ARE_EXIT = doorDistance ->
            getBuilding().getDoorTransferInGivenRoom(doorDistance.getDoor(),
                    getBuilding().getRoomByPoint(actualPosition).getId()).getType() == EXIT_SIGNED &&
                    this.config.getKnowledge() > 7;

    Predicate<DoorDistance> KNOWS_ARE_DEADEND = doorDistance ->
            getBuilding().getDoorTransferInGivenRoom(doorDistance.getDoor(),
                    getBuilding().getRoomByPoint(actualPosition).getId()).getType() != DEADEND &&
                    this.config.getKnowledge() > 7;


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

        if (getBuilding().getDoorByPoint(actualPosition) != null) {
            //TODO: If in the doors currently - continue moving in opposite direction from which came.
        }

        // TODO: implement human actor turn making
        // TODO: Check if you see {EXIT or TRANSITION} door and those doors are not already checked
        // TODO: If we see "good" doors - we go there

        if (isNull(strategy)) {
            strategy = getVisibleDoorsWithFilteredTransfers(SIGNED_EXIT_PREDICATE)
                    .orElse(getVisibleDoorsWithFilteredTransfers(KNOWS_ARE_EXIT)
                            .orElse(getVisibleDoorsWithFilteredTransfers(DOORS_NOT_VISITED_YET)
                                    .orElse(getVisibleDoorsWithFilteredTransfers(KNOWS_ARE_DEADEND)
                                            .orElse(null))));
        }


        if (isNull(strategy)) {
            if (statCheck(config.getNervousness())) {
                setPanic();
            }
            // TODO: decide on strategy again
            // TODO: make movement
        } else {
            // TODO: Check if we keep our strategy
            if (statCheck(config.getInsistence())) {
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

    private Optional<Door> getVisibleDoorsWithFilteredTransfers(Predicate<DoorDistance> predicate) {
        Building building = getBuilding();
        return building.getVisibleDoors(actualPosition, config.getVision()).stream()
                .filter(predicate)
                .min(comparing(DoorDistance::getDistance))
                .map(DoorDistance::getDoor);
    }
}
