/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.component;

import com.opengamma.component.factory.source.ConventionBundleSourceComponentFactory;
import com.opengamma.examples.convention.SyntheticInMemoryConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMaster;

/**
 * Component factory for the Example-specific convention bundle source.
 */
public class ExampleConventionBundleSourceComponentFactory extends ConventionBundleSourceComponentFactory {

  @Override
  protected ConventionBundleMaster getOrCreateConventionBundleMaster() {
    return new SyntheticInMemoryConventionBundleMaster();
  }

}
