package org.ooad.server.model;

import java.time.LocalDateTime;

/**
 * Component Interface for the Decorator Pattern.
 */

public interface Medication {
    Long getId();
    String getName();
    String getDosageForm();
    String getAttributes();
    LocalDateTime getNextDueTime();
}