package org.ooad.server.observer;

import org.ooad.server.model.Medication;
import org.ooad.server.repository.MedicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject/Observable in the Observer Pattern.
 * Manages observers (subscribers) and notifies them when an event occurs (medication is due).
 * Also acts as the Reminder Scheduler (a separate responsibility).
 */
@Service
public class ReminderSystem {

    private final List<ReminderObserver> observers = new ArrayList<>();

    private final MedicationRepository medicationRepository;

    @Autowired
    public ReminderSystem(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public void attach(ReminderObserver observer) {
        observers.add(observer);
        System.out.println("Observer attached: " + observer.getClass().getSimpleName());
    }

    public void detach(ReminderObserver observer) {
        observers.remove(observer);
        System.out.println("Observer detached: " + observer.getClass().getSimpleName());
    }

    public void notifyObservers(Medication medication) {
        System.out.println("--- NOTIFYING OBSERVERS for: " + medication.getName() + " ---");
        for (ReminderObserver observer : observers) {
            observer.update(medication);
        }
    }

    /**
     * Scheduled method to check for due medications every 10 seconds (for demo).
     * The @Scheduled annotation requires @EnableScheduling on the main application class.
     */
    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void checkForDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        // In a real app, this query would be optimized (e.g., only check items due in the next minute)
        medicationRepository.findAll().stream()
                .filter(med -> med.getNextDueTime() != null && med.getNextDueTime().isBefore(now))
                .forEach(med -> {
                    notifyObservers(med);

                    // Advance the due time (Crucial for persistence logic)
                    // For demo, we just advance the time by a fixed amount (e.g., 24 hours)
                    med.setNextDueTime(med.getNextDueTime().plusDays(1));
                    medicationRepository.save(med); // Persist the updated time
                });
    }
}