/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;


/**
 * Context information needed to write risk into a database.
 * <p>
 * Implementations will typically be Serializable and/or Fudge-Serializable. 
 * The reason for this requirement is that BatchDbRiskContexts
 * are sent over to the grid. 
 */
public interface BatchDbRiskContext {
}
