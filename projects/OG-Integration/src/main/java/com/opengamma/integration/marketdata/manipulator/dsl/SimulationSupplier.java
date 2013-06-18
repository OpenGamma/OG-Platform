/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

/**
 * A supplier of {@link Simulation}s. It's probably easier to run simulations using the approach in {@code RunSimulation}.
 */
public interface SimulationSupplier {

  /**
   * TODO should this have parameters? tool context? command line? something else?
   * @return A simulation
   */
  Simulation get();
}
