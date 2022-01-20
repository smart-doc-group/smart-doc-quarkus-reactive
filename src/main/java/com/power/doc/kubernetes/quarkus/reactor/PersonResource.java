package com.power.doc.kubernetes.quarkus.reactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.power.doc.kubernetes.quarkus.exception.PersonNotFoundException;
import com.power.doc.kubernetes.quarkus.model.Person;
import com.power.doc.kubernetes.quarkus.repository.PersonRepository;
import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;

import static javax.ws.rs.core.Response.Status.CREATED;

@Path("persons")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class PersonResource {

    @Inject
    private PersonRepository personRepository;


    @GET
    public Uni<List<Person>> get() {
        return Uni.createFrom().item(personRepository.findAll());
    }

    @GET
    @Path("{id}")
    public Uni<Person> getSingle(@RestPath Long id) {
        return
                Uni.createFrom().item(personRepository.findById(id).get());
    }

    @GET
    @Path("/multi")
    public Multi<List<Person>> findAll() {
        return Multi.createFrom().items(personRepository.findAll());
    }

    @POST
    public Uni<Response> create(Person person) {
        return Uni.createFrom().item(Response.ok(personRepository.save(person)).status(CREATED)::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@RestPath Long id, Person personDetails) {
        Person person = personRepository.findById(id).
                orElseThrow(() -> new PersonNotFoundException(id));
        person.setEmail(personDetails.getEmail());
        person.setLastName(personDetails.getLastName());
        person.setFirstName(personDetails.getFirstName());
        return Uni.createFrom().item(Response.ok(personRepository.save(person)).status(CREATED)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@RestPath Long id) {
        Person person = personRepository.findById(id).
                orElseThrow(() -> new PersonNotFoundException(id));
        return Uni.createFrom().item(Response.ok(personRepository.delete(person)).status(CREATED)::build);
    }

    /**
     * Create a HTTP response from an exception.
     * <p>
     * Response Example:
     *
     * <pre>
     * HTTP/1.1 422 Unprocessable Entity
     * Content-Length: 111
     * Content-Type: application/json
     *
     * {
     *     "code": 422,
     *     "error": "Fruit name was not set on request.",
     *     "exceptionType": "javax.ws.rs.WebApplicationException"
     * }
     * </pre>
     */
    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {

            Throwable throwable = exception;

            int code = 500;
            if (throwable instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            // This is a Mutiny exception and it happens, for example, when we try to insert a new
            // fruit but the name is already in the database
            if (throwable instanceof CompositeException) {
                throwable = ((CompositeException) throwable).getCause();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", throwable.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", throwable.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
