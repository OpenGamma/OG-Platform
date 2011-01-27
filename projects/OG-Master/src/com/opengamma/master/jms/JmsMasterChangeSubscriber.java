/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.jms;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.msg.Added;
import com.opengamma.master.msg.Corrected;
import com.opengamma.master.msg.MasterChangeMessage;
import com.opengamma.master.msg.MasterChangeMessageVisitor;
import com.opengamma.master.msg.Removed;
import com.opengamma.master.msg.Updated;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives MasterChange message and delegates to underlying  MasterChangeListener
 */
public class JmsMasterChangeSubscriber implements MessageListener {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsMasterChangeSubscriber.class);
  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();
  
  private final JmsByteArrayMessageDispatcher _messageDispatcher;
  private final FudgeMessageReceiver _fudgeReceiver;
  private final MasterChangeMessageVisitor _messageVisitor = new MasterChangeMessageVisitor() {

    @Override
    protected void visitUnexpectedMessage(final MasterChangeMessage message) {
      s_logger.warn("Unexpected message - {}", message);
    }

    @Override
    protected void visitAddedMessage(Added message) {
      notifyAdded(message.getAddedItem());
    }

    @Override
    protected void visitRemovedMessage(Removed message) {
      notifyRemoved(message.getRemovedItem());
    }

    @Override
    protected void visitUpdatedMessage(Updated message) {
      notifyUpdated(message.getOldItem(), message.getNewItem());
    }

    @Override
    protected void visitCorrectedMessage(Corrected message) {
      notifyCorrected(message.getOldItem(), message.getNewItem());
    }
  };
  
  /**
   * Creates Master change subscriber
   * 
   * @param fudgeContext the fudgeContext, not null
   */
  public JmsMasterChangeSubscriber(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeReceiver = new MasterChangeFudgeMessageReceiver();
    _messageDispatcher = new JmsByteArrayMessageDispatcher(
        new ByteArrayFudgeMessageReceiver(_fudgeReceiver, fudgeContext));
  }
  
  private void notifyAdded(UniqueIdentifier addedItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.added(addedItem);
    }
  }
  
  private void notifyRemoved(UniqueIdentifier removedItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.removed(removedItem);
    }
  }
  
  private void notifyUpdated(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.updated(oldItem, newItem);
    }
  }
  
  private void notifyCorrected(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.corrected(oldItem, newItem);
    }
  }

  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  public void removeChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }
  
  private class MasterChangeFudgeMessageReceiver implements FudgeMessageReceiver {
    @Override
    public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
      
      final FudgeFieldContainer msg = msgEnvelope.getMessage();
      s_logger.debug("Received {}", msg);
      final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
      final MasterChangeMessage message = context.fudgeMsgToObject(MasterChangeMessage.class, msg);
      message.accept(_messageVisitor);
      
    }
  }

  @Override
  public void onMessage(Message message) {
    _messageDispatcher.onMessage(message);
  }

}
