/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.custom.CustomMessageVisitor;
import com.opengamma.language.custom.CustomMessageVisitorRegistry;
import com.opengamma.language.custom.CustomVisitors;
import com.opengamma.language.function.FunctionVisitor;
import com.opengamma.language.livedata.LiveDataVisitor;
import com.opengamma.language.procedure.ProcedureVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Delegating visitor for the top level messages. Each message type has a default visitor that can be overridden.
 * For example, to filter a set of messages, get the existing handler, register a new one and delegate to the
 * original from the new one. To handle custom messages, register the handler and message types.
 */
public class UserMessagePayloadHandler implements UserMessagePayloadVisitor<UserMessagePayload, SessionContext>,
    InitializingBean, CustomMessageVisitorRegistry<UserMessagePayload, SessionContext> {

  private final CustomVisitors<UserMessagePayload, SessionContext> _customVisitors = new CustomVisitors<UserMessagePayload, SessionContext>();
  private FunctionVisitor<UserMessagePayload, SessionContext> _functionVisitor;
  private LiveDataVisitor<UserMessagePayload, SessionContext> _liveDataVisitor;
  private ProcedureVisitor<UserMessagePayload, SessionContext> _procedureVisitor;

  public UserMessagePayloadHandler() {
  }

  public UserMessagePayloadHandler(final UserMessagePayloadHandler copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _customVisitors.registerAll(copyFrom._customVisitors);
    _functionVisitor = copyFrom._functionVisitor;
    _liveDataVisitor = copyFrom._liveDataVisitor;
    _procedureVisitor = copyFrom._procedureVisitor;
  }

  // Main message type delegates

  public void setFunctionHandler(final FunctionVisitor<UserMessagePayload, SessionContext> functionVisitor) {
    ArgumentChecker.notNull(functionVisitor, "functionVisitor");
    _functionVisitor = functionVisitor;
  }

  public FunctionVisitor<UserMessagePayload, SessionContext> getFunctionHandler() {
    return _functionVisitor;
  }

  public void setLiveDataHandler(final LiveDataVisitor<UserMessagePayload, SessionContext> liveDataVisitor) {
    ArgumentChecker.notNull(liveDataVisitor, "liveDataVisitor");
    _liveDataVisitor = liveDataVisitor;
  }

  public LiveDataVisitor<UserMessagePayload, SessionContext> getLiveDataHandler() {
    return _liveDataVisitor;
  }

  public void setProcedureHandler(final ProcedureVisitor<UserMessagePayload, SessionContext> procedureVisitor) {
    ArgumentChecker.notNull(procedureVisitor, "procedureVisitor");
    _procedureVisitor = procedureVisitor;
  }

  public ProcedureVisitor<UserMessagePayload, SessionContext> getProcedureHandler() {
    return _procedureVisitor;
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getFunctionHandler(), "functionHandler");
    ArgumentChecker.notNull(getLiveDataHandler(), "liveDataHandler");
    ArgumentChecker.notNull(getProcedureHandler(), "procedureHandler");
  }

  // CustomMessageVisitorRegistry

  @Override
  public <M extends Custom> void register(Class<M> clazz,
      CustomMessageVisitor<M, UserMessagePayload, SessionContext> visitor) {
    _customVisitors.register(clazz, visitor);
  }

  // UserMessagePayloadVisitor

  @Override
  public UserMessagePayload visitUserMessagePayload(final UserMessagePayload payload, final SessionContext session) {
    // No-op
    return null;
  }

  @Override
  public UserMessagePayload visitTest(final Test message, final SessionContext session) {
    return TestMessageHandler.testMessage(message, session);
  }

  @Override
  public UserMessagePayload visitLiveData(final LiveData message, final SessionContext session) throws AsynchronousExecution {
    return message.accept(_liveDataVisitor, session);
  }

  @Override
  public UserMessagePayload visitFunction(final Function message, final SessionContext session) throws AsynchronousExecution {
    return message.accept(_functionVisitor, session);
  }

  @Override
  public UserMessagePayload visitProcedure(final Procedure message, final SessionContext session) throws AsynchronousExecution {
    return message.accept(_procedureVisitor, session);
  }

  @Override
  public UserMessagePayload visitCustom(final Custom message, final SessionContext session) {
    return _customVisitors.visit(message, session);
  }

}
