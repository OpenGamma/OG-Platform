/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;


/**
 * Represents a single trade against a particular {@link Counterparty}.
 *
 * @author kirk
 */
public interface Trade extends Position {

  Counterparty getCounterparty();
}
