/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

import java.util.List;

import javax.annotation.Nullable;

import com.opengamma.sesame.function.scenarios.ScenarioDefinition;

/**
 *
 */
public abstract class CurveDataShiftBuilder {

  protected final String _curveGlob;
  protected final List<String> _curveNames;
  protected final List<String> _columnNames;

  CurveDataShiftBuilder(@Nullable String curveGlob, List<String> curveNames, List<String> columnNames) {
    _curveGlob = curveGlob;
    _curveNames = curveNames;
    _columnNames = columnNames;
  }

  public ScenarioDefinition build() {
    throw new UnsupportedOperationException();

  }
}
