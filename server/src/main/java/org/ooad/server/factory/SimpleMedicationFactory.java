package org.ooad.server.factory;

import org.ooad.server.model.BaseMedication;
import org.ooad.server.model.FoodSensitiveMedication;
import org.ooad.server.model.Medication;
import org.springframework.stereotype.Component;

/**
 * Concrete Factory for creating Medication objects.
 * Uses Dependency Injection via @Component.
 * Demonstrates how a Factory can use other patterns (like Decorator) internally.
 */
@Component
public class SimpleMedicationFactory implements MedicationFactory {

    /**
     * Factory method implementation.
     * Creates a BaseMedication and wraps it with appropriate decorators.
     */
    @Override
    public Medication createMedication(String name, String dosageForm, boolean isFoodSensitive) {
        // 1. Create the base component
        Medication medication = new BaseMedication(name, dosageForm);

        // 2. Apply decorators based on flags (Coding to Abstraction - Medication interface)
        if (isFoodSensitive) {
            medication = new FoodSensitiveMedication(medication);
        }

        // We can add other decorators here later (e.g., TimeSensitiveMedication, ControlledMedication)

        return medication;
    }
}
