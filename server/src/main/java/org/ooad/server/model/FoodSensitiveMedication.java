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
        String baseAttributes = decoratedMedication.getAttributes();
        if ("Standard".equals(baseAttributes)) {
            return "Food Sensitive (Take with food)";
        } else {
            return baseAttributes + ", Food Sensitive (Take with food)";
        }
    }
}