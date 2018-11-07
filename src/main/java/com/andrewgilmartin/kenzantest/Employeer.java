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
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    public void load(Reader in) throws IOException {
        Employee[] ee = gson.fromJson(in, Employee[].class);
        Arrays.stream(ee).forEach(e -> employees.add(e));
    }

    public void store(Writer out) throws IOException {
        gson.toJson(employees, out);
    }

    public Optional<Employee> findById(String id) {
        lock.readLock().lock();
        try {
            return employees.stream().filter(e -> e.isActive() && e.getId().equals(id)).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Employee> findAll() {
        lock.readLock().lock();
        try {
            return employees.stream().filter(e -> e.isActive()).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void deleteAll() {
        lock.writeLock().lock();
        try {
            employees.forEach(e -> e.setActive(false));
        } finally {
            lock.writeLock().unlock();
        }
    }

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
