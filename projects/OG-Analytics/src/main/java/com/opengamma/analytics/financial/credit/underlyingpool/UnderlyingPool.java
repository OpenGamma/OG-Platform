/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to specify the composition and characteristics of a 'pool' of obligors
 * In the credit index context the underlying pool is the set of obligors that constitute the index
 */
public abstract class UnderlyingPool {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work-in-Progress

  // TODO : Will want to include calculations such as e.g. average T year spread of constituents and other descriptive statistics
  // TODO : The standard index pools will be classes that derive from this one

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The number of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the pool of obligor objects

  public UnderlyingPool(int numberOfObligors) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNegative(numberOfObligors, "Number of obligors");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _numberOfObligors = numberOfObligors;

    Obligor[] _obligors = new Obligor[_numberOfObligors];
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
