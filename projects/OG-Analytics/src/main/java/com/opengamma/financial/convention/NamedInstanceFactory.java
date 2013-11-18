/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.List;

/**
 * Interface for factories that can create instances from names.
 * @param <T> type of objects returned
 */
public interface NamedInstanceFactory<T extends NamedInstance> {
  /* 
  static T of(String name)
   */
  /**
   * Gets an unmodifiable list of supported name values
   * @return unmodifiable list of supported name values, not null
   */
  List<T> values();
}
