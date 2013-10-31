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
public interface NamedInstance {
  /**
   * @return the name of this instance
   */
  String getName();
}
