package org.ooad.server.model;

/**
 * Concrete Decorator 1: Adds the 'Food Sensitive' attribute.
 */
public class FoodSensitiveMedication extends MedicationDecorator {

    public FoodSensitiveMedication(Medication decoratedMedication) {
        super(decoratedMedication);
    }

    @Override
    public String getAttributes() {
        return decoratedMedication.getAttributes() + ", Food Sensitive (Take with food)";
    }
}