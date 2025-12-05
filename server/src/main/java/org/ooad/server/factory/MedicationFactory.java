package org.ooad.server.factory;

import org.ooad.server.model.Medication;

/**
 * Factory Method Interface.
 * Defines the contract for creating Medication objects.
 * This codes to an abstraction, allowing different implementations (factories)
 * to be swapped out without changing the client code (Service).
 */
public interface MedicationFactory {
    /**
     * Creates a Medication object, applying decorators based on input flags.
     * @param name The medication name.
     * @param dosageForm The form (Tablet, Capsule, etc.).
     * @param isFoodSensitive Flag for applying the FoodSensitiveMedication decorator.
     * @return A decorated or base Medication instance.
     */
    Medication createMedication(String name, String dosageForm, boolean isFoodSensitive);
}