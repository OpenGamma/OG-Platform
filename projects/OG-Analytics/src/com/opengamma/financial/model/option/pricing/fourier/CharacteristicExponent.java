/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public abstract class CharacteristicExponent extends Function1D<ComplexNumber, ComplexNumber> {
  /** Defining <it>i</it>*/
  protected static final ComplexNumber I = new ComplexNumber(0, 1);
  /** Defining <it>-i</it>*/
  protected static final ComplexNumber MINUS_I = new ComplexNumber(0, -1);

  public abstract double getTime();

  public abstract double getLargestAlpha();

  public abstract double getSmallestAlpha();

  // public abstract ComplexNumber evaluate(final ComplexNumber u, final double t);

}
