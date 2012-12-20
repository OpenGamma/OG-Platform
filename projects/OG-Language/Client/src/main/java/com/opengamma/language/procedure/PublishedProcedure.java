/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

/**
 * Marks an object as a procedure that can be published to a client.
 */
public interface PublishedProcedure {

  MetaProcedure getMetaProcedure();

}
