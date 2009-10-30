/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

/**
 * This test is only here to ensure that the Ivy configuration is sufficient
 * to launch an in-memory ActiveMQ connection.
 *
 * @author kirk
 */
public class ActiveMQTest {

  @Test
  public void testVMBroker() throws Exception {
    ConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    Connection conn = cf.createConnection();
    Session session = conn.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    Topic t = session.createTopic("fibble");
    MessageProducer producer = session.createProducer(t);
    Message message = session.createMessage();
    producer.send(message);
    conn.close();
  }
}
