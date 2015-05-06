/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.sensitivity;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the present value Black volatility sensitivity.
 */
public class PresentValueSwaptionSurfaceSensitivity {

  /**
   * The object containing the volatility sensitivity. The sensitivity is in the currency of the swap generator. Not null.
   */
  private final SurfaceValue _sensitivity;
  /**
   * The standard swap generator (in particular fixed leg convention and floating leg tenor) for which the 
   * volatility surface is valid. Not null.
   */
  private final GeneratorInstrument<GeneratorAttributeIR> _generatorSwap;

  /**
   * Constructor with empty sensitivity.
   * @param generatorSwap The standard swap generator for which the volatility surface is valid.
   */
  public PresentValueSwaptionSurfaceSensitivity(final GeneratorInstrument<GeneratorAttributeIR> generatorSwap) {
    ArgumentChecker.notNull(generatorSwap, "Swap generator");
    _sensitivity = new SurfaceValue();
    _generatorSwap = generatorSwap;
  }

  /**
   * Constructor from parameter sensitivities.
   * @param sensitivity The volatility sensitivity as a map.
   * @param generatorSwap The standard swap generator for which the volatility surface is valid.
   */
  public PresentValueSwaptionSurfaceSensitivity(final Map<DoublesPair, Double> sensitivity, 
      final GeneratorInstrument<GeneratorAttributeIR> generatorSwap) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(generatorSwap, "Swap generator");
    _sensitivity = SurfaceValue.from(sensitivity);
    _generatorSwap = generatorSwap;
  }

  /**
   * Constructor from parameter sensitivities. The SurfaceValue are not copied but used directly.
   * @param sensitivity The volatility sensitivity as a SurfaceValue.
   * @param generatorSwap The standard swap generator for which the volatility surface is valid.
   */
  public PresentValueSwaptionSurfaceSensitivity(final SurfaceValue sensitivity, 
      final GeneratorInstrument<GeneratorAttributeIR> generatorSwap) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(generatorSwap, "Swap generator");
    _sensitivity = sensitivity;
    _generatorSwap = generatorSwap;
  }

  /**
   * Add one sensitivity to the volatility sensitivity. The existing object is modified. If the point is not in the existing points of the sensitivity, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param expiryMaturity The expiration time/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addSensitivity(final DoublesPair expiryMaturity, final double sensitivity) {
    _sensitivity.add(expiryMaturity, sensitivity);
  }

  /**
   * Create a new sensitivity object with all the sensitivities multiplied by a common factor.
   * @param sensi The Black sensitivity.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public static PresentValueSwaptionSurfaceSensitivity multiplyBy(final PresentValueSwaptionSurfaceSensitivity sensi, 
      final double factor) {
    return new PresentValueSwaptionSurfaceSensitivity(SurfaceValue.multiplyBy(sensi._sensitivity, factor), sensi._generatorSwap);
  }

  /**
   * Return the sum of to sensitivities in a new one. The original sensitivities are unchanged. The associated swap generators should be identical.
   * @param sensi1 The first Black sensitivity.
   * @param sensi2 The second Black sensitivity.
   * @return The sum sensitivity.
   */
  public static PresentValueSwaptionSurfaceSensitivity plus(final PresentValueSwaptionSurfaceSensitivity sensi1, 
      final PresentValueSwaptionSurfaceSensitivity sensi2) {
    ArgumentChecker.isTrue(sensi1._generatorSwap.equals(sensi2._generatorSwap), "Swap generators should be equal to add sensitivities");
    return new PresentValueSwaptionSurfaceSensitivity(SurfaceValue.plus(sensi1._sensitivity, sensi2._sensitivity), sensi1._generatorSwap);
  }

  /**
   * Gets the volatility sensitivity.
   * @return The sensitivity.
   */
  public SurfaceValue getSensitivity() {
    return _sensitivity;
  }

  /**
   * Gets the standard swap generator for which the volatility surface is valid.
   * @return The generator.
   */
  public GeneratorInstrument<GeneratorAttributeIR> getGeneratorSwap() {
    return _generatorSwap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _generatorSwap.hashCode();
    result = prime * result + _sensitivity.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PresentValueSwaptionSurfaceSensitivity other = (PresentValueSwaptionSurfaceSensitivity) obj;
    if (!ObjectUtils.equals(_generatorSwap, other._generatorSwap)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
