/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * {@link ConventionBundleSource} is effectively deprecated and shouldn't be
 * used. A no op version is provided here to meet legacy interface requirements
 * where one is required in order to construct an object. However it should 
 * never be called for any calculations.
 */
class NoOpConventionBundleSource implements ConventionBundleSource {

  @Override
  public ConventionBundle getConventionBundle(ExternalId identifier) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ConventionBundle getConventionBundle(ExternalIdBundle identifiers) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ConventionBundle getConventionBundle(UniqueId identifier) {
    throw new UnsupportedOperationException("Not implemented");
  }

}
