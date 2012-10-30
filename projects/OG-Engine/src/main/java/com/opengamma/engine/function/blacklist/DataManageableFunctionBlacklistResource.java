/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.jms.JmsConnector;

/**
 * Publishes a {@link ManageableFunctionBlacklist} to remote clients
 */
public class DataManageableFunctionBlacklistResource extends DataFunctionBlacklistResource {

  /**
   * Name of a field containing a rule in a request.
   */
  public static final String RULE_FIELD = "rule";
  /**
   * Name of a field containing the ttl in a request.
   */
  public static final String TTL_FIELD = "ttl";

  public DataManageableFunctionBlacklistResource(final ManageableFunctionBlacklist underlying, final FudgeContext fudgeContext, final JmsConnector jmsConnector) {
    super(underlying, fudgeContext, jmsConnector);
  }

  @Override
  protected ManageableFunctionBlacklist getUnderlying() {
    return (ManageableFunctionBlacklist) super.getUnderlying();
  }

  @POST
  @Path("add")
  @Consumes(FudgeRest.MEDIA)
  public void add(final FudgeMsg request) {
    final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
    final Integer ttl = request.getInt(TTL_FIELD);
    final List<FudgeField> fields = request.getAllByName(RULE_FIELD);
    if (fields.size() > 1) {
      final Collection<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(fields.size());
      for (FudgeField field : fields) {
        rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, field));
      }
      if (ttl != null) {
        getUnderlying().addBlacklistRules(rules, ttl);
      } else {
        getUnderlying().addBlacklistRules(rules);
      }
    } else if (!fields.isEmpty()) {
      final FunctionBlacklistRule rule = fdc.fieldValueToObject(FunctionBlacklistRule.class, fields.get(0));
      if (ttl != null) {
        getUnderlying().addBlacklistRule(rule, ttl);
      } else {
        getUnderlying().addBlacklistRule(rule);
      }
    }
  }

  @POST
  @Path("remove")
  @Consumes(FudgeRest.MEDIA)
  public void remove(final FudgeMsg request) {
    final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
    final List<FudgeField> fields = request.getAllByName(RULE_FIELD);
    if (fields.size() > 1) {
      final Collection<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(fields.size());
      for (FudgeField field : fields) {
        rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, field));
      }
      getUnderlying().removeBlacklistRules(rules);
    } else if (!fields.isEmpty()) {
      final FunctionBlacklistRule rule = fdc.fieldValueToObject(FunctionBlacklistRule.class, fields.get(0));
      getUnderlying().removeBlacklistRule(rule);
    }
  }

}
