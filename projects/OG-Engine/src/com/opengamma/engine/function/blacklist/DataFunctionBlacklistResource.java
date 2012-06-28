/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Publishes a {@link FunctionBlacklist} to remote clients
 */
public class DataFunctionBlacklistResource extends AbstractDataResource implements FunctionBlacklistRuleListener {

  private static final Logger s_logger = LoggerFactory.getLogger(DataFunctionBlacklistResource.class);

  /**
   * Field containing the blacklist name when included in a response message.
   */
  public static final String NAME_FIELD = "name";
  /**
   * Field containing the blacklist modification count when included in a response message.
   */
  public static final String MODIFICATION_COUNT_FIELD = "modificationCount";
  /**
   * Field containing the blacklist rules when included in a response message.
   */
  public static final String RULES_FIELD = "rules";
  /**
   * Field containing new blacklist rules when published to remote listeners.
   */
  public static final String RULES_ADDED_FIELD = "add";
  /**
   * Field containing removed blacklist rules when published to remote listeners.
   */
  public static final String RULES_REMOVED_FIELD = "remove";
  /**
   * Field containing the JMS topic name updates will be published on.
   */
  public static final String JMS_TOPIC_FIELD = "jms";

  private final FunctionBlacklist _underlying;
  private final FudgeContext _fudgeContext;
  private final String _jmsTopic;
  private final FudgeMessageSender _publish;

  public DataFunctionBlacklistResource(final FunctionBlacklist underlying, final FudgeContext fudgeContext, final JmsConnector jmsConnector) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    String name = jmsConnector.getTopicName();
    if (name == null) {
      _jmsTopic = underlying.getName() + "_BLACKLIST";
    } else {
      _jmsTopic = name + "_" + underlying.getName() + "_BLACKLIST";
    }
    JmsByteArrayMessageSender jmsSender = new JmsByteArrayMessageSender(_jmsTopic, jmsConnector.getJmsTemplateTopic());
    _publish = new ByteArrayFudgeMessageSender(jmsSender, fudgeContext);
    underlying.addRuleListener(this);
  }

  protected FunctionBlacklist getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @GET
  public Response info() {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg info = fsc.newMessage();
    info.add(NAME_FIELD, getUnderlying().getName());
    info.add(MODIFICATION_COUNT_FIELD, getUnderlying().getModificationCount());
    final MutableFudgeMsg rules = info.addSubMessage(RULES_FIELD, null);
    for (FunctionBlacklistRule rule : getUnderlying().getRules()) {
      fsc.addToMessage(rules, null, null, rule);
    }
    info.add(JMS_TOPIC_FIELD, _jmsTopic);
    return responseOk(info);
  }

  @GET
  @Path("mod/{mod}")
  public Response info(@PathParam("mod") final int mod) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg info = fsc.newMessage();
    final int modificationCount = getUnderlying().getModificationCount();
    if (modificationCount != mod) {
      info.add(MODIFICATION_COUNT_FIELD, modificationCount);
      final MutableFudgeMsg rules = info.addSubMessage(RULES_FIELD, null);
      for (FunctionBlacklistRule rule : getUnderlying().getRules()) {
        fsc.addToMessage(rules, null, null, rule);
      }
    }
    return responseOk(info);
  }

  private final class Publish implements Runnable {

    private final FudgeMsg _msg;

    public Publish(final FudgeMsg msg) {
      _msg = msg;
    }

    @Override
    public void run() {
      try {
        _publish.send(_msg);
      } catch (RuntimeException e) {
        s_logger.warn("Error publishing JMS message", e);
      }
    }

  }

  @Override
  public void ruleAdded(final int modificationCount, final FunctionBlacklistRule rule, final ExecutorService defer) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(MODIFICATION_COUNT_FIELD, modificationCount);
    final MutableFudgeMsg rulesMessage = msg.addSubMessage(RULES_ADDED_FIELD, null);
    fsc.addToMessage(rulesMessage, null, null, rule);
    defer.submit(new Publish(msg));
  }

  @Override
  public void rulesAdded(final int modificationCount, final Collection<FunctionBlacklistRule> rules, final ExecutorService defer) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(MODIFICATION_COUNT_FIELD, modificationCount);
    final MutableFudgeMsg rulesMessage = msg.addSubMessage(RULES_ADDED_FIELD, null);
    for (FunctionBlacklistRule rule : rules) {
      fsc.addToMessage(rulesMessage, null, null, rule);
    }
    defer.submit(new Publish(msg));
  }

  @Override
  public void ruleRemoved(final int modificationCount, final FunctionBlacklistRule rule, final ExecutorService defer) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(MODIFICATION_COUNT_FIELD, modificationCount);
    final MutableFudgeMsg rulesMessage = msg.addSubMessage(RULES_REMOVED_FIELD, null);
    fsc.addToMessage(rulesMessage, null, null, rule);
    defer.submit(new Publish(msg));
  }

  @Override
  public void rulesRemoved(final int modificationCount, final Collection<FunctionBlacklistRule> rules, final ExecutorService defer) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(MODIFICATION_COUNT_FIELD, modificationCount);
    final MutableFudgeMsg rulesMessage = msg.addSubMessage(RULES_REMOVED_FIELD, null);
    for (FunctionBlacklistRule rule : rules) {
      fsc.addToMessage(rulesMessage, null, null, rule);
    }
    defer.submit(new Publish(msg));
  }

}
