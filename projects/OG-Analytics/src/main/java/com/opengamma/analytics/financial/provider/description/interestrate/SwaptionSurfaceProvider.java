/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Interface for swaption provider with parameters represented by a surface.
 */
public interface SwaptionSurfaceProvider {

  /**
   * Returns the surface describing the model parameters.
   * @return The surface
   */
  Surface<Double, Double, Double> getParameterSurface();

  /**
   * Returns the swap generator for which the parameters are valid, 
   * i.e. the data is calibrated to swaption on vanilla swaps with conventions as described in the generator.
   * @return The generator.
   */
  GeneratorInstrument<GeneratorAttributeIR> getGeneratorSwap();

}
