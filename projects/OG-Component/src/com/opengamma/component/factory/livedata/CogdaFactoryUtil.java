/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.livedata;

import org.slf4j.Logger;

import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.livedata.server.MapLastKnownValueStoreProvider;
import com.opengamma.livedata.server.RedisLastKnownValueStoreProvider;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.jms.JmsConnector;

/**
 * 
 */
public final class CogdaFactoryUtil {
  
  private CogdaFactoryUtil() {
  }

  public static FudgeMessageSender constructMessageSender(String topicName, JmsConnector jmsConnector) {
    JmsByteArrayMessageSender jmsSender = new JmsByteArrayMessageSender(topicName, jmsConnector.getJmsTemplateTopic());
    ByteArrayFudgeMessageSender bafms = new ByteArrayFudgeMessageSender(jmsSender);
    return bafms;
  }

  public static LastKnownValueStoreProvider constructLastKnownValueStoreProvider(
      String redisServer,
      Integer redisPort,
      String redisPrefix,
      boolean writeThrough,
      Logger logger) {
    if (redisServer == null) {
      logger.info("No Redis Server specified in configuration so using map LKVStoreProvider.");
      return new MapLastKnownValueStoreProvider();
    } else {
      logger.info("Connecting Redis LKV Store Provider to {}:{} {} {}", new Object[] {redisServer, redisPort, redisPrefix, writeThrough});
      RedisLastKnownValueStoreProvider lkvProvider = new RedisLastKnownValueStoreProvider();
      lkvProvider.setServer(redisServer);
      lkvProvider.setWriteThrough(writeThrough);
      if (redisPort == null) {
        logger.info("No Redis port provided. Defaulting to 6379");
        lkvProvider.setPort(6379);
      } else {
        lkvProvider.setPort(redisPort);
      }
      if (redisPrefix != null) {
        logger.info("Setting Redis key prefix to {}", redisPrefix);
        lkvProvider.setGlobalPrefix(redisPrefix);
      }
      return lkvProvider;
    }
  }
}
