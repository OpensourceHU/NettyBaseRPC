package com.app.test.functionTest.service;

import java.util.List;

/**
 * Created by OpensourceHU on 2021-03-10.
 */
public interface PersonService {

  List<Person> callPerson(String name, Integer num);
}
