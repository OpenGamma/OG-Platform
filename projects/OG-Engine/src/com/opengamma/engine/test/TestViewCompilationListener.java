/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.view.compilation.ViewCompilationListener;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;

/**
 * Test implementation of {@link ViewCompilationListener}.
 */
public class TestViewCompilationListener extends AbstractTestResultListener<ViewEvaluationModel> implements ViewCompilationListener {

  @Override
  public void viewCompiled(ViewEvaluationModel evaluationModel) {
    resultReceived(evaluationModel);
  }

}
