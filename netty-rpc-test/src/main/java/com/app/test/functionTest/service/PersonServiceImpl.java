package com.app.test.functionTest.service;

import com.netty.rpc.service.annotation.NettyRpcService;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OpensourceHU on 2021-03-10.
 */
@NettyRpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

  @Override
  public List<Person> callPerson(String name, Integer num) {
    List<Person> persons = new ArrayList<>(num);
    for (int i = 0; i < num; ++i) {
      persons.add(new Person(Integer.toString(i), name));
    }
    return persons;
  }
}
