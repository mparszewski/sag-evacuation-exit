package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.enums.FireRelation;
import com.infrastructure.Building;
import com.infrastructure.Door;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.enums.FireRelation.*;
import static com.enums.Mobility.*;
import static com.enums.TransferType.DEADEND;
import static com.enums.TransferType.EXIT_SIGNED;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Building.DoorDistance;
import static com.infrastructure.Building.getBuilding;
import static com.utility.RandomUtil.randomCheck;
import static com.utility.Utils.SAFE_POINT;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class HumanActor extends AbstractBehavior<HumanActorMessage> {
    public static final String DELIMITER = "%";
    private HumanConfig config;
    private Point actualPosition;
    private Door strategy = null;
    private List<Door> checkedDoors = newArrayList();
    private static final Logger logger = Logger.getLogger(HumanActor.class);
    private Point lastPosition;

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
        this.actualPosition = humanConfig.getStartingPoint();
    }

    @Override
    public Receive<HumanActorMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(MakeTurn.class, this::makeTurn)
                .build();
    }

    public HumanActor makeTurn(MakeTurn makeTurn) {
        if (config.getMobility() == CANT_MOVE || config.getMobility() == SAFE) {
            logPosition(makeTurn.getNumberOfRound());
            return this;
        }

        checkFire();

        Door currentDoors = getBuilding().getDoorByPoint(actualPosition);
        if (nonNull(currentDoors)) {
            if (currentDoors.isExitDoor()) {
                config.setMobility(SAFE);
                actualPosition = SAFE_POINT;
                getBuilding().updatePoint(actualPosition, SAFE_POINT);
                logPosition(makeTurn.getNumberOfRound());
                return this;
            }
            moveInDoors();
            return this;
        }

        strategy = getObviousStrategy();

        if (isNull(strategy)) {
            if (statCheck(config.getNervousness())) {
                setPanic();
            }
            moveAccordingToStrategy();
        } else {
            moveAccordingToStrategy();
        }
        logPosition(makeTurn.getNumberOfRound());
        return this;
    }

    private void logPosition(int roundNumber) {
        logger.info(roundNumber + DELIMITER + config.getName() +
                DELIMITER + actualPosition.toString() + DELIMITER + config.getMobility());
    }

    private void setPanic() {
        config.setMobility(PANIC);
    }

    private void checkFire() {
        FireRelation fireRelation = getBuilding().checkIfOnFire(actualPosition);

        if (fireRelation == ON_FIRE) {
            config.setMobility(CANT_MOVE);
        } else if (fireRelation == NEAR_FIRE) {
            config.setHealth(config.getHealth() - 3);
            moveToNonFirePoint();
        }
    }

    private void moveToNonFirePoint() {
        if (getBuilding().isPointAvailable(actualPosition.up())) {
            trueMove(actualPosition.up());
        }

        if (getBuilding().isPointAvailable(actualPosition.down())) {
            trueMove(actualPosition.down());
        }

        if (getBuilding().isPointAvailable(actualPosition.right())) {
            trueMove(actualPosition.right());
        }

        if (getBuilding().isPointAvailable(actualPosition.left())) {
            trueMove(actualPosition.left());
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

    public void moveAccordingToStrategy() {
        Point destination = strategy.getStartPoint();
        for (int i = 0; i < config.getSpeed(); i++) {
            move(destination);
        }
    }

    private void trueMove(Point newPoint) {
        getBuilding().updatePoint(actualPosition, newPoint);
        lastPosition = actualPosition;
        actualPosition = newPoint;
    }

    private Point moveOnX(Point destination) {
        Point newPoint;
        if (destination.getX() > actualPosition.getX()) {
            newPoint = actualPosition.right();
            if (getBuilding().isPointAvailable(newPoint)) {
                return newPoint;
            }
        } else if (destination.getX() < actualPosition.getX()) {
            newPoint = actualPosition.left();
            if (getBuilding().isPointAvailable(newPoint)) {
                return newPoint;
            }
        }
        return null;
    }

    private Point moveOnY(Point destination) {
        Point newPoint;
        if (destination.getY() < actualPosition.getY()) {
            newPoint = actualPosition.up();
            if (getBuilding().isPointAvailable(newPoint)) {
                return newPoint;
            }
        }
        if (destination.getY() > actualPosition.getY()) {
            newPoint = actualPosition.down();
            if (getBuilding().isPointAvailable(newPoint)) {
                return newPoint;
            }
        }
        return null;
    }

    private void move(Point destination) {
        Point newPoint = moveOnX(destination);
        if (isNull(newPoint)) {
            newPoint = moveOnY(destination);
        }
        if (nonNull(newPoint)) {
            trueMove(newPoint);
        }
    }

    private Optional<Door> getVisibleDoorsWithFilteredTransfers(Predicate<DoorDistance> predicate) {
        Building building = getBuilding();
        return building.getVisibleDoors(actualPosition, config.getVision()).stream()
                .filter(predicate)
                .min(comparing(DoorDistance::getDistance))
                .map(DoorDistance::getDoor);
    }

    private void moveInDoors() {
        Point newPoint = actualPosition;
        checkedDoors.add(getBuilding().getDoorByPoint(actualPosition));
        if (actualPosition.getX() > lastPosition.getX()) {
            newPoint = actualPosition.right();
        } else if (actualPosition.getX() < lastPosition.getX()) {
            newPoint = actualPosition.left();
        } else if (actualPosition.getY() > lastPosition.getY()) {
            newPoint = actualPosition.up();
        } else if (actualPosition.getY() < lastPosition.getY()) {
            newPoint = actualPosition.down();
        }

        if (getBuilding().isPointAvailable(newPoint)) {
            trueMove(newPoint);
        }
    }
}
