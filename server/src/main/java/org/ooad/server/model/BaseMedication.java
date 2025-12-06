package org.ooad.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class BaseMedication implements Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private  String dosageForm;
    private LocalDateTime nextDueTime;

    public BaseMedication() {
    }

    public BaseMedication(String name, String dosageForm) {
        this.name = name;
        this.dosageForm = dosageForm;
        // FIX: Set due time to 10 seconds from now, not 8 hours,
        // so the ReminderSystem (Observer) triggers almost instantly.
        this.nextDueTime = LocalDateTime.now().plusSeconds(10);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDosageForm() {
        return dosageForm;
    }

    @Override
    public String getAttributes() {
        return "Standard";
    }

    @Override
    public LocalDateTime getNextDueTime() {
        return nextDueTime;
    }

    public void setNextDueTime(LocalDateTime nextDueTime) {
        this.nextDueTime = nextDueTime;
    }

    // Setters for JPA/Hibernate
    public void setName(String name) {
        this.name = name;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }
}