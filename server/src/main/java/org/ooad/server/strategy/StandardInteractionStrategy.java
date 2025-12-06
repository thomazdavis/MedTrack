package org.ooad.server.strategy;

import org.ooad.server.model.Medication;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Implements a basic rule-based interaction check.
 */
@Component
public class StandardInteractionStrategy implements InteractionStrategy {

    @Override
    public String checkInteraction(Medication newMedication, List<Medication> existingMedications) {
        String newName = newMedication.getName().toLowerCase();

        for (Medication existing : existingMedications) {
            String existingName = existing.getName().toLowerCase();

            if ((newName.contains("aspirin") && existingName.contains("warfarin")) ||
                    (newName.contains("warfarin") && existingName.contains("aspirin"))) {
                return "CRITICAL INTERACTION: " + newMedication.getName() + " and " + existing.getName() + " may cause bleeding risks.";
            }

            if (newName.contains("cipro") && existing.getAttributes().contains("Food Sensitive")) {
                return "INTERACTION: " + newMedication.getName() + " might interact with food/supplements associated with " + existing.getName();
            }
        }
        return null;
    }
}
