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
import java.util.Optional;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET /employee/id 
        Optional<String> id = extractId(request.getPathInfo());
        if (id.isPresent()) {
            Optional<Employee> o = employeer.findById(id.get());
            if (o.isPresent()) {
                response.setStatus(HttpServletResponse.SC_OK);
                try (Writer out = response.getWriter()) {
                    gson.toJson(o.get(), out);
                }
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // PUT /employee + JSON
        Optional<Employee> data = extractEmployee(request);
        if (data.isPresent()) {
            Employee employee = employeer.create(
                    data.get().getFirstName(),
                    data.get().getLastName(),
                    data.get().getDateOfBirth(),
                    data.get().getDateOfEmployment()
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // POST /employee/id + JSON
        Optional<String> id = extractId(request.getPathInfo());
        if (id.isPresent()) {
            Optional<Employee> employee = employeer.findById(id.get());
            if (employee.isPresent()) {
                Optional<Employee> data = extractEmployee(request);
                if (data.isPresent()) {
                    employee.get().overwrite(data);
                    response.setStatus(HttpServletResponse.SC_OK);
                    try (Writer out = response.getWriter()) {
                        gson.toJson(employee.get(), out);
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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // DELETE /employees
        employeer.deleteAll();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.setContentLength(0);
    }

    private Optional<String> extractId(String text) {
        if (!isEmpty(text)) {
            Matcher m = ID_PATTERN.matcher(text);
            if (m.matches()) {
                return Optional.of(m.group(1));
            }
        }
        return Optional.empty();
    }

    private Optional<Employee> extractEmployee(HttpServletRequest request) {
        try {
            try (Reader in = request.getReader()) {
                Employee data = gson.fromJson(in, Employee.class);
                return Optional.of(data);
            }
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    private boolean isEmpty(String text) {
        return text == null || IS_EMPTY_STRING.matcher(text).matches();
    }

}

// END
