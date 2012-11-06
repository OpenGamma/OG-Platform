/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.engine.depgraph.ComputationTargetCollapser;
import com.opengamma.engine.depgraph.DefaultComputationTargetCollapser;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.view.ViewEvaluationTarget;
import com.opengamma.financial.view.ViewEvaluationTargetCollapser;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates {@link ComputationTargetCollapser} appropriate for the {@link DemoStandardFunctionConfiguration} functions.
 */
public class DemoComputationTargetCollapserBean extends SingletonFactoryBean<ComputationTargetCollapser> {

  private FunctionCompilationContext _compilationContext;

  public void setCompilationContext(final FunctionCompilationContext context) {
    _compilationContext = context;
  }

  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  @Override
  protected ComputationTargetCollapser createObject() {
    final DefaultComputationTargetCollapser collapser = new DefaultComputationTargetCollapser();
    if (getCompilationContext() != null) {
      final TempTargetRepository tempTargets = OpenGammaCompilationContext.getTempTargets(getCompilationContext());
      if (tempTargets != null) {
        collapser.addCollapser(ViewEvaluationTarget.TYPE, new ViewEvaluationTargetCollapser(tempTargets));
      }
    }
    return collapser;
  }

}
