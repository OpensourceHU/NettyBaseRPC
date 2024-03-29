package com.app.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.app.test.functionTest.service.HelloService;
import com.app.test.functionTest.service.Person;
import com.app.test.functionTest.service.PersonService;
import com.app.test.mock_util.PersonGen;
import com.netty.rpc.client.RpcClient;
import com.netty.rpc.client.core.handler.RpcFuture;
import com.netty.rpc.client.core.proxy.RpcService;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class ServiceTest {

  @Autowired
  private RpcClient rpcClient;

  @After
  public void stop() {
    rpcClient.stop();
  }

  @Test
  public void helloTest1() {
    HelloService helloService = rpcClient.createService(HelloService.class, "1.0");
    String result = helloService.hello("World");
    Assert.assertEquals("Hello World", result);
  }

  @Test
  public void helloTest2() {
    HelloService helloService = rpcClient.createService(HelloService.class, "2.0");
    Person person = new Person("Yong", "Huang");
    String result = helloService.hello(person);
    Assert.assertEquals("Hi Yong Huang", result);
  }

  @Test
  public void helloPersonTest() {
    PersonService personService = rpcClient.createService(PersonService.class, "");
    Integer num = 5;
    List<Person> persons = personService.callPerson("jerry", num);
    List<Person> expectedPersons = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      expectedPersons.add(new Person(Integer.toString(i), "jerry"));
    }
    assertThat(persons, equalTo(expectedPersons));

    for (int i = 0; i < persons.size(); ++i) {
      System.out.println(persons.get(i));
    }
  }

  @Test
  public void helloFutureTest1() throws Exception {
    RpcService helloService = rpcClient.createAsyncService(HelloService.class, "1.0");
    RpcFuture result = helloService.call("hello", "World");
    Assert.assertEquals("Hello World", result.get());
  }

  @Test
  public void helloFutureTest2() throws Exception {
    RpcService helloService = rpcClient.createAsyncService(HelloService.class, "1.0");
//    Person person = new Person("Yong", "Huang");
//    Person person2 = new Person("Yong", "Huang");
//    person2.setNext(List.of(person2));
    Person person = PersonGen.genPerson(2);
    RpcFuture result = helloService.call("hello", person);
  }

  @Test
  public void helloPersonFutureTest1() throws Exception {
    RpcService helloPersonService = rpcClient.createAsyncService(PersonService.class, "");
    Integer num = 5;
    RpcFuture result = helloPersonService.call("callPerson", "jerry", num);
    List<Person> persons = (List<Person>) result.get();
    List<Person> expectedPersons = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      expectedPersons.add(new Person(Integer.toString(i), "jerry"));
    }
    assertThat(persons, equalTo(expectedPersons));

    for (int i = 0; i < num; ++i) {
      System.out.println(persons.get(i));
    }
  }

}
