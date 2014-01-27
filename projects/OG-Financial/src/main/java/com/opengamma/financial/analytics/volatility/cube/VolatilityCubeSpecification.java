/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Specification for a volatility cube - contains all available points on the cube.
 */
@Config(description = "Volatility cube specification", group = ConfigGroups.VOL)
public class VolatilityCubeSpecification {
  private final CubeInstrumentProvider<?, ?, ?> _cubeInstrumentProvider;
  private final String _name;
  private final String _cubeQuoteType;
  private final String _quoteUnits;
  private final UniqueIdentifiable _target;
  private final ExerciseType _exerciseType;
  private final boolean _useUnderlyingSecurityForExpiry;

  public VolatilityCubeSpecification(final String name,
                                     final UniqueIdentifiable target,
                                     final String cubeQuoteType,
                                     final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider) {
    this(name, target, cubeQuoteType, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE, new EuropeanExerciseType(),
         cubeInstrumentProvider);
  }

  public VolatilityCubeSpecification(final String name,
                                     final UniqueIdentifiable target,
                                     final String cubeQuoteType,
                                     final String quoteUnits,
                                     final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider) {
    this(name, target, cubeQuoteType, quoteUnits, new EuropeanExerciseType(), cubeInstrumentProvider);
  }

  public VolatilityCubeSpecification(final String name,
                                     final UniqueIdentifiable target,
                                     final String cubeQuoteType,
                                     final String quoteUnits,
                                     final ExerciseType exerciseType,
                                     final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider) {
    this(name, target, cubeQuoteType, quoteUnits, exerciseType, cubeInstrumentProvider, false);
  }

  public VolatilityCubeSpecification(final String name,
                                     final UniqueIdentifiable target,
                                     final String cubeQuoteType,
                                     final String quoteUnits,
                                     final ExerciseType exerciseType,
                                     final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider,
                                     final boolean useUnderlyingSecurityForExpiry) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(cubeQuoteType, "cube quote type");
    ArgumentChecker.notNull(quoteUnits, "quote units");
    ArgumentChecker.notNull(exerciseType, "exerciseType");
    ArgumentChecker.notNull(cubeInstrumentProvider, "cube instrument provider");
    _name = name;
    _target = target;
    _cubeQuoteType = cubeQuoteType;
    _quoteUnits = quoteUnits;
    _exerciseType = exerciseType;
    _cubeInstrumentProvider = cubeInstrumentProvider;
    _useUnderlyingSecurityForExpiry = useUnderlyingSecurityForExpiry;
  }

  public CubeInstrumentProvider<?, ?, ?> getCubeInstrumentProvider() {
    return _cubeInstrumentProvider;
  }

  public String getName() {
    return _name;
  }

  public String getCubeQuoteType() {
    return _cubeQuoteType;
  }

  public String getQuoteUnits() {
    return _quoteUnits;
  }

  public boolean isUseUnderlyingSecurityForExpiry() {
    return _useUnderlyingSecurityForExpiry;
  }

  /**
   * @deprecated use getTarget()
   * @throws ClassCastException if target not a currency
   * @return currency assuming that the target is a currency
   */
  @Deprecated
  public Currency getCurrency() {
    return (Currency) _target;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilityCubeSpecification)) {
      return false;
    }
    final VolatilityCubeSpecification other = (VolatilityCubeSpecification) o;
    return other.getName().equals(getName()) &&
        other.getTarget().equals(getTarget()) &&
        other.getCubeInstrumentProvider().equals(getCubeInstrumentProvider()) &&
        other.getCubeQuoteType().equals(getCubeQuoteType()) &&
        other.getExerciseType().equals(getExerciseType()) &&
        other.getQuoteUnits().equals(getQuoteUnits()) &&
        other.isUseUnderlyingSecurityForExpiry() == isUseUnderlyingSecurityForExpiry();
  }

  @Override
  public int hashCode() {
    return getName().hashCode() * getTarget().hashCode() * getCubeQuoteType().hashCode() * getQuoteUnits().hashCode() * getExerciseType().hashCode();
  }


}
