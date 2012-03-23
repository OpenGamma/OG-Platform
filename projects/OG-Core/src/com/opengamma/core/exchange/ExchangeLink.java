/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import com.opengamma.core.Link;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between an object and an exchange.
 * <p>
 * The exchange link represents a connection from an entity to an exchange.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * To obtain the target exchange, the link must be resolved.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface ExchangeLink extends Link<Exchange> {

}
