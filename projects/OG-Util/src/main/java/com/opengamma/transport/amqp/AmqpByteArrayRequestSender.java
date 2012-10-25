/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.Lifecycle;

import com.google.common.base.Charsets;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;

/**
 * RabbitMQ based sender for AMQP.
 */
public class AmqpByteArrayRequestSender extends AbstractAmqpByteArraySender implements ByteArrayRequestSender, MessageListener, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(AmqpByteArrayRequestSender.class);

  private final String _replyToQueue;
  private final AtomicLong _correlationIdGenerator = new AtomicLong();
  private final long _timeout;
  private final ScheduledExecutorService _executor;
  private final SimpleMessageListenerContainer _container;
  private final ConcurrentHashMap<String, ByteArrayMessageReceiver> _correlationId2MessageReceiver = new ConcurrentHashMap<String, ByteArrayMessageReceiver>();

  /**
   * Creates an instance.
   * 
   * @param connectionFactory  the connection factory, not null
   * @param exchange  the exchange, not null
   * @param routingKey  the routing key, not null
   */
  public AmqpByteArrayRequestSender(ConnectionFactory connectionFactory, String exchange, String routingKey) {
    this(connectionFactory, 30000, Executors.newSingleThreadScheduledExecutor(), exchange, routingKey);
  }

  /**
   * Creates an instance.
   * 
   * @param connectionFactory  the connection factory, not null
   * @param timeout  the timeout, positive
   * @param executor  the executor, not null
   * @param exchange  the exchange, not null
   * @param routingKey  the routing key, not null
   */
  public AmqpByteArrayRequestSender(
      ConnectionFactory connectionFactory,
      long timeout,
      ScheduledExecutorService executor,
      String exchange,
      String routingKey) {
    super(new RabbitTemplate(connectionFactory), exchange, routingKey);
    ArgumentChecker.notNull(connectionFactory, "connectionFactory");    
    ArgumentChecker.notNull(executor, "executor");
    
    if (timeout <= 0) {
      throw new IllegalArgumentException("Timeout must be positive");
    }
    _timeout = timeout;
    _executor = executor;
    
    try {
      Connection connection = connectionFactory.createConnection();
      Channel channel = connection.createChannel(false);
      
      Queue.DeclareOk declareResult = channel.queueDeclare();
      _replyToQueue = declareResult.getQueue();
      
      channel.queueBind(_replyToQueue, getExchange(), _replyToQueue);
      connection.close();
      
    } catch (IOException e) {
      throw new RuntimeException("Failed to create reply to queue", e);
    }
    
    _container = new SimpleMessageListenerContainer(); 
    _container.setConnectionFactory(connectionFactory);
    _container.setQueueNames(_replyToQueue);
    _container.setMessageListener(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reply-to queue.
   * 
   * @return the queue, not null
   */
  public String getReplyToQueue() {
    return _replyToQueue;
  }

  //-------------------------------------------------------------------------
  @Override
  public void sendRequest(final byte[] request, final ByteArrayMessageReceiver responseReceiver) {
    s_logger.debug("Dispatching request of size {} to exchange {}, routing key = {}", 
        new Object[] {request.length, getExchange(), getRoutingKey()});
    
    getAmqpTemplate().send(getExchange(), getRoutingKey(), createMessage(request, responseReceiver));
  }

  /**
   * Creates the message.
   * 
   * @param request  the request, not null
   * @param responseReceiver  the receiver, not null
   * @return the message, not null
   */
  private Message createMessage(final byte[] request, final ByteArrayMessageReceiver responseReceiver) {
    MessageProperties properties = new MessageProperties();
    Address replyTo = new Address(ExchangeTypes.DIRECT, getExchange(), getReplyToQueue());
    properties.setReplyToAddress(replyTo);
    
    final String correlationId = getReplyToQueue() + "-" + _correlationIdGenerator.getAndIncrement();
    byte[] correlationIdBytes = correlationId.getBytes(Charsets.UTF_8);
    properties.setCorrelationId(correlationIdBytes);
    
    Message message = new Message(request, properties);
    
    _correlationId2MessageReceiver.put(correlationId, responseReceiver);
    
    // Make sure the map stays clean if no response is received before timeout occurs. 
    // It would be nice if AmqpTemplate had a receive() method with a timeout parameter.
    _executor.schedule(new Runnable() {
      @Override
      public void run() {
        ByteArrayMessageReceiver receiver = _correlationId2MessageReceiver.remove(correlationId);
        if (receiver != null) {
          s_logger.error("Timeout reached while waiting for a response to send to {}", responseReceiver);
        }
      }
    }, _timeout, TimeUnit.MILLISECONDS);
    
    return message;
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    _container.start();
  }

  @Override
  public void stop() {
    _container.stop();
  }

  @Override
  public boolean isRunning() {
    return _container.isRunning();
  }

  //-------------------------------------------------------------------------
  @Override
  public void onMessage(Message message) {
    byte[] correlationIdBytes = message.getMessageProperties().getCorrelationId();
    if (correlationIdBytes == null) {
      s_logger.error("Got reply with no correlation ID: {} ", message);
      return;
    }
    
    String correlationId = new String(correlationIdBytes, Charsets.UTF_8);
    ByteArrayMessageReceiver receiver = _correlationId2MessageReceiver.remove(correlationId);
    if (receiver != null) {
      receiver.messageReceived(message.getBody());      
    } else {
      s_logger.warn("No receiver for message: {}", message);      
    }
  }

}
