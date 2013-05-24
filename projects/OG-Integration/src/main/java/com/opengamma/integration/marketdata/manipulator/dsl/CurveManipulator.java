/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveManipulator {

  public static class Builder {

    private final CurveSelector _selector;
    private final Scenario _scenario;
    private final List<StructureManipulator<YieldAndDiscountCurve>> _manipulators = Lists.newArrayList();

    public Builder(CurveSelector selector, Scenario scenario) {
      ArgumentChecker.notNull(selector, "selector");
      ArgumentChecker.notNull(scenario, "scenario");
      _selector = selector;
      _scenario = scenario;
    }

    public Builder parallelShift(double shift) {
      _manipulators.add(new ParallelShift(shift));
      return this;
    }

    // TODO this won't work for remote view clients
    /*public Builder transform(Function<YieldAndDiscountCurve, YieldAndDiscountCurve> fn) {
      return this;
    }*/

    // TODO what should this method be called? should it be package private and called by Scenario.add(builder)?
    public void execute() {
      _scenario.add(_selector, new CompositeStructureManipulator<>(_manipulators));
    }
  }
}
