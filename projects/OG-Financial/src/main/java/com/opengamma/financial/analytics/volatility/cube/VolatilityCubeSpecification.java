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

  private final String _name;
  private final String _cubeQuoteType;
  private final String _quoteUnits;
  private final UniqueIdentifiable _target;
  private final ExerciseType _exerciseType;

  public VolatilityCubeSpecification(final String name,
      final UniqueIdentifiable target,
      final String cubeQuoteType) {
    this(name, target, cubeQuoteType, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE, new EuropeanExerciseType());
  }

  public VolatilityCubeSpecification(final String name,
      final UniqueIdentifiable target,
      final String cubeQuoteType,
      final String quoteUnits) {
    this(name, target, cubeQuoteType, quoteUnits, new EuropeanExerciseType());
  }

  public VolatilityCubeSpecification(final String name,
      final UniqueIdentifiable target,
      final String cubeQuoteType,
      final String quoteUnits,
      final ExerciseType exerciseType) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(cubeQuoteType, "cube quote type");
    ArgumentChecker.notNull(quoteUnits, "quote units");
    ArgumentChecker.notNull(exerciseType, "exerciseType");
    _name = name;
    _target = target;
    _cubeQuoteType = cubeQuoteType;
    _quoteUnits = quoteUnits;
    _exerciseType = exerciseType;
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
        other.getCubeQuoteType().equals(getCubeQuoteType()) &&
        other.getExerciseType().equals(getExerciseType()) &&
        other.getQuoteUnits().equals(getQuoteUnits());
  }

  @Override
  public int hashCode() {
    return getName().hashCode() * getTarget().hashCode() * getCubeQuoteType().hashCode() * getQuoteUnits().hashCode() * getExerciseType().hashCode();
  }

}
