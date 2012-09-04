/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.Position;

/**
 * Service interface for constructing a random, but reasonable, position.
 */
public interface PositionGenerator {

  /**
   * Creates a new position object. The implementing class will determine the structure and content of the position.
   * 
   * @return the new position or null if a position couldn't be generated
   */
  Position createPosition();

}
