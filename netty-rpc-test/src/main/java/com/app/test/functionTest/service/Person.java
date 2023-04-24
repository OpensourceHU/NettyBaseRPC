package com.app.test.functionTest.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class Person implements Serializable {

  private static final long serialVersionUID = -3475626311941868983L;
  private String firstName;
  private String lastName;

  private Collection<Person> next;

  public Person(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Collection<Person> getNext() {
    return next;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setNext(List<Person> next) {
    this.next = next;
  }

  @Override
  public String toString() {
    return "Person{" +
        "firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", next=" + next +
        '}';
  }
}
