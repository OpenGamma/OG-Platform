/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * This test is only here to ensure that the Ivy configuration is sufficient
 * to launch an in-memory ActiveMQ connection.
 *
 * @author kirk
 */
public class ActiveMQTestUtil {
  
  public static ActiveMQConnectionFactory createTestConnectionFactory() {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    return cf;
  }
}
