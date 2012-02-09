/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import com.opengamma.component.ComponentInfo;

/**
 * Component info attribute names.
 * <p>
 * See {@link ComponentInfo#getAttributes()}.
 * <p>
 * This is a class of constants.
 */
public final class ComponentInfoAttributes {

  /**
   * The attribute name for the UniqueId scheme.
   */
  public static final String UNIQUE_ID_SCHEME = "uniqueIdScheme";
  /**
   * The attribute name for the JMS broker.
   */
  public static final String JMS_BROKER_URI = "jmsBrokerUri";
  /**
   * The attribute name for the JMS change manager topic.
   */
  public static final String JMS_CHANGE_MANAGER_TOPIC = "jmsChangeManagerTopic";

  /**
   * Restricted constructor.
   */
  private ComponentInfoAttributes() {
  }

}
