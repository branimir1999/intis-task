package org.ivic.intis;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import org.ivic.intis.database.User;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {

    @POST
    @Transactional
    public Response create(@Valid UserService user) {
        if (user.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        user.persist();
        return Response.ok(user).status(201).build();
    }

    @GET
    @Path("/{id}")
    public User getSingle(@PathParam("id") Long id) {
        return User.findById(id);
    }

    @GET
    public List<User> getAll() {
        return User.listAll();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public User update(@PathParam("id") Long id, @Valid User user) {
        if (user.getFirstName() == null  || user.getLastName() == null || user.getEmail() == null) {
            throw new WebApplicationException("User params were not set on request.", 422);
        }

        User entity = User.findById(id);

        if (entity == null) {
            throw new WebApplicationException("User with id of " + id + " does not exist.", 404);
        }

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());

        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        User entity = User.findById(id);
        if (entity == null) {
            throw new WebApplicationException("User with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
}