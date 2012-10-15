/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Class to hold a matrix giving the transition threshold probabilities for migrations between different rating states
 */
public class RatingTransitionMatrix {

  private final int _numberOfRatingStates;

  public RatingTransitionMatrix(final int numberOfRatingStates) {

    _numberOfRatingStates = numberOfRatingStates;
  }

  public int getNumberOfRatingStates() {
    return _numberOfRatingStates;
  }

}
