/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

/**
 * Wrapper marking instances of inner classes to fudge automatically.
 * 
 * @param <T> the type
 */
public class AutoFudgable<T> {

  private T _object;

  public T object() {
    return _object;
  }

  public AutoFudgable(T object) {
    _object = object;
  }

  public static <T> AutoFudgable<T> autoFudge(T object) {
    return new AutoFudgable<T>(object);
  }

}
