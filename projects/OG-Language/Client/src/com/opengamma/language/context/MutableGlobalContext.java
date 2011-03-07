/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * A mutable version of {@link GlobalContext}.
 */
public class MutableGlobalContext extends GlobalContext {

  /* package */MutableGlobalContext() {
  }
  
  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}
