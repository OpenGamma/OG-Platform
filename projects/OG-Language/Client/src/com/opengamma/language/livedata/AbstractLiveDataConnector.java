/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.Constants;
import com.opengamma.language.invoke.AbstractInvoker;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;

/**
 * Partial implementation of a {@link LiveDataConnector} that converts the parameters and results using
 * the converters bound to the invoking session context.
 */
public abstract class AbstractLiveDataConnector extends AbstractInvoker implements LiveDataConnector {

  /**
   * Connection abstraction passed to the subclass.
   */
  public final class AbstractConnection extends Connection {

    private final SessionContext _context;
    private Runnable _cancel;

    private AbstractConnection(final SessionContext context) {
      _context = context;
    }

    public void setValue(final Object value) {
      if (value == null) {
        super.setValue(new Data());
      } else {
        final Data resultData = convertResult(_context, value);
        if (resultData == null) {
          // This is a fault - non-null should not have been converted to null
          final Value err = ValueUtils.ofError(Constants.ERROR_INTERNAL);
          err.setStringValue("Conversion of non-null '" + value.toString() + "' should not have given null");
          super.setValue(DataUtils.of(err));
        } else {
          super.setValue(resultData);
        }
      }
    }

    public void setCancelHandler(final Runnable cancel) {
      _cancel = cancel;
    }
    
    public Runnable getCancelHandler() {
      return _cancel;
    }

    @Override
    public void cancel() {
      super.cancel();
      if (_cancel != null) {
        _cancel.run();
      }
    }

  }

  protected AbstractLiveDataConnector(final List<MetaParameter> parameters) {
    super(parameters);
  }

  @Override
  protected List<MetaParameter> getParameters() {
    return super.getParameters();
  }

  protected abstract void connectImpl(SessionContext sessionContext, Object[] parameters, AbstractConnection connection);

  // AbstractInvoker

  @Override
  protected ResultConverter getResultConverter(final GlobalContext globalContext) {
    return globalContext.getLiveDataResultConverter();
  }

  @Override
  protected ParameterConverter getParameterConverter(final GlobalContext globalContext) {
    return globalContext.getLiveDataParameterConverter();
  }

  // LiveDataConnector

  @Override
  public final Connection connect(final SessionContext sessionContext, final List<Data> parameterValue) {
    final Object[] parameters = convertParameters(sessionContext, parameterValue);
    final AbstractConnection connection = new AbstractConnection(sessionContext);
    connectImpl(sessionContext, parameters, connection);
    return connection;
  }

}
