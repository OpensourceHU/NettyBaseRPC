package com.app.test.mock_util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;

public class StrGenerator {

  static int len;

  static String type;

  static int size;

  static Random random = new Random();

  public StrGenerator(int len, String type, int size) {
    this.len = len;
    this.type = type;
    this.size = size;
  }

  public List<String> gen() {
    ArrayList<String> ret = new ArrayList<>(size);
    switch (type) {
      case "KB":
        for (int i = 0; i < size; i++) {
          ret.add(RandomStringUtils.randomAlphabetic(len * 1024));
        }
        break;
      case "B":
        for (int i = 0; i < size; i++) {
          ret.add(RandomStringUtils.randomAlphabetic(len));
        }
    }
    return ret;
  }

//  public static void main(String[] args) {
//    StrGenerator strGenerator = new StrGenerator(10, "KB", 1000);
//    List<String> list = strGenerator.gen();
//
//    System.out.println(list.toString());
//  }
}
