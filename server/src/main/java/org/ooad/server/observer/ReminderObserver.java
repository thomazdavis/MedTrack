package org.ooad.server.observer;

import org.ooad.server.model.Medication;

/**
 * Observer Interface (Observer Pattern).
 * This abstraction ensures all observers respond to the same notification method.
 */
public interface ReminderObserver {
    /**
     * Called by the Subject (ReminderSystem) when a medication is due.
     * @param medication The medication that is due.
     */
    void update(Medication medication);
}