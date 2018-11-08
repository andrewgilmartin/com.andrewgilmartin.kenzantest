package com.andrewgilmartin.kenzantest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmployerHttpServlet extends HttpServlet {

    private static final Pattern ID_PATTERN = Pattern.compile("/([^/]+)$");
    private static final Pattern IS_EMPTY_STRING = Pattern.compile("\\s*");

    private final Employeer employeer = new Employeer();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            String localname = config.getInitParameter("employees");
            if (!isEmpty(localname)) {
                InputStream stream = config.getServletContext().getResourceAsStream(localname);
                if (stream != null) {
                    try (Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        employeer.load(in);
                    }
                }
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Get an employee details. {@code
     *
     * curl -D - 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'
     *
     * }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET /employee/id 
        String id = extractId(request.getPathInfo());
        if (id != null) {
            Employee employee = employeer.findById(id);
            if (employee != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                try (Writer out = response.getWriter()) {
                    gson.toJson(employee, out);
                }
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
    }

    /**
     * Create an employee. The field values are a partial employee JSON
     * structure. Include in the structure those fields to define. {@code
     *
     * 
     * curl -D - -X PUT -H 'Content-Type: application/json' --data '{ "firstName":"Joe", "lastName":"Friday", "dateOfBirth":"1936-02-12" }' 'http://localhost:8080/kenzantest'
     *
     * }
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // PUT /employee + JSON
        Employee data = extractEmployee(request);
        if (data != null) {
            Employee employee = employeer.create(
                    data.getFirstName(),
                    data.getLastName(),
                    data.getDateOfBirth(),
                    data.getDateOfEmployment()
            );
            response.setStatus(HttpServletResponse.SC_OK);
            try (Writer out = response.getWriter()) {
                gson.toJson(employee, out);
            }
            return;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentLength(0);
    }

    /**
     * Update an employee details. The the field values are in a partial
     * employee JSON structure. Only include in the structure those fields to
     * update. {@code
     *
     * curl -D - -X POST -H 'Content-Type: application/json' --data '{ "firstName":"Henry", "dateOfBirth":"1999-11-19" }' 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'
     * 
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // POST /employee/id + JSON
        String id = extractId(request.getPathInfo());
        if (id != null) {
            Employee employee = employeer.findById(id);
            if (employee != null) {
                Employee updates = extractEmployee(request);
                if (updates != null) {
                    employee.overwrite(updates);
                    response.setStatus(HttpServletResponse.SC_OK);
                    try (Writer out = response.getWriter()) {
                        gson.toJson(employee, out);
                    }
                    return;
                }
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentLength(0);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
    }

    /**
     * Deactivate all the employees. {@code
     *
     * curl -D - -X DELETE --user kenzan:kenzan 'http://localhost:8080/kenzantest'
     * 
     * }
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // DELETE /employees
        if (request.isUserInRole("manager")) {
            employeer.deactivateAll();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.setContentLength(0);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentLength(0);
        }
    }

    private String extractId(String text) {
        if (!isEmpty(text)) {
            Matcher m = ID_PATTERN.matcher(text);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return null;
    }

    private Employee extractEmployee(HttpServletRequest request) {
        try (Reader in = request.getReader()) {
            Employee data = gson.fromJson(in, Employee.class);
            return data;
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            return null;
        }
    }

    private boolean isEmpty(String text) {
        return text == null || IS_EMPTY_STRING.matcher(text).matches();
    }

}

// END
