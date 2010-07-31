/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Allows clients to determine a {@code long} value for a
 * {@link ValueSpecification} for interaction with other caching interfaces.
 */
public interface ValueSpecificationIdentifierSource {

  long getIdentifier(ValueSpecification spec);
}
