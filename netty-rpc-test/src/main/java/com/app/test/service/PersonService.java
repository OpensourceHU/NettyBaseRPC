package com.app.test.service;

import java.util.List;

/**
 * Created by OpensourceHU on 2020-03-10.
 */
public interface PersonService {
    List<Person> callPerson(String name, Integer num);
}