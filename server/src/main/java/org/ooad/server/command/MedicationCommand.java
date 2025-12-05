package org.ooad.server.command;

/**
 * Command Pattern Interface.
 * Encapsulates a request as an object, allowing for parameterization of clients
 * with different requests (Take, Snooze, Skip).
 */
public interface MedicationCommand {
    void execute();
}