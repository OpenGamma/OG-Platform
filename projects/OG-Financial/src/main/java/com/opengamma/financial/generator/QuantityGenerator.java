/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;

/**
 * Service interface for constructing a random, but reasonable, quantity.
 */
public interface QuantityGenerator {

  /**
   * Creates a new quantity value.
   * 
   * @return the new quantity, or null if no quantity can be generated
   */
  BigDecimal createQuantity();

}
