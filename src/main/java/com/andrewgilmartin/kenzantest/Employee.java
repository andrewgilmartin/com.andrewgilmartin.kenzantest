package com.andrewgilmartin.kenzantest;

import java.util.Date;
import java.util.Optional;

public class Employee {

    public static final boolean ACTIVE = true;
    public static final boolean INACTIVE = !ACTIVE;

    private String id;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private Date dateOfEmployment;
    private Boolean active;

    public Employee(String id, String firstName, String lastName, Date dateOfBirth, Date dateOfEmployment, boolean active) {
        assert id != null;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.dateOfEmployment = dateOfEmployment;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean hasDateOfEmployment() {
        return dateOfEmployment != null;
    }

    public Date getDateOfEmployment() {
        return dateOfEmployment;
    }

    public void setDateOfEmployment(Date dateOfEmployment) {
        this.dateOfEmployment = dateOfEmployment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Overwrite the fields of this instance with those that are present in that
     * instance. Can not overwrite id.
     */
    public void overwrite(Employee that) {
        if (that.firstName != null) {
            this.setFirstName(that.firstName);
        }
        if (that.lastName != null) {
            this.setLastName(that.lastName);
        }
        if (that.dateOfBirth != null) {
            this.setDateOfBirth(that.dateOfBirth);
        }
        if (that.dateOfEmployment != null) {
            this.setDateOfEmployment(that.dateOfEmployment);
        }
        if (that.active != null) {
            this.setActive(that.active);
        }
    }
    
    public void overwrite(Optional<Employee> that) {
        if ( that.isPresent() ) {
            overwrite(that.get());
        }
    }
}

// END
