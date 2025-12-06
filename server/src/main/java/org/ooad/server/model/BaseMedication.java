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
    private String dosageForm;
    private LocalDateTime nextDueTime;

    private int dosagesPerDay = 1;

    public BaseMedication() {}

    public BaseMedication(String name, String dosageForm, int dosagesPerDay) {
        this.name = name;
        this.dosageForm = dosageForm;
        this.dosagesPerDay = dosagesPerDay;
        this.nextDueTime = LocalDateTime.now().plusSeconds(10);
    }

    public BaseMedication(String name, String dosageForm) {
        this(name, dosageForm, 1);
    }

    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    @Override
    public String getAttributes() { return "Standard"; }

    @Override
    public LocalDateTime getNextDueTime() { return nextDueTime; }
    public void setNextDueTime(LocalDateTime nextDueTime) { this.nextDueTime = nextDueTime; }

    public int getDosagesPerDay() { return dosagesPerDay; }
    public void setDosagesPerDay(int dosagesPerDay) { this.dosagesPerDay = dosagesPerDay; }
}