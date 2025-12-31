package com.mogrul.prison.model;

import com.mogrul.prison.manager.ConfigManager;

import java.time.LocalDateTime;
import java.util.UUID;

public class Prisoner {
    private final UUID uuid;
    private final String sentence;
    private final LocalDateTime firstJoin;
    private final Cell cell;

    private String username;
    private LocalDateTime lastJoin;
    private int contraband;

    public Prisoner(
            UUID uuid,
            String username, String sentence,
            Integer contraband,
            LocalDateTime firstJoin, LocalDateTime lastJoin,
            Cell cell
    ) {
        this.uuid = uuid;
        this.username = username;
        this.sentence = sentence;
        this.contraband = contraband;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.cell = cell;
    }

    // GETTERS
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getSentence() {
        return sentence;
    }

    public LocalDateTime getFirstJoin() {
        return firstJoin;
    }

    public LocalDateTime getLastJoin() {
        return lastJoin;
    }

    public int getContraband() {
        return contraband;
    }

    public Cell getCell() {
        return cell;
    }


    // SETTERS
    public void setLastJoin(LocalDateTime lastJoin) {
        this.lastJoin = lastJoin;
    }

    public void setContraband(int amount) {
        this.contraband = amount;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    // ADDERS
    public void addContraband(int amount) {
        this.contraband += amount;
    }


    // REMOVERS
    public void removeContraband(int amount) {
        if (amount <= 0) return;
        if (amount >= contraband) {
            contraband = 0;
        } else {
            contraband -= amount;
        }
    }


    // RESETTERS
    public void resetContraband() {
        this.contraband = ConfigManager.getInt("starting-contraband");
    }
}
