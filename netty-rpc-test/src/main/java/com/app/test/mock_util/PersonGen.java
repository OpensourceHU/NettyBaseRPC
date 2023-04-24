package com.app.test.mock_util;

import com.app.test.functionTest.service.Person;

public class PersonGen {


  public static Person genPerson(int size) {
    StrGenerator strGenerator = new StrGenerator(size / 2, "KB", 1);
    if (size >= 0) {
      //生成一共size大小的 KB的姓与名
      String fi = strGenerator.gen().get(0);
      String se = strGenerator.gen().get(0);
      Person ret = new Person(fi, se);
      return ret;
    }
    return null;
  }

  public static void main(String[] args) {
    Person person = genPerson(20);
    System.out.println(person);
  }

}
