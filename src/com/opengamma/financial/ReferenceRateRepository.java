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
 * Repository for rates and associated metadata - e.g. LIBOR/EURIBOR etc...
 */
public interface ReferenceRateRepository {
  ReferenceRate getReferenceRate(IdentifierBundle bundle);
  ReferenceRate getReferenceRate(Identifier identifier);
  ReferenceRate getReferenceRate(UniqueIdentifier uniqueIdentifier);
}
