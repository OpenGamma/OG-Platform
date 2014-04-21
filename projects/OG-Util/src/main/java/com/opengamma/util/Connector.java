/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.Closeable;

/**
 * Connector used to access a resource, such as a database.
 * <p>
 * See the implementation classes for more information.
 * <p>
 * This interface is essentially a marker that allows connectors to be identified.
 */
public interface Connector extends Closeable {

  /**
   * Gets the display name of the connector.
   * 
   * @return a name usable for display, not null
   */
  String getName();

  /**
   * Gets the type of the connector.
   * <p>
   * This returns the type that should be used to manage the connector.
   * For example, consider {@code BarConnector} and its subclass {@code FooBarConnector}.
   * When this method is called on either type, it should return {@code BarConnector}, as that
   * is the type under which the connector should be managed.
   * 
   * @return the connector type, not null
   */
  Class<? extends Connector> getType();

}
