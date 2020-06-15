package com;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.enums.FireRelation;
import com.infrastructure.Building;
import com.infrastructure.Door;
import com.messages.humanactor.HelloMessage;
import com.messages.humanactor.HumanActorMessage;
import com.messages.humanactor.MakeTurn;
import com.models.HumanConfig;
import com.models.Point;
import com.utility.RandomUtil;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.enums.FireRelation.NEAR_FIRE;
import static com.enums.FireRelation.ON_FIRE;
import static com.enums.Mobility.*;
import static com.enums.TransferType.DEADEND;
import static com.enums.TransferType.EXIT_SIGNED;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Building.DoorDistance;
import static com.infrastructure.Building.getBuilding;
import static com.utility.RandomUtil.randomCheck;
import static java.lang.Math.abs;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

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
                .onMessage(HelloMessage.class, this::helloMessage)
                .onMessage(MakeTurn.class, this::makeTurn)
                .build();
    }

    public HumanActor helloMessage(HelloMessage message) {
        logger.info("Hello actor: " + config.getName());
        System.out.println("Hello from actor " + config.getName());
        return this;
    }

    public HumanActor makeTurn(MakeTurn makeTurn) {
        if(config.getMobility() == CANT_MOVE || config.getMobility() == SAFE) {
            logger.info(makeTurn.getNumberOfRound() + DELIMITER + config.getName() +
                    DELIMITER + actualPosition.toString() + DELIMITER + config.getMobility());
            return this;
        }

        checkFire();

        if (getBuilding().getDoorByPoint(actualPosition) != null) {
            moveInDoors();
        }

        strategy = getObviousStrategy();

        if (isNull(strategy)) {
            if (statCheck(config.getNervousness())) {
                setPanic();
            }
            moveWithoutStrategy();
        } else {
            moveAccordingToStrategy();
        }
        logger.info(makeTurn.getNumberOfRound() + DELIMITER + config.getName() +
                DELIMITER + actualPosition.toString() + DELIMITER + config.getMobility());
        return this;
    }

    private void setPanic() {
        config.setMobility(PANIC);
        // TODO: add changes to inconsistency, speed ...
        return;
    }

    private void checkFire() {
        FireRelation fireRelation = getBuilding().checkIfOnFire(actualPosition);

        if (fireRelation == ON_FIRE) {
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

    public void moveAccordingToStrategy() {
        Point vector = new Point(strategy.getStartPoint().getX() - actualPosition.getX(),
                strategy.getStartPoint().getY() - actualPosition.getY());
        move(vector);
    }

    public void moveWithoutStrategy() {
        int divide = RandomUtil.getRandomValue(0, config.getSpeed());
        int x = config.getSpeed() - divide;
        Point vector = new Point(x, divide);
        move(vector);
    }

    private void move(Point vector) {
        for (int i = 1; i <= config.getSpeed(); i++) {
            Point transformation;
            if (abs(vector.getX()) > abs(vector.getY())) {
                transformation = new Point(vector.getX() / abs(vector.getX()), 0);
            } else {
                transformation = new Point(0, vector.getX() / abs(vector.getX()));
            }
            Point newPoint = new Point(actualPosition.getX() + transformation.getX(),
                    actualPosition.getY() + transformation.getY());
            if (getBuilding().isPointAvailable(newPoint)) {
                lastPosition = actualPosition;
                actualPosition = newPoint;
                vector.setX(vector.getX() - transformation.getX());
                vector.setY(vector.getY() - transformation.getY());
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
        checkedDoors.add(getBuilding().getDoorByPoint(actualPosition));
        if(actualPosition.getX() > lastPosition.getX() ) {
            actualPosition.setX(actualPosition.getX() + 1);
        } else if (actualPosition.getX() < lastPosition.getX()) {
            actualPosition.setX(actualPosition.getX() - 1 );
        } else if(actualPosition.getY() > lastPosition.getY() ) {
            actualPosition.setY(actualPosition.getY() + 1);
        } else if (actualPosition.getY() < lastPosition.getY()) {
            actualPosition.setY(actualPosition.getY() - 1 );
        }
    }
}
