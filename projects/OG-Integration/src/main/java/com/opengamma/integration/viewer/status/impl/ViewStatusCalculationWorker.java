/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Executes the View status calculation task with the default Executor service
 */
public class ViewStatusCalculationWorker {
  
  private static final ExecutorService DEFAULT_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadPoolFactory("ViewStatus"));
  
  private final ExecutorService _executor;
    
  private final Map<String, Set<String>> _valueRequirementBySecType;
  
  private final ToolContext _toolContext;
  
  private final UniqueId _portfolioId;
  
  private final UserPrincipal _user;
  
  public ViewStatusCalculationWorker(final Map<String, Collection<String>> valueRequirementBySecType, ToolContext toolContext, UniqueId portfolioId, UserPrincipal user) {
    this(valueRequirementBySecType, toolContext, portfolioId, user, DEFAULT_EXECUTOR);
  }
  
  public ViewStatusCalculationWorker(final Map<String, Collection<String>> valueRequirementBySecType, ToolContext toolContext, UniqueId portfolioId, 
      UserPrincipal user, final ExecutorService executorService) {
    ArgumentChecker.notNull(valueRequirementBySecType, "valueRequirementBySecType");
    ArgumentChecker.notNull(toolContext, "toolContex");
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(executorService, "executorService");
    
    _portfolioId = portfolioId;
    _user = user;
    _valueRequirementBySecType = deepCopy(valueRequirementBySecType);
    _toolContext = toolContext;
    _executor = executorService;
  }
  
  private Map<String, Set<String>> deepCopy(Map<String, Collection<String>> valueRequirementBySecType) {
    Map<String, Set<String>> result = Maps.newHashMap();
    for (String securityType : valueRequirementBySecType.keySet()) {
      result.put(securityType, Sets.newHashSet(valueRequirementBySecType.get(securityType)));
    }
    return result;
  }

  public ViewStatusResultAggregator run() {
    ViewStatusResultAggregator aggregator = new ViewStatusResultAggregatorImpl();
    CompletionService<PerViewStatusResult> completionService = new ExecutorCompletionService<PerViewStatusResult>(_executor);
    //submit task to executor to run partitioned by security type
    for (String securityType : _valueRequirementBySecType.keySet()) {
      Set<String> valueRequirements = _valueRequirementBySecType.get(securityType);
      completionService.submit(new ViewStatusCalculationTask(_toolContext, _portfolioId, _user, securityType, valueRequirements));
    }
    try {
      // process all completed task
      for (int i = 0; i < _valueRequirementBySecType.size(); i++) {
        Future<PerViewStatusResult> futureTask = completionService.take();
        PerViewStatusResult perViewStatusResult = futureTask.get();
        for (ViewStatusKey viewStatusKey : perViewStatusResult.keySet()) {
          aggregator.putStatus(viewStatusKey, perViewStatusResult.get(viewStatusKey));
        }
        
      } 
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      throw new OpenGammaRuntimeException("Error running View status report", ex.getCause());
    }
    return aggregator;
  }
}
