package crud.nominas.controller;

import crud.nominas.dao.EmployeeDaoImpl;
import crud.nominas.dao.FactoryDAO;
import crud.nominas.model.Employee;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@WebServlet (description = "Employee " + "Servlet", urlPatterns = {"/empresa"})
public class EmployeeController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final EmployeeDaoImpl empleadoDaoImpl = FactoryDAO.getDao(EmployeeDaoImpl.class);

    public EmployeeController() {

    }

    /**
     * Method to handle GET method request.
     *
     * @param req  an {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String option = req.getParameter("option");
        HttpSession session = req.getSession();

        if(option.equals("create")) {
            req.setAttribute("option", "create");
            RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);
        } else if(option.equals("listing")) {
            req.setAttribute("option", "listing");

            try {
                List<Employee> employeeList = empleadoDaoImpl.getAll();
                req.setAttribute("EmployeeList", employeeList);
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                rd.forward(req, resp);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else if(option.equals("salary")) {
            req.setAttribute("option", "salary");
            RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);

        } else if(option.equals("search")) {
            req.setAttribute("option", "search");
            RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);
        } else if(option.equals("editEmployee")) {
            req.setAttribute("option", "editEmployee");

            String dni = req.getParameter("dni");
            Employee empl = new Employee();
            try {
                empl = empleadoDaoImpl.getByDni(dni);
                req.setAttribute("Employee", empl);
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                rd.forward(req, resp);
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        } else if(option.equals("deleteEmployee")) {

            String dni = req.getParameter("dni");
            try {
                empleadoDaoImpl.delete(dni);
                session.setAttribute("error4", "Register succesfully deleted...");
                resp.sendRedirect(req.getContextPath() + "/empresa?option=listing");
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Method to handle POST method request.
     *
     * @param req  an {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String option = req.getParameter("option");
        if(option.equals("save")) {
            pushEmployee(req, session);
            if(session.getAttribute("error") != null) {
                RequestDispatcher rd = req.getRequestDispatcher("/views/addEmployee.jsp");
                rd.forward(req, resp);
            } else {
                session.setAttribute("error", "Employee added succesfully");
                resp.sendRedirect(req.getContextPath() + "/empresa?option=listing");
            }
        } else if(option.equals("searchSalary")) {
            searchSalary(req, session);
            if(session.getAttribute("error") != null || session.getAttribute("error2") != null) {
                RequestDispatcher rd = req.getRequestDispatcher("/views/searchSalary.jsp");
                rd.forward(req, resp);
            } else {
                List<Employee> singleEmployee = new ArrayList<>();
                singleEmployee.add(searchSalary(req, session));
                session.setAttribute("Employee", singleEmployee);
                RequestDispatcher rd = req.getRequestDispatcher("/views/searchSalary.jsp");
                rd.forward(req, resp);
                singleEmployee.clear();
            }
        } else if(option.equals("searchEmployee")) {
            List<Employee> employeeList = searchEmployee(req, session);
            if(employeeList.isEmpty()) {
                RequestDispatcher rd = req.getRequestDispatcher("/views/searchEmployee.jsp");
                rd.forward(req, resp);
                session.setAttribute("error3", "No employees found");
            } else {
                session.setAttribute("EmployeeList", employeeList);
                RequestDispatcher rd = req.getRequestDispatcher("/views/searchEmployee.jsp");
                rd.forward(req, resp);
            }
            employeeList.clear();
        } else if(option.equals("edit")) {
            editEmployee(req, session);
            if(session.getAttribute("error") != null) {
                resp.sendRedirect(req.getContextPath() + "/empresa?option=editEmployee&dni=" + req.getParameter("dni"));
            } else {
                resp.sendRedirect(req.getContextPath() + "/empresa?option=listing");
            }
        }
    }

    /**
     * Create an Employee object and add it to the database.
     *
     * @param req
     * @param session
     */
    private void pushEmployee(HttpServletRequest req, HttpSession session) {

        Employee empl = new Employee();
        try {
            if(isValidParams(req, session)) {
                empl = assignParams(req);
                empleadoDaoImpl.add(empl);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Edit an employee in the database.
     *
     * @param req
     * @param session
     */
    private void editEmployee(HttpServletRequest req, HttpSession session) {

        Employee empl = new Employee();
        try {
            if(isValidParams(req, session)) {
                empl = assignParams(req);
                empleadoDaoImpl.update(empl);
            } else {
                session.setAttribute("error", "Invalid parameters");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Validate the parameters received from the form.
     *
     * @param req
     * @return True if the parameters are correct.
     */
    private boolean isValidParams(HttpServletRequest req, HttpSession session) {
        if(req.getParameter("name") == null || req.getParameter("name").isEmpty() || req.getParameter("dni") == null || req.getParameter("dni").isEmpty() || req.getParameter("category") == null || req.getParameter("category").isEmpty() || req.getParameter("workingYears") == null || req.getParameter("workingYears").isEmpty() || req.getParameter("gender") == null || req.getParameter("gender").isEmpty()) {
            session.setAttribute("error", "Invalid parameters");
            return false;
        }
        return true;
    }


    /**
     * Assign the parameters received from the form to an Employee object.
     *
     * @param req
     * @return Employee object.
     */
    private Employee assignParams(HttpServletRequest req) {
        return new Employee(req.getParameter("gender"), req.getParameter("dni"), req.getParameter("name"),
                Integer.parseInt(req.getParameter("category")), Integer.parseInt(req.getParameter("workingYears")));
    }

    /**
     * Search for an employee by DNI.
     *
     * @param req
     * @param session
     * @return Employee object if found, null otherwise.
     */
    private Employee searchSalary(HttpServletRequest req, HttpSession session) {

        Employee empl = null;
        try {
            if(isDniValid(req, session) && isEmployeeExisting(req.getParameter("dni"))) {
                empl = empleadoDaoImpl.getByDni(req.getParameter("dni"));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(empl == null) {
            session.setAttribute("error2", "Employee not found");
        }
        return empl;
    }

    /**
     * Search for an employee by any parameter.
     *
     * @param req
     * @param session
     * @return All employees that match the search.
     */
    private List<Employee> searchEmployee(HttpServletRequest req, HttpSession session) {

        List<Employee> employeeList = new ArrayList<>();
        Employee empl = new Employee();
        empl = addParams(req, empl);
        try {
            employeeList = empleadoDaoImpl.getByAnything(empl);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return employeeList;
    }

    /**
     * Check if an employee exists in the database.
     *
     * @param dni
     * @return True if the employee exists, otherwise false.
     */
    private boolean isEmployeeExisting(String dni) {

        try {
            if(empleadoDaoImpl.getByDni(dni) != null) {
                return true;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Checks and add the parameters to the Employee object.
     *
     * @param req
     * @param empl
     * @return Employee object with the parameters added.
     */
    private Employee addParams(HttpServletRequest req, Employee empl) {
        if(req.getParameter("name") != null && !req.getParameter("name").isEmpty()) {
            empl.setName(req.getParameter("name"));
        } else if(req.getParameter("dni") != null && !req.getParameter("dni").isEmpty()) {
            empl.setDni(req.getParameter("dni"));
        } else if(req.getParameter("category") != null && !req.getParameter("category").isEmpty()) {
            empl.setCategory(Integer.parseInt(req.getParameter("category")));
        } else if(req.getParameter("workingYears") != null && !req.getParameter("workingYears").isEmpty()) {
            empl.setWorkYears(Integer.parseInt(req.getParameter("workingYears")));
        } else if(req.getParameter("gender") != null && !req.getParameter("gender").isEmpty()) {
            empl.setGender(req.getParameter("gender"));
        }
        return empl;
    }

    /**
     * Check if the DNI is valid.
     *
     * @param req
     * @param session
     * @return True if all the conditions are met, false otherwise
     */
    private boolean isDniValid(HttpServletRequest req, HttpSession session) {
        if(req.getParameter("dni") == null || req.getParameter("dni").isEmpty()) {
            session.setAttribute("error", "Invalid DNI, ");
            return false;
        }
        return true;

    }

}