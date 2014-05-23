/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory bean template for creating a simple {@link FunctionConfigurationSource} which returns a static configuration for a given version/correction timestamp.
 */
public abstract class VersionedFunctionConfigurationBean extends AbstractFunctionConfigurationBean {

  private VersionCorrection _versionCorrection = VersionCorrection.LATEST;

  public void setVersionCorrection(final VersionCorrection versionCorrection) {
    _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

}
