package com.sousa.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.sousa.batch.model.Person;

@Component
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) {
        person.setNome(person.getNome().toUpperCase().concat(" - teste"));
        return person;
    }

}