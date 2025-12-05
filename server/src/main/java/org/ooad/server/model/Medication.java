package org.ooad.server.model;

import java.time.LocalDateTime;

/**
 * Component Interface for the Decorator Pattern.
 * This defines the common operations that both the base object
 * and the decorators must support.
 * The core functionality is getting medication details and attributes.
 */

public class Medication {
    Long getId();
    String getName();
    String getDosageForm();
    String getAttributes();
    LocalDateTime getNextDueTime();
}
