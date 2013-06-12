/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.google.common.base.Supplier;

/**
 * A supplier of {@link Simulation}s.
 */
public interface SimulationSupplier extends Supplier<Simulation> {

  /**
   * @return A simulation
   */
  @Override
  Simulation get();
}
