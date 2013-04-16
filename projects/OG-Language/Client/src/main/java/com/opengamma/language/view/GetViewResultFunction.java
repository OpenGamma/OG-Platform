/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.Cancelable;
import com.opengamma.util.async.ResultCallback;

/**
 * Returns the latest result from a calculating view
 */
public class GetViewResultFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(GetViewResultFunction.class);

  /**
   * Default instance.
   */
  public static final GetViewResultFunction INSTANCE = new GetViewResultFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientHandle.class).get());
    final MetaParameter waitForResult = new MetaParameter("waitForResult", JavaTypeInfo.builder(Integer.class).defaultValue(0).get());
    final MetaParameter lastViewCycleId = new MetaParameter("lastViewCycleId", JavaTypeInfo.builder(UniqueId.class).allowNull().get());
    return Arrays.asList(viewClient, waitForResult, lastViewCycleId);
  }

  private GetViewResultFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "GetViewResult", getParameters(), this));
  }

  protected GetViewResultFunction() {
    this(new DefinitionAnnotater(GetViewResultFunction.class));
  }

  private static final class Listener implements ViewResultListener, Cancelable {

    private final ViewClientHandle _viewClientHandle;
    private final ResultCallback<Object> _asyncResult;
    private final UniqueId _lastViewCycleId;
    private final AtomicBoolean _resultPosted = new AtomicBoolean();
    private final Cancelable _timeout;

    public Listener(final ViewClientHandle viewClientHandle, final ResultCallback<Object> asyncResult, final int timeoutMillis, final UniqueId lastViewCycleId) {
      _viewClientHandle = viewClientHandle;
      _lastViewCycleId = lastViewCycleId;
      _asyncResult = asyncResult;
      if (timeoutMillis > 0) {
        _timeout = AsynchronousOperation.timeout(this, timeoutMillis);
      } else {
        _timeout = null;
      }
      _viewClientHandle.get().addResultListener(this);
    }

    private boolean postResultImpl(final Object result) {
      if (!_resultPosted.getAndSet(true)) {
        s_logger.info("Posting result {}", result);
        _viewClientHandle.get().removeResultListener(this);
        _viewClientHandle.unlock();
        _asyncResult.setResult(result);
        return true;
      } else {
        s_logger.debug("Result already posted - ignoring {}", result);
        return false;
      }
    }

    public void postResult(final Object result) {
      if (postResultImpl(result)) {
        if (_timeout != null) {
          _timeout.cancel(false);
        }
      }
    }

    // ViewResultListener
    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      // Ignore
      s_logger.debug("View definition compiled");
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      postResult("View compilation failed - " + exception.getMessage());
    }

    @Override
    public void cycleStarted(final ViewCycleMetadata cycleMetadata) {
    }

    @Override
    public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, final ViewDeltaResultModel deltaFragment) {
      // Ignore
      s_logger.debug("Ignoring partial results");
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      if (fullResult != null) {
        if (!fullResult.getViewCycleId().equals(_lastViewCycleId)) {
          s_logger.debug("Posting full result");
          postResult(fullResult);
        } else {
          // This probably shouldn't happen
          s_logger.warn("Ignoring full result from cycle {}", _lastViewCycleId);
        }
      } else {
        // Received only a delta; query the latest full result from the client
        if (!_resultPosted.getAndSet(true)) {
          final ViewComputationResultModel result;
          try {
            s_logger.debug("Querying full result after receiving delta result");
            result = _viewClientHandle.get().getViewClient().getLatestResult();
          } finally {
            _resultPosted.set(false);
          }
          if (result != null) {
            if (!result.getViewCycleId().equals(_lastViewCycleId)) {
              s_logger.debug("Posting full result");
              postResult(result);
            } else {
              // This probably shouldn't happen
              s_logger.warn("Ignoring delta result from cycle {}", _lastViewCycleId);
            }
          } else {
            s_logger.warn("Cycle completed, but latest result not available");
            postResult("No result after cycle completion");
          }
        } else {
          s_logger.debug("Ignoring delta result; result already posted");
        }
      }
    }

    @Override
    public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
      postResult("Cycle execution failed - " + exception.getMessage());
    }

    @Override
    public UserPrincipal getUser() {
      return _viewClientHandle.get().getUserContext().getLiveDataUser();
    }

    @Override
    public void processCompleted() {
      s_logger.info("Process completed");
      postResult(null);
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      postResult("View process terminated");
    }

    @Override
    public void clientShutdown(final Exception e) {
    }

    // Cancellable
    @Override
    public boolean cancel(final boolean mayInterrupt) {
      s_logger.info("Timeout elapsed");
      return postResultImpl(null);
    }

  }

  public static Object invoke(final ViewClientHandle viewClientHandle, final int waitForResult, final UniqueId lastViewCycleId) throws AsynchronousExecution {
    final ViewClient viewClient = viewClientHandle.get().getViewClient();
    ViewComputationResultModel result = viewClient.getLatestResult();
    if ((result == null) || result.getViewCycleId().equals(lastViewCycleId)) {
      if (waitForResult != 0) {
        s_logger.info("Registering listener for asynchronous result");
        final AsynchronousOperation<Object> async = AsynchronousOperation.create(Object.class);
        final Listener listener = new Listener(viewClientHandle, async.getCallback(), waitForResult, lastViewCycleId);
        result = viewClient.getLatestResult();
        if ((result != null) && !result.getViewCycleId().equals(lastViewCycleId)) {
          // Result might have arrived before the listener was registered
          s_logger.debug("Inline result received");
          listener.postResult(result);
        }
        return async.getResult();
      } else {
        s_logger.debug("No result available; returning NULL");
      }
    } else {
      s_logger.debug("Result {} immediately available", result);
    }
    viewClientHandle.unlock();
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    final ViewClientHandle viewClientHandle = (ViewClientHandle) parameters[0];
    final int waitForResult = (Integer) parameters[1];
    final UniqueId lastViewCycleId = (UniqueId) parameters[2];
    return invoke(viewClientHandle, waitForResult, lastViewCycleId);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
