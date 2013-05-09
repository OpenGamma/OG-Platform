/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.functional;

import static org.testng.Assert.fail;

import java.util.Iterator;

public class EqualityUtil {

  static String format(Object actual, Object expected, String message) {
    String formatted = "";
    if (null != message) {
      formatted = message + " ";
    }

    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  public static void assertEqualRecursive(Object actual, Object expected) {
    if (!equal(actual, expected)) {
      fail(format(actual, expected, null));
    }
  }


  public static boolean equal(Object o1, Object o2) {
    Iterator i1 = null;
    Iterator i2 = null;

    if (o1 instanceof Iterator) {
      i1 = (Iterator) o1;
    } else if (o1 instanceof Iterable) {
      i1 = ((Iterable) o1).iterator();
    }
    if (o2 instanceof Iterator) {
      i2 = (Iterator) o2;
    } else if (o2 instanceof Iterable) {
      i2 = ((Iterable) o2).iterator();
    }

    if (i1 != null && i2 != null) {
      return equal(i1, i2);
    } else {
      if (o1 == null && o2 == null) {
        return true;
      } else if (o1 != null && o2 != null) {
        return o1.equals(o2) || o2.equals(o1);
      } else {
        return false;
      }
    }
  }

  public static boolean equal(Iterable it1, Iterable it2) {
    Iterator i1 = it1.iterator();
    Iterator i2 = it2.iterator();
    return equal(i1, i2);
  }

  public static boolean equal(Iterator i1, Iterator i2) {
    if (i1 == i2) return true;
    if ((i1.hasNext() && !i2.hasNext()) || (!i1.hasNext() && i2.hasNext())) {
      return false;
    }
    while (i1.hasNext() && i2.hasNext()) {
      Object e1 = i1.next();
      Object e2 = i2.next();
      if (e1 != e2 && !equal(e1, e2))
        return false;
      if ((i2.hasNext() && !i1.hasNext()) || (!i2.hasNext() && i1.hasNext()))
        return false;
    }
    return true;
  }
}
