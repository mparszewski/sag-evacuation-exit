package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.enums.FireRelation;
import com.enums.Mobility;
import com.infrastructure.Building;
import com.infrastructure.Door;
import com.messages.humanactor.HelloMessage;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.enums.FireRelation.NEAR_FIRE;
import static com.enums.FireRelation.ON_FIRE;
import static com.enums.Mobility.CANT_MOVE;
import static com.enums.Mobility.PANIC;
import static com.enums.TransferType.DEADEND;
import static com.enums.TransferType.EXIT_SIGNED;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Building.DoorDistance;
import static com.infrastructure.Building.getBuilding;
import static com.utility.RandomUtil.randomCheck;
import static java.util.Comparator.comparing;
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
            !checkedDoors.contains(doorDistance.getDoor()) || config.getMobility() == PANIC;

    Predicate<DoorDistance> KNOWS_ARE_EXIT = doorDistance ->
            getBuilding().getDoorTransferInGivenRoom(doorDistance.getDoor(),
                    getBuilding().getRoomByPoint(actualPosition).getId()).getType() == EXIT_SIGNED &&
                    statCheck(config.getKnowledge());

    Predicate<DoorDistance> KNOWS_ARE_DEADEND = doorDistance ->
            getBuilding().getDoorTransferInGivenRoom(doorDistance.getDoor(),
                    getBuilding().getRoomByPoint(actualPosition).getId()).getType() != DEADEND &&
                    statCheck(config.getKnowledge());


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

        checkFire();

        if (getBuilding().getDoorByPoint(actualPosition) != null) {
            //TODO: If in the doors currently - continue moving in opposite direction from which came.
        }

        strategy = getObviousStrategy();

        if(isNull(strategy)) {
            if (statCheck(config.getNervousness())) {
                setPanic();
            }
            moveWithoutStrategy();
        } else {
            moveAccordingStrategy();
        }
        return this;
    }

    private void setPanic() {
        config.setMobility(PANIC);
        // TODO: add changes to inconsistency, speed ...
        return;
    }

    private void checkFire() {
        FireRelation fireRelation = getBuilding().checkIfOnFire(actualPosition);

        if(fireRelation == ON_FIRE) {
            config.setMobility(CANT_MOVE);
        } else if (fireRelation == NEAR_FIRE) {
            config.setHealth(config.getHealth() - 3);
        }
    }

    private Door getObviousStrategy() {
        return getVisibleDoorsWithFilteredTransfers(SIGNED_EXIT_PREDICATE)
                .orElse(getVisibleDoorsWithFilteredTransfers(KNOWS_ARE_EXIT)
                        .orElse(getVisibleDoorsWithFilteredTransfers(DOORS_NOT_VISITED_YET)
                                .orElse(getVisibleDoorsWithFilteredTransfers(KNOWS_ARE_DEADEND)
                                        .orElse(null))));
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
