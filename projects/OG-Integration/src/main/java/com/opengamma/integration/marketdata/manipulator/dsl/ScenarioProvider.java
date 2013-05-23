/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.google.common.base.Supplier;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * Provides a {@link Scenario} and receives notification when its results arrive.
 */
public interface ScenarioProvider extends Supplier<Scenario> {

  void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult);
}
