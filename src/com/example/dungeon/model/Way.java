package com.example.dungeon.model;

public class Way {
    private final Room destination;
    private Door door;

    public Way(Room destination) {
        this.destination = destination;
        this.door = null;
    }

    public Way(Room destination, Door door) {
        this.destination = destination;
        this.door = door;
    }

    public Room getDestination() {
        return destination;
    }

    public Door getDoor() {
        return door;
    }

    public void setDoor(Door door) {
        this.door = door;
    }

    public boolean isBlocked() {
        return door != null && door.isLocked();
    }

    public String getDescription() {
        if (door != null) {
            String doorName = door.getName();
            String doorStatus = door.isLocked() ? " [заперта]" : " [открыта]";
            return  doorName + doorStatus;
        }
        return "";
    }
}