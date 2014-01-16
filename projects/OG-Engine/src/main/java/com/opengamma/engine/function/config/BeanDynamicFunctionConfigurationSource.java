/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.VersionCorrection;

/**
 * Wrapper for a {@link AbstractFunctionConfigurationBean} that supports dynamic configurations by recreating the bean for each version timestamp.
 */
public abstract class BeanDynamicFunctionConfigurationSource extends DynamicFunctionConfigurationSource {

  public BeanDynamicFunctionConfigurationSource(final ChangeManager underlying) {
    super(underlying);
  }

  public BeanDynamicFunctionConfigurationSource(final ChangeProvider underlying) {
    super(underlying);
  }

  protected abstract VersionedFunctionConfigurationBean createConfiguration();

  @Override
  protected FunctionConfigurationBundle getFunctionConfiguration(final VersionCorrection version) {
    final VersionedFunctionConfigurationBean factory = createConfiguration();
    factory.setVersionCorrection(version);
    return factory.createObject().getFunctionConfiguration(version.getCorrectedTo());
  }

}
