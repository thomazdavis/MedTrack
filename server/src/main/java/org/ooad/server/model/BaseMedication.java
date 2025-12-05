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
        this.nextDueTime = LocalDateTime.now().plusHours(8);
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