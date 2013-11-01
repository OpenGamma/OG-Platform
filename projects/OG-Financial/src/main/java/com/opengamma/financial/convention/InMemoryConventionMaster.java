/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;

/**
 * In-memory convention master.
 *
 * Versioning is *NOT* supported.
 * Attempting a version correction will throw an exception.
 * Only VersionCorrection.LATEST is supported for retrieval.
 */
public class InMemoryConventionMaster extends com.opengamma.master.convention.impl.InMemoryConventionMaster {

  /**
   * Initializes the conventions.
   */
  public InMemoryConventionMaster() {
    init();
  }

  /**
   * Initializes the convention master.
   */
  protected void init() {
    ConventionMasterInitializer.initDefaultConventions(this);
  }

}
