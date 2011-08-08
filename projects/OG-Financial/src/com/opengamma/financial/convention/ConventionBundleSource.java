/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Simple to use interface to a ReferenceRateMaster
 */
public interface ConventionBundleSource {
  ConventionBundle getConventionBundle(ExternalId identifier);
  ConventionBundle getConventionBundle(ExternalIdBundle identifiers);
  ConventionBundle getConventionBundle(UniqueId identifier);
}
