/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

/**
 * 
 */
public class GaussianMartingaleCharacteristicExponent extends MeanCorrection {
  public GaussianMartingaleCharacteristicExponent(final double sigma) {
    super(new GaussianCharacteristicExponent(0, sigma));
  }
}
