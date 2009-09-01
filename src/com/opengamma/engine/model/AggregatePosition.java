/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.model;

import java.util.Collection;

/**
 * 
 *
 * @author kirk
 */
public interface AggregatePosition extends Position {
  Collection<Position> getSubPositions();

}
