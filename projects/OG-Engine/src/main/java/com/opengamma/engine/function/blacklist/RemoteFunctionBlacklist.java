/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Provides remote access to a {@link FunctionBlacklist}.
 */
public class RemoteFunctionBlacklist extends AbstractFunctionBlacklist {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteFunctionBlacklist.class);

  private final class Listener extends BaseFunctionBlacklistRuleListener implements FudgeMessageReceiver {

    @Override
    protected Pair<Integer, ? extends Collection<FunctionBlacklistRule>> getUnderlyingRules(final int modificationCount) {
      final FudgeMsg msg = getProvider().refresh(getName(), modificationCount);
      if (msg.isEmpty()) {
        return null;
      }
      final FudgeDeserializer fdc = new FudgeDeserializer(getProvider().getFudgeContext());
      return Pairs.of(msg.getInt(DataFunctionBlacklistResource.MODIFICATION_COUNT_FIELD), getRules(fdc, msg.getMessage(DataFunctionBlacklistResource.RULES_FIELD)));
    }

    @Override
    protected synchronized void replaceRules(final Collection<FunctionBlacklistRule> rules) {
      final List<FunctionBlacklistRule> newRules = new ArrayList<FunctionBlacklistRule>(rules.size());
      final Set<FunctionBlacklistRule> oldRules = new HashSet<FunctionBlacklistRule>(_rules);
      for (FunctionBlacklistRule rule : rules) {
        if (_rules.contains(rule)) {
          oldRules.remove(rule);
          continue;
        }
        newRules.add(rule);
      }
      if (!newRules.isEmpty()) {
        addRules(newRules);
      }
      if (!oldRules.isEmpty()) {
        removeRules(oldRules);
      }
    }

    @Override
    protected synchronized void addRule(final FunctionBlacklistRule rule) {
      _rules.add(rule);
      notifyAddRule(rule);
    }

    @Override
    protected synchronized void addRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.addAll(rules);
      notifyAddRules(rules);
    }

    @Override
    protected synchronized void removeRule(final FunctionBlacklistRule rule) {
      _rules.remove(rule);
      notifyRemoveRule(rule);
    }

    @Override
    protected synchronized void removeRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.removeAll(rules);
      notifyRemoveRules(rules);
    }

    @Override
    public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
      final FudgeMsg msg = msgEnvelope.getMessage();
      final int modificationCount = msg.getInt(DataFunctionBlacklistResource.MODIFICATION_COUNT_FIELD);
      FudgeField field = msg.getByName(DataFunctionBlacklistResource.RULES_ADDED_FIELD);
      final FudgeDeserializer fdc = new FudgeDeserializer(fudgeContext);
      if (field != null) {
        final List<FudgeField> rulesMsg = msg.getFieldValue(FudgeMsg.class, field).getAllFields();
        if (rulesMsg.size() == 1) {
          ruleAdded(modificationCount, fdc.fieldValueToObject(FunctionBlacklistRule.class, rulesMsg.get(0)), getProvider().getBackgroundTasks());
        } else {
          final List<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(rulesMsg.size());
          for (FudgeField ruleField : rulesMsg) {
            rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, ruleField));
          }
          rulesAdded(modificationCount, rules, getProvider().getBackgroundTasks());
        }
      }
      field = msg.getByName(DataFunctionBlacklistResource.RULES_REMOVED_FIELD);
      if (field != null) {
        final List<FudgeField> rulesMsg = msg.getFieldValue(FudgeMsg.class, field).getAllFields();
        if (rulesMsg.size() == 1) {
          ruleRemoved(modificationCount, fdc.fieldValueToObject(FunctionBlacklistRule.class, rulesMsg.get(0)), getProvider().getBackgroundTasks());
        } else {
          final List<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(rulesMsg.size());
          for (FudgeField ruleField : rulesMsg) {
            rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, ruleField));
          }
          rulesRemoved(modificationCount, rules, getProvider().getBackgroundTasks());
        }
      }
    }

  }

  private final RemoteFunctionBlacklistProvider _provider;
  private final Set<FunctionBlacklistRule> _rules = new HashSet<FunctionBlacklistRule>();
  private final Listener _listener = new Listener();
  private final Connection _connection;

  private static Collection<FunctionBlacklistRule> getRules(final FudgeDeserializer fdc, final FudgeMsg rulesField) {
    if (rulesField != null) {
      final List<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(rulesField.getNumFields());
      for (FudgeField rule : rulesField) {
        rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, rule));
      }
      return rules;
    } else {
      return Collections.emptyList();
    }
  }

  public RemoteFunctionBlacklist(final FudgeDeserializer fdc, FudgeMsg info, final RemoteFunctionBlacklistProvider provider) {
    super(info.getString(DataFunctionBlacklistResource.NAME_FIELD), provider.getBackgroundTasks());
    _provider = provider;
    _listener.init(info.getInt(DataFunctionBlacklistResource.MODIFICATION_COUNT_FIELD), getRules(fdc, info.getMessage(DataFunctionBlacklistResource.RULES_FIELD)));
    _connection = startJmsConnection(info.getString(DataFunctionBlacklistResource.JMS_TOPIC_FIELD), _listener);
    _listener.refresh();
  }

  protected Connection startJmsConnection(final String topicName, final FudgeMessageReceiver listener) {
    try {
      final Connection connection = getProvider().getJmsConnector().getConnectionFactory().createConnection();
      connection.start();
      final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final Topic topic = session.createTopic(topicName);
      final MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(listener, getProvider().getFudgeContext())));
      return connection;
    } catch (JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create JMS connection on " + topicName, e);
    }
  }

  @Override
  protected void finalize() {
    if (_connection != null) {
      try {
        _connection.close();
      } catch (JMSException e) {
        s_logger.warn("Failed to close JMS connection", e);
      }
    }
  }

  protected RemoteFunctionBlacklistProvider getProvider() {
    return _provider;
  }

  @Override
  public Set<FunctionBlacklistRule> getRules() {
    synchronized (_listener) {
      return new HashSet<FunctionBlacklistRule>(_rules);
    }
  }

}
