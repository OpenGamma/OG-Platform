/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Destination;
import javax.jms.Session;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass())
      return false;
    AbstractDestination other = (AbstractDestination) obj;
    if(!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
