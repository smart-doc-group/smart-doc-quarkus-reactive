package com.power.doc.kubernetes.quarkus.repository;

import com.power.doc.kubernetes.quarkus.model.Person;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yu 2021/7/13.
 */
@ApplicationScoped
public class PersonRepository {

    private static Map<Long, Person> persons = new ConcurrentHashMap<>();

    static {
        Person person1 = new Person();
        person1.setEmail("xx@gmail.com");
        person1.setFirstName("Carrot");
        person1.setLastName("Zucchini");
        person1.setId(1);
        persons.put(person1.getId(), person1);

        Person person2 = new Person();
        person2.setEmail("aa@gmail.com");
        person2.setFirstName("Jack");
        person2.setLastName("Neo");
        person2.setId(2);
        persons.put(person2.getId(), person2);
    }


    public Optional<Person> findById(long id) {
        return Optional.ofNullable(persons.get(id));
    }

    public Person save(Person person) {
        persons.put(person.getId(), person);
        return persons.get(person.getId());
    }

    public void add(Person person) {
        persons.put(person.getId(), person);
    }

    public List<Person> findAll() {
        return persons.values().stream().collect(Collectors.toList());
    }

    public boolean delete(Person person) {
        return persons.remove(person.getId(), person);
    }
}
