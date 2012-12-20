/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

/**
 * Marks an object as a function that can be published to a client.
 */
public interface PublishedFunction {

  MetaFunction getMetaFunction();

}
