/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO get rid of this, follow the pattern in CurveManipulator
 */
public class PointManipulatorBuilder {

    private final PointSelector _selector;
    private final Scenario _scenario;
    private final List<StructureManipulator<Double>> _manipulators = Lists.newArrayList();

    public PointManipulatorBuilder(PointSelector selector, Scenario scenario) {
      ArgumentChecker.notNull(selector, "selector");
      ArgumentChecker.notNull(scenario, "scenario");
      _selector = selector;
      _scenario = scenario;
    }

    public PointManipulatorBuilder scaling(double scalingFactor) {
      _manipulators.add(new Scaling(scalingFactor));
      return this;
    }

    public void execute() {
      _scenario.add(_selector, new CompositeStructureManipulator<>(_manipulators));
    }
}
