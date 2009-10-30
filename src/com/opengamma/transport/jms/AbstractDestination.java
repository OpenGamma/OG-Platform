/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Destination;
import javax.jms.Session;

import com.opengamma.util.ArgumentChecker;

/**
 * The JMS API sucks, and we need this structure just to be able to work with it properly.
 *
 * @author kirk
 */
public abstract class AbstractDestination {
  private final String _name;
  
  public AbstractDestination(String name) {
    ArgumentChecker.checkNotNull(name, "Destination name");
    _name = name;
  }
  
  public String getName() {
    return _name;
  }

  public abstract Destination constructDestination(Session jmsSession);
}
