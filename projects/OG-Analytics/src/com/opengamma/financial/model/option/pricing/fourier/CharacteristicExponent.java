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

  public abstract double getTime();

  public abstract double getLargestAlpha();

  public abstract double getSmallestAlpha();

}
