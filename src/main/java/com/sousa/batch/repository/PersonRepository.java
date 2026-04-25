package com.sousa.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sousa.batch.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
