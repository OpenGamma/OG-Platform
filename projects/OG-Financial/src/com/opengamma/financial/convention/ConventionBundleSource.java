/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Simple to use interface to a ReferenceRateMaster
 */
public interface ConventionBundleSource {
  ConventionBundle getConventionBundle(Identifier identifier);
  ConventionBundle getConventionBundle(IdentifierBundle identifiers);
  ConventionBundle getConventionBundle(UniqueIdentifier identifier);
}
