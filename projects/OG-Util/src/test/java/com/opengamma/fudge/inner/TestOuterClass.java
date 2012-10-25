/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

import java.util.Arrays;

public class TestOuterClass {
  int fieldA = 5;
  double fieldB = 5;
  int[] fieldC;

/*    TestOuterClass(int fieldA) {
      this.fieldA = fieldA;
    }

    TestOuterClass(int fieldA, int fieldB, int[] fieldC) {
      this.fieldA = fieldA;
      this.fieldB = fieldB;
      this.fieldC = fieldC;
    }*/

  public TestOuterClass() {

  }

  /**
   * Default dummy implementation of eval = identity
   * @param arg the arguemnt
   * @return the result
   */
  public double eval(double arg) {
    return arg;
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TestOuterClass)) return false;

    TestOuterClass that = (TestOuterClass) o;

    if (fieldA != that.fieldA) return false;
    if (Double.compare(that.fieldB, fieldB) != 0) return false;
    if (!Arrays.equals(fieldC, that.fieldC)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = fieldA;
    temp = fieldB != +0.0d ? Double.doubleToLongBits(fieldB) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (fieldC != null ? Arrays.hashCode(fieldC) : 0);
    return result;
  }
}
