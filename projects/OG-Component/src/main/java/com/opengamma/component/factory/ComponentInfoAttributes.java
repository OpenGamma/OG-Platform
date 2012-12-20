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
   * The attribute name for the level.
   * The level of a component represents how much aggregation has occurred.
   * A database level component would have level=1, whereas one that combines
   * it with a second database would have level=2.
   */
  public static final String LEVEL = "level";
  /**
   * The attribute name for the remote client Java class name.
   * The Java class name that can be used to create a client for this component.
   */
  public static final String REMOTE_CLIENT_JAVA = "remoteClientJava";
  /**
   * The attribute name for the JMS broker.
   */
  public static final String JMS_BROKER_URI = "jmsBrokerUri";
  /**
   * The attribute name for the JMS change manager topic.
   */
  public static final String JMS_CHANGE_MANAGER_TOPIC = "jmsChangeManagerTopic";
  /**
   * The attribute name for the time-out where heartbeating is required.
   */
  public static final String TIMEOUT = "timeout";
  /**
   * The attribute name for the accepted types of a component.
   * This attribute can be used for anything where the component only accepts a subset
   * of all possible input. The types should be expressed as a comma separated string.
   */
  public static final String ACCEPTED_TYPES = "acceptedTypes";

  /**
   * Restricted constructor.
   */
  private ComponentInfoAttributes() {
  }

}
