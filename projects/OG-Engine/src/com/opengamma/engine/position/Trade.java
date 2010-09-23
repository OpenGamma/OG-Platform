/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * A position of a single trade against a particular {@code Counterparty}.
 */
public interface Trade extends Position {

  Counterparty getCounterparty();

}
