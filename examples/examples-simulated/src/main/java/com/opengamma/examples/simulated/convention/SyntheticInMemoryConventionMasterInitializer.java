/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.financial.convention.initializer.USFXConventions;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;

/**
 * The default set of conventions for examples-simulated that have been hard-coded.
 */
public class SyntheticInMemoryConventionMasterInitializer extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new SyntheticInMemoryConventionMasterInitializer();

  /**
   * Creates an {@code InMemoryConventionMaster} populated with default hard-coded conventions.
   * 
   * @return the populated master, not null
   */
  public static InMemoryConventionMaster createPopulated() {
    final InMemoryConventionMaster master = new InMemoryConventionMaster();
    SyntheticInMemoryConventionMasterInitializer.INSTANCE.init(master);
    return master;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  protected SyntheticInMemoryConventionMasterInitializer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    USFXConventions.INSTANCE.init(master);
    ExampleUSConventions.INSTANCE.init(master);
  }
}
