/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;

/**
 * The default set of conventions that have been hard-coded.
 */
public class DefaultConventionMasterInitializer extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new DefaultConventionMasterInitializer();

  /**
   * Creates an {@code InMemoryConventionMaster} populated with default hard-coded conventions.
   * 
   * @return the populated master, not null
   */
  public static InMemoryConventionMaster createPopulated() {
    InMemoryConventionMaster master = new InMemoryConventionMaster();
    DefaultConventionMasterInitializer.INSTANCE.init(master);
    return master;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  protected DefaultConventionMasterInitializer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    AUConventions.INSTANCE.init(master);
    CAConventions.INSTANCE.init(master);
    CHConventions.INSTANCE.init(master);
    EUConventions.INSTANCE.init(master);
    GBConventions.INSTANCE.init(master);
    JPConventions.INSTANCE.init(master);
    KRConventions.INSTANCE.init(master);
    USConventions.INSTANCE.init(master);
    USFXConventions.INSTANCE.init(master);
    ZAConventions.INSTANCE.init(master);
  }

}
