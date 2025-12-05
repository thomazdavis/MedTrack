package org.ooad.server.strategy;

import org.ooad.server.model.Medication;
import java.util.List;

/**
 * Strategy Pattern Interface.
 * Defines a family of algorithms for checking drug interactions.
 */
public interface InteractionStrategy {
    /**
     * Checks if a new medication interacts dangerously with existing medications.
     * @param newMedication The medication being added.
     * @param existingMedications The list of current medications.
     * @return A warning message if an interaction exists, or null if safe.
     */
    String checkInteraction(Medication newMedication, List<Medication> existingMedications);
}