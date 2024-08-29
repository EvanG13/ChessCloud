package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ValidateObjectTest {

  private static class TestClass {
    private int fooInt;
    private String fooStr;

    public TestClass(int fooInt, String fooStr) {
      this.fooInt = fooInt;
      this.fooStr = fooStr;
    }
  }

  @Test
  public void checkRequireNull() {
    TestClass testClass = new TestClass(0, null);

    assertThrows(
        NullPointerException.class,
        () -> {
          ValidateObject.requireNonNull(testClass);
        });
  }

  @Test
  public void isAnyFieldNull() {
    TestClass testClass = new TestClass(0, null);

    assertTrue(ValidateObject.isAnyFieldInObjectNull(testClass));
  }
}
