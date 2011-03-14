/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Properties;

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

  public void setSystemSettings(final Properties properties) {
    setValue(SYSTEM_SETTINGS, properties);
  }

}
