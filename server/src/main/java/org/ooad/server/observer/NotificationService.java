package org.ooad.server.observer;

import jakarta.annotation.PostConstruct;
import org.ooad.server.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles the actual notification logic (e.g., displaying an alert on the UI).
 */
@Service
public class NotificationService implements ReminderObserver {

    private final ReminderSystem reminderSystem;

    // Use DI to get the Subject (ReminderSystem)
    @Autowired
    public NotificationService(ReminderSystem reminderSystem) {
        this.reminderSystem = reminderSystem;
    }

    /**
     * Use @PostConstruct to ensure the observer registers itself with the subject
     * as soon as the service bean is initialized by Spring.
     */
    @PostConstruct
    public void init() {
        // Self-registration: The observer attaches itself to the subject
        this.reminderSystem.attach(this);
    }

    @Override
    public void update(Medication medication) {
        System.out.println("--- UI/PUSH NOTIFICATION HANDLER ---");
        System.out.printf("ALERT: Time to take %s (%s). Attributes: %s\n",
                medication.getName(),
                medication.getDosageForm(),
                medication.getAttributes());
        System.out.println("------------------------------------");
    }
}