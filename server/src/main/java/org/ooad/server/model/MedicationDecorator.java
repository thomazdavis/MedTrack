package org.ooad.server.model;

import jakarta.persistence.Transient;
import java.time.LocalDateTime;

/*
 * Abstract Decorator class for the Decorator Pattern.
 */

public abstract class MedicationDecorator implements Medication {

    // The wrapped component. This is marked as @Transient because we don't
    // want JPA to try and persist this relationship directly in this abstract class.
    @Transient
    protected final Medication decoratedMedication;

    protected MedicationDecorator(Medication decoratedMedication) {
        this.decoratedMedication = decoratedMedication;
    }

    @Override
    public Long getId() {
        return decoratedMedication.getId();
    }

    @Override
    public String getName() {
        return decoratedMedication.getName();
    }

    @Override
    public String getDosageForm() {
        return decoratedMedication.getDosageForm();
    }

    @Override
    public LocalDateTime getNextDueTime() {
        return decoratedMedication.getNextDueTime();
    }

    @Override
    public abstract String getAttributes();
}