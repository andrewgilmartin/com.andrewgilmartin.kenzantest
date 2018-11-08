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
     * Get an employee details.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET /employee/id 
        // GET /employee 
        String id = extractId(request.getPathInfo());
        if (id != null) {
            Employee employee = employeer.findById(id);
            if (employee != null) {
                respond(response, HttpServletResponse.SC_OK, employee);
            } else {
                respond(response, HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            respond(response, HttpServletResponse.SC_OK, employeer.findAll());
        }
    }

    /**
     * Create an employee. The field values are a partial employee JSON
     * structure. Include in the structure those fields to define.
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
            respond(response, HttpServletResponse.SC_OK, employee);
        } else {
            respond(response, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Update an employee details. The the field values are in a partial
     * employee JSON structure. Only include in the structure those fields to
     * update.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // POST /employee/id + JSON
        String id = extractId(request.getPathInfo());
        if (id != null) {
            Employee employee = employeer.findAnyById(id); // TODO can I update an inactive employee?
            if (employee != null) {
                Employee updates = extractEmployee(request);
                if (updates != null) {
                    employee.overwrite(updates);
                    respond(response, HttpServletResponse.SC_OK, employee);
                } else {
                    respond(response, HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                respond(response, HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            respond(response, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Deactivate all the employees.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // DELETE /employee/id
        // DELETE /employee
        if (request.isUserInRole("manager")) {
            String id = extractId(request.getPathInfo());
            if (id != null) {
                Employee employee = employeer.deactiveById(id);
                if (employee != null) {
                    respond(response, HttpServletResponse.SC_NO_CONTENT);
                } else {
                    respond(response, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                employeer.deactivateAll();
                respond(response, HttpServletResponse.SC_NO_CONTENT);
            }
        } else {
            respond(response, HttpServletResponse.SC_UNAUTHORIZED);
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
            Employee data = gson.fromJson(in, Employee.class
            );
            return data;
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            return null;
        }
    }

    private boolean isEmpty(String text) {
        return text == null || IS_EMPTY_STRING.matcher(text).matches();
    }

    private void respond(HttpServletResponse response, int status) {
        respond(response, status, null);
    }

    private void respond(HttpServletResponse response, int status, Object data) {
        response.setStatus(status);
        if (data != null) {
            try (Writer out = response.getWriter()) {
                gson.toJson(data, out);
            } catch (IOException | JsonIOException e) {
                // TODO
            }
        } else {
            response.setContentLength(0);
        }
    }

}

// END
