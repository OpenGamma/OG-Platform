/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.ratingtransitionmodel;

/**
 * Class to hold a matrix giving the transition threshold probabilities for migrations between different rating states
 */
public class RatingTransitionMatrix {

  private final int _numberOfRatingStates;

  private final double[][] _ratingTransitionMatrix;

  public RatingTransitionMatrix(final int numberOfRatingStates, final double[][] ratingTransitionMatrix) {

    _numberOfRatingStates = numberOfRatingStates;

    _ratingTransitionMatrix = ratingTransitionMatrix;
  }

  public int getNumberOfRatingStates() {
    return _numberOfRatingStates;
  }

  public double[][] getRatingTransitionMatrix() {
    return _ratingTransitionMatrix;
  }

}
