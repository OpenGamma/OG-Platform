/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
