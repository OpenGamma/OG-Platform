/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Publishes a {@link FunctionBlacklist} to remote clients
 */
public class DataFunctionBlacklistResource extends AbstractDataResource {

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

  private final FunctionBlacklist _underlying;
  private final FudgeContext _fudgeContext;

  public DataFunctionBlacklistResource(final FunctionBlacklist underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    // TODO: create a JMS topic for publishing changes to this underlying
  }

  protected FunctionBlacklist getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected void info(final FudgeSerializer fsc, final MutableFudgeMsg info) {
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
    // TODO: add the JMS topic to the message
    info(fsc, info);
    return responseOk(info);
  }

  @GET
  @Path("mod/{mod}")
  public Response info(@PathParam("mod") final int mod) {
    final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg info = fsc.newMessage();
    final int modificationCount = getUnderlying().getModificationCount();
    info.add(MODIFICATION_COUNT_FIELD, modificationCount);
    if (modificationCount != mod) {
      final MutableFudgeMsg rules = info.addSubMessage(RULES_FIELD, null);
      for (FunctionBlacklistRule rule : getUnderlying().getRules()) {
        fsc.addToMessage(rules, null, null, rule);
      }
    }
    return responseOk(info);
  }

}
