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
public interface CharacteristicExponent1 {

  Function1D<ComplexNumber, ComplexNumber> getFunction(double t);

  double getLargestAlpha();

  double getSmallestAlpha();

}
