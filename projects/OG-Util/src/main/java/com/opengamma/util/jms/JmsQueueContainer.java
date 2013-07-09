/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import org.springframework.context.Lifecycle;

/**
 * Container used to receive JMS messages.
 * <p>
 * See {@link JmsQueueContainerFactory}.
 */
public interface JmsQueueContainer extends Lifecycle {
  // no messages, as this is started up and left to run in the background
}
