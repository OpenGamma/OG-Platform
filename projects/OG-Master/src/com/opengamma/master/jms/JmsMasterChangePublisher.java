/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.jms;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.msg.Added;
import com.opengamma.master.msg.Corrected;
import com.opengamma.master.msg.MasterChangeMessage;
import com.opengamma.master.msg.Removed;
import com.opengamma.master.msg.Updated;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Sends Master modification to a JMS topic
 */
public class JmsMasterChangePublisher implements MasterChangeListener {
  
  private static final Logger s_logger = LoggerFactory.getLogger(JmsMasterChangePublisher.class);
  
  private static final String ADDED_TOPIC = "MasterChangeAdded";
  private static final String UPDATED_TOPIC = "MasterChangeUpdated";
  private static final String REMOVED_TOPIC = "MasterChangeRemoved";
  private static final String CORRECTED_TOPIC = "MasterChangeCorrected";
  
  private JmsTemplate _jmsTemplate;
  private final FudgeContext _fudgeContext;
  private final FudgeSerializationContext _fudgeSerializationContext;
  private String _addedTopic = ADDED_TOPIC;
  private String _updatedTopic = UPDATED_TOPIC;
  private String _removedTopic = REMOVED_TOPIC;
  private String _correctedTopic = CORRECTED_TOPIC;

  public JmsMasterChangePublisher(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeSerializationContext = new FudgeSerializationContext(fudgeContext);
    _fudgeContext = fudgeContext;
  }
    
  @Override
  public void added(UniqueIdentifier addedItem) {
    sendMessage(new Added(addedItem), _addedTopic);
  }

  private void sendMessage(final MasterChangeMessage message, final String topic) {
    final FudgeFieldContainer msg = FudgeSerializationContext.addClassHeader(_fudgeSerializationContext.objectToFudgeMsg(message), message.getClass(), MasterChangeMessage.class);
    s_logger.debug("sending message {}", msg);
    byte[] fudgeMsg = _fudgeContext.toByteArray(msg);
    s_logger.debug("Writing {} bytes data", fudgeMsg.length);
    JmsByteArrayMessageSender messageSender = new JmsByteArrayMessageSender(topic, getJmsTemplate());
    messageSender.send(fudgeMsg);
  }

  @Override
  public void removed(UniqueIdentifier removedItem) {
    sendMessage(new Removed(removedItem), _removedTopic);
  }

  @Override
  public void updated(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    sendMessage(new Updated(oldItem, newItem), _updatedTopic);
  }

  @Override
  public void corrected(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    sendMessage(new Corrected(oldItem, newItem), _correctedTopic);
  }

  /**
   * Gets the jmsTemplate field.
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

  /**
   * Sets the jmsTemplate field.
   * @param jmsTemplate  the jmsTemplate
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    _jmsTemplate = jmsTemplate;
  }

  /**
   * Gets the updatedTopic field.
   * @return the updatedTopic
   */
  public String getUpdatedTopic() {
    return _updatedTopic;
  }

  /**
   * Sets the updatedTopic field.
   * @param updatedTopic  the updatedTopic
   */
  public void setUpdatedTopic(String updatedTopic) {
    _updatedTopic = updatedTopic;
  }

  /**
   * Gets the removedTopic field.
   * @return the removedTopic
   */
  public String getRemovedTopic() {
    return _removedTopic;
  }

  /**
   * Sets the removedTopic field.
   * @param removedTopic  the removedTpoic
   */
  public void setRemovedTopic(String removedTopic) {
    _removedTopic = removedTopic;
  }

  /**
   * Gets the addedTopic field.
   * @return the addedTopic
   */
  public String getAddedTopic() {
    return _addedTopic;
  }

  /**
   * Sets the addedTopic field.
   * @param addedTopic  the addedTopic
   */
  public void setAddedTopic(String addedTopic) {
    _addedTopic = addedTopic;
  }

  /**
   * Gets the correctedTopic field.
   * @return the correctedTopic
   */
  public String getCorrectedTopic() {
    return _correctedTopic;
  }

  /**
   * Sets the correctedTopic field.
   * @param correctedTopic  the correctedTopic
   */
  public void setCorrectedTopic(String correctedTopic) {
    _correctedTopic = correctedTopic;
  }
  
}
