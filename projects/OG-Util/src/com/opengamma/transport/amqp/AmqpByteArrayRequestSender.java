/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.ExchangeType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageCreator;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.SimpleMessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * This only works with RabbitMQ at the moment.
 */
public class AmqpByteArrayRequestSender extends AbstractAmqpByteArraySender implements ByteArrayRequestSender, MessageListener, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(AmqpByteArrayRequestSender.class);
  
  private final String _replyToQueue;
  private final AtomicLong _correlationIdGenerator = new AtomicLong();
  private final long _timeout;
  private final ScheduledExecutorService _executor;
  private final SimpleMessageListenerContainer _container;
  private final ConcurrentHashMap<String, ByteArrayMessageReceiver> _correlationId2MessageReceiver = new ConcurrentHashMap<String, ByteArrayMessageReceiver>();
  
  public AmqpByteArrayRequestSender(ConnectionFactory connectionFactory, String exchange, String routingKey) {
    this(connectionFactory, 30000, Executors.newSingleThreadScheduledExecutor(), exchange, routingKey);
  }
  
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
      Channel channel = connection.createChannel();
      
      Queue.DeclareOk declareResult = channel.queueDeclare();
      _replyToQueue = declareResult.getQueue();
      
      channel.queueBind(_replyToQueue, getExchange(), _replyToQueue);
      
      connection.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create reply to queue", e);
    }
    
    _container = new SimpleMessageListenerContainer(); 
    _container.setConnectionFactory(connectionFactory);
    _container.setQueueName(_replyToQueue);
    _container.setMessageListener(this);
  }
  
  public String getReplyToQueue() {
    return _replyToQueue;
  }

  @Override
  public void sendRequest(final byte[] request,
      final ByteArrayMessageReceiver responseReceiver) {
    s_logger.debug("Dispatching request of size {} to exchange {}, routing key = {}", 
        new Object[] {request.length, getExchange(), getRoutingKey()});
    
    getAmqpTemplate().send(getExchange(), getRoutingKey(), new MessageCreator() {
      public Message createMessage() {
        SimpleMessageProperties properties = new SimpleMessageProperties();
        Address replyTo = new Address(ExchangeType.direct, getExchange(), getReplyToQueue());
        properties.setReplyTo(replyTo);
        
        final String correlationId = getReplyToQueue() + "-" + _correlationIdGenerator.getAndIncrement();
        byte[] correlationIdBytes;
        try {
          correlationIdBytes = correlationId.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new OpenGammaRuntimeException("This should never happen - UTF-8 should be supported", e);
        }
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
    });
  }

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

  @Override
  public void onMessage(Message message) {
    byte[] correlationIdBytes = message.getMessageProperties().getCorrelationId();
    if (correlationIdBytes == null) {
      s_logger.error("Got reply with no correlation ID: {} ", message);
      return;
    }
    
    String correlationId;
    try {
      correlationId = new String(correlationIdBytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new OpenGammaRuntimeException("This should never happen - UTF-8 should be supported", e);
    }
    
    ByteArrayMessageReceiver receiver = _correlationId2MessageReceiver.remove(correlationId);
    if (receiver != null) {
      receiver.messageReceived(message.getBody());      
    } else {
      s_logger.warn("No receiver for message: {}", message);      
    }
  }
  
}
