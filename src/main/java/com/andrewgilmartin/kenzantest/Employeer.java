package com.andrewgilmartin.kenzantest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * The employer manages a collection of employees.
 */
public class Employeer {

    private final List<Employee> employees = new LinkedList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    /**
     * Load the employees from the reader. The employees are encoded as a JSON
     * array of employee objects.
     */
    public void load(Reader in) throws IOException {
        Employee[] ee = gson.fromJson(in, Employee[].class);
        Arrays.stream(ee).forEach(e -> employees.add(e));
    }

    /**
     * Store the employees to the writer. The employees are encoded as a JSON
     * array of employee objects.
     */
    public void store(Writer out) throws IOException {
        gson.toJson(employees, out);
    }

    /**
     * Find the employee with the given id. Null is returned when no employee
     * found.
     */
    public Employee findById(String id) {
        lock.readLock().lock();
        try {
            Optional<Employee> employee = employees.stream().filter(e -> e.isActive() && e.getId().equals(id)).findFirst();
            return employee.isPresent() ? employee.get() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Return a list of all employees.
     */
    public List<Employee> findAll() {
        lock.readLock().lock();
        try {
            return employees.stream().filter(e -> e.isActive()).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Deactivate all the employees.
     */
    public void deactivateAll() {
        lock.writeLock().lock();
        try {
            employees.forEach(e -> e.setActive(false));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Create the new employee and add to the list of employees. Returns the
     * created employee.
     */
    public Employee create(String firstName, String lastName, Date dateOfBirth, Date dateOfEmployment) {
        Employee e = new Employee(
                UUID.randomUUID().toString(),
                firstName,
                lastName,
                dateOfBirth,
                dateOfEmployment,
                Employee.ACTIVE
        );
        lock.writeLock().lock();
        try {
            employees.add(e);
        } finally {
            lock.writeLock().unlock();
        }
        return e;
    }
}

// END
