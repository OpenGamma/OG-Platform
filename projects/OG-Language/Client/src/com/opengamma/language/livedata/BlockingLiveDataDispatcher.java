/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.language.Data;
import com.opengamma.language.async.AsynchronousExecution;
import com.opengamma.language.async.AsynchronousOperation;
import com.opengamma.language.async.ResultCallback;
import com.opengamma.language.context.SessionContext;
import com.opengamma.util.tuple.Pair;

/**
 * Implementation of {@link LiveDataDispatcher} that blocks the connection until the first result is
 * produced and then cancels the underlying connection.
 */
public class BlockingLiveDataDispatcher implements LiveDataDispatcher {

  private final Map<Pair<SessionContext, Integer>, Object> _data = new HashMap<Pair<SessionContext, Integer>, Object>();

  @SuppressWarnings("unchecked")
  @Override
  public void dispatchValue(final SessionContext context, final int identifier, final Data value) {
    ResultCallback<Result> callback = null;
    synchronized (this) {
      final Pair<SessionContext, Integer> key = Pair.of(context, identifier);
      if (value != null) {
        Object o = _data.remove(key);
        if (o instanceof ResultCallback) {
          callback = (ResultCallback<Result>) o;
        } else {
          _data.put(key, value);
        }
      } else {
        _data.remove(key);
      }
    }
    if (callback != null) {
      callback.setResult(new Result(identifier, value));
      context.getConnections().cancel(identifier);
    }
  }

  @Override
  public Result createResult(final SessionContext context, final int identifier, final Data result) throws AsynchronousExecution {
    if (result != null) {
      context.getConnections().cancel(identifier);
      return new Result(identifier, result);
    } else {
      final AsynchronousOperation<Result> async = new AsynchronousOperation<Result>();
      synchronized (this) {
        final Pair<SessionContext, Integer> key = Pair.of(context, identifier);
        Object o = _data.remove(key);
        if (o instanceof Data) {
          context.getConnections().cancel(identifier);
          return new Result(identifier, (Data) o);
        } else {
          _data.put(key, async.getCallback());
        }
      }
      return async.getResult();
    }
  }

}
