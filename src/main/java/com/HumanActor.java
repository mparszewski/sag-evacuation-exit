package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.enums.Direction;
import com.enums.FireRelation;
import com.enums.InfrastructureElement;
import com.infrastructure.Building;
import com.infrastructure.Door;
import com.infrastructure.Fire;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;
import com.utility.RandomUtil;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.enums.FireRelation.NEAR_FIRE;
import static com.enums.FireRelation.ON_FIRE;
import static com.enums.InfrastructureElement.*;
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
                while (getBuilding().getAgents().contains(actualPosition)) {
                    getBuilding().getAgents().remove(actualPosition);
                }
                actualPosition = SAFE_POINT;
                getBuilding().updatePoint(actualPosition, SAFE_POINT);
                logPosition(makeTurn.getNumberOfRound());
                return this;
            }
            moveInDoors();
            logPosition(makeTurn.getNumberOfRound());
            return this;
        }

        strategy = getObviousStrategy();

        if (isNull(strategy)) {
            if (statCheck(config.getNervousness())) {
                setPanic();
            }
            moveRandomly();
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
            if (config.getHealth() <= 0) {
                config.setMobility(CANT_MOVE);
            } else {
                moveToNonFirePoint();
            }
        }
    }

    private void moveToNonFirePoint() {
        Stream.of(Direction.values())
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .map(this::mapDirectionToPoint)
                .filter(getBuilding()::isPointAvailable)
                .filter(point -> point != lastPosition)
                .findFirst()
                .ifPresent(this::trueMove);
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
            checkMoveInDoors();
        }
    }

    public void moveRandomly() {
        for (int i = 1; i <= config.getSpeed(); i++) {
            moveToNonFirePoint();
            checkMoveInDoors();
        }
    }

    private void checkMoveInDoors() {
        Door currentDoors = getBuilding().getDoorByPoint(actualPosition);
        if (nonNull(currentDoors)) {
            if (currentDoors.isExitDoor()) {
                config.setMobility(SAFE);
                while (getBuilding().getAgents().contains(actualPosition)) {
                    getBuilding().getAgents().remove(actualPosition);
                }
                actualPosition = SAFE_POINT;
                getBuilding().updatePoint(actualPosition, SAFE_POINT);
            }
            moveInDoors();
            strategy = getObviousStrategy();
        }
    }

    private Point mapDirectionToPoint(Direction direction) {
        switch (direction) {
            case UP:
                return actualPosition.up();
            case DOWN:
                return actualPosition.down();
            case RIGHT:
                return actualPosition.right();
            case LEFT:
                return actualPosition.left();
            default:
                return null;
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
        if (RandomUtil.randomCheck(5)) {
            Point newPoint = moveOnX(destination);
            if (isNull(newPoint)) {
                newPoint = moveOnY(destination);
            }
            if (nonNull(newPoint)) {
                trueMove(newPoint);
            }
        } else {
            Point newPoint = moveOnY(destination);
            if (isNull(newPoint)) {
                newPoint = moveOnX(destination);
            }
            if (nonNull(newPoint)) {
                trueMove(newPoint);
            }
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
            newPoint = actualPosition.down();
        } else if (actualPosition.getY() < lastPosition.getY()) {
            newPoint = actualPosition.up();
        }

        if (getBuilding().isPointAvailable(newPoint)) {
            trueMove(newPoint);
        } else if (getBuilding().isHumanThere(newPoint)) {
            trueMove(lastPosition);
        }
    }
}
