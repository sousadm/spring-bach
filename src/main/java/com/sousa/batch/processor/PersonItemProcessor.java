package com.sousa.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.sousa.batch.model.Person;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private int contador = 0;

    @Override
    public Person process(@NonNull Person person) {
        contador++;

        // Verifica se é o décimo item
        if (contador % 10 == 0) {
            log.warn("Ignorando o item: {}", person.getNome());
            return null; // Retornar null faz o Spring Batch descartar o item
        }

        log.info("Importando: {}", person.getNome());
        person.setNome(person.getNome().toUpperCase().concat(" - teste"));
        return person;
    }

}