/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import com.opengamma.analytics.math.random.RandomNumberGenerator;

/**
 * Generic Monte-Carlo pricing method.
 */
public abstract class MonteCarloMethod {

  /**
   * The random number generator.
   */
  private final RandomNumberGenerator _numberGenerator;
  /**
   * The number of paths.
   */
  private final int _nbPath;

  /**
   * Constructor.
   * @param numberGenerator The random number generator.
   * @param nbPath The number of paths.
   */
  public MonteCarloMethod(RandomNumberGenerator numberGenerator, int nbPath) {
    _numberGenerator = numberGenerator;
    _nbPath = nbPath;
  }

  /**
   * Gets the _numberGenerator field.
   * @return the _numberGenerator
   */
  public RandomNumberGenerator getNumberGenerator() {
    return _numberGenerator;
  }

  /**
   * Gets the _nbPath field.
   * @return the _nbPath
   */
  public int getNbPath() {
    return _nbPath;
  }

}
