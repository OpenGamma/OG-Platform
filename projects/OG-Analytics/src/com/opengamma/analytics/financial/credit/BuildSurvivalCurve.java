/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Helper class for calibrating the survival curve - Work-In-Progress
 */
public class BuildSurvivalCurve {

  public double[][] buildSurvivalCurve(double[] tenors, double[] hazardRates) {

    int numberOfTenors = tenors.length;

    double[][] survivalCurve = new double[numberOfTenors][2];

    return survivalCurve;
  }
}
