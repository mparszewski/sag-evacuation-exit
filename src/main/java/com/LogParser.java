package com;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.HumanActor.DELIMITER;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class LogParser {

    public void parse(int numberOfActors, String pathToSave) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("logs.log"));
        List<String> fireEntries = newArrayList();
        List<ActorEntry> actorEntries = newArrayList();
        for (String line = br.readLine(); !line.equals("FINISH"); line = br.readLine()) {
            if (line.equals("START")) {
                continue;
            } else if (line.contains("ROUND")) {
                fireEntries.add(br.readLine());
            } else {
                String[] values = line.split(DELIMITER);
                actorEntries.add(new ActorEntry(values[0], values[1], values[2], values[3]));
            }
        }

        fireEntries.forEach(System.out::println);


        FileWriter writer = new FileWriter(pathToSave);
        StringBuilder builder = new StringBuilder("nr_rundy");
        for (int i = 1; i <= numberOfActors; i++) {
            builder.append(", ").append(i);
        }
        builder.append(", ");
        builder.append("ogien");
        builder.append("\n");

        int i = 0;
        for (String fireEntry : fireEntries) {
            i++;
            builder.append(i);
            for (ActorEntry actorEntry : filteredActorEntries(actorEntries, valueOf(i))) {
                builder.append(", ").append(actorEntry.mergeCoordinatesAndMobility());
            }
            builder.append(", [").append(fireEntry).append("]").append("\n");
        }
        writer.append(builder.toString());
        writer.flush();
        writer.close();
    }

    public List<ActorEntry> filteredActorEntries(List<ActorEntry> actors, String roundNumber) {
        return actors.stream()
                .filter(actorEntry -> actorEntry.roundNumber.equals(roundNumber))
                .sorted(comparing(ActorEntry::getId))
                .collect(toList());
    }


    @Getter
    @AllArgsConstructor
    private static class ActorEntry {
        private String roundNumber;
        private String id;
        private String coordinates;
        private String mobility;

        public String mergeCoordinatesAndMobility() {
            return coordinates.replace(")", ", " + mobility + ")");
        }

    }
}
