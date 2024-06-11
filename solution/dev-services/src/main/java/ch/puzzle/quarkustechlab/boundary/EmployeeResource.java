package ch.puzzle.quarkustechlab.boundary;

import ch.puzzle.quarkustechlab.entity.Employee;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/employee")
public class EmployeeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> findAll() {
        return Employee.findAll().list();
    }
}