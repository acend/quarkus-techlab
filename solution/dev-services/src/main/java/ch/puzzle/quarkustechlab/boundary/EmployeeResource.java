package ch.puzzle.quarkustechlab.boundary;

import ch.puzzle.quarkustechlab.entity.Employee;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/employee")
public class EmployeeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> findAll() {
        return Employee.findAll().list();
    }
}