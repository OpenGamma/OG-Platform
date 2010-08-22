/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Simple to use interface to a ReferenceRateMaster
 */
public interface ReferenceRateSource {
  ReferenceRate getSingleReferenceRate(Identifier identifier);
  ReferenceRate getSingleReferenceRate(IdentifierBundle identifiers);
  ReferenceRate getReferenceRate(UniqueIdentifier identifier);
}
