/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceShiftManipulatorTest {

  private static final ZonedDateTime VALUATION_TIME = ZonedDateTime.of(2011, 3, 8, 11, 0, 0, 0, ZoneOffset.UTC);
  private static final Clock CLOCK = Clock.fixed(VALUATION_TIME.toInstant(), ZoneOffset.UTC);
  private static final FunctionExecutionContext EXECUTION_CONTEXT = new FunctionExecutionContext();
  private static final ValueSpecification VALUE_SPECIFICATION =
      new ValueSpecification("valueName",
                             ComputationTargetSpecification.NULL,
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get());

  static {
    EXECUTION_CONTEXT.setValuationClock(CLOCK);
  }

  @Test
  public void relativeSingle() {
    double x = 1.1;
    double y = 1.5;
    double shiftAmount = 0.1;

    VolatilitySurfaceShift shift = new VolatilitySurfaceShift(x, y, shiftAmount);
    VolatilitySurface surface = mock(VolatilitySurface.class);
    VolatilitySurfaceShiftManipulator manipulator =
        VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, Lists.newArrayList(shift));
    manipulator.execute(surface, VALUE_SPECIFICATION, EXECUTION_CONTEXT);

    verify(surface).withSingleMultiplicativeShift(x, y, 1 + shiftAmount);
  }

  @Test
  public void relativeMultiple() {
    Period xPeriod1 = Period.ofYears(1);
    double x1 = TimeCalculator.getTimeBetween(VALUATION_TIME, VALUATION_TIME.plus(xPeriod1));
    double y1 = 1.5;
    double shiftAmount1 = 0.1;

    Period xPeriod2 = Period.ofYears(2);
    double x2 = TimeCalculator.getTimeBetween(VALUATION_TIME, VALUATION_TIME.plus(xPeriod2));
    double y2 = 2.5;
    double shiftAmount2 = 0.2;

    VolatilitySurfaceShift shift1 = new VolatilitySurfaceShift(xPeriod1, y1, shiftAmount1);
    VolatilitySurfaceShift shift2 = new VolatilitySurfaceShift(xPeriod2, y2, shiftAmount2);

    VolatilitySurface surface = mock(VolatilitySurface.class);
    VolatilitySurfaceShiftManipulator manipulator =
        VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, Lists.newArrayList(shift1, shift2));
    manipulator.execute(surface, VALUE_SPECIFICATION, EXECUTION_CONTEXT);

    verify(surface).withMultipleMultiplicativeShifts(new double[]{x1, x2},
                                                     new double[]{y1, y2},
                                                     new double[]{1 + shiftAmount1, 1 + shiftAmount2});
  }

  @Test
  public void absoluteSingle() {
    double x = 1.5;
    Period yPeriod = Period.ofYears(1);
    double y = TimeCalculator.getTimeBetween(VALUATION_TIME, VALUATION_TIME.plus(yPeriod));
    double shiftAmount = 0.1;

    VolatilitySurfaceShift shift = new VolatilitySurfaceShift(x, yPeriod, shiftAmount);
    VolatilitySurface surface = mock(VolatilitySurface.class);
    VolatilitySurfaceShiftManipulator manipulator =
        VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.ABSOLUTE, Lists.newArrayList(shift));
    manipulator.execute(surface, VALUE_SPECIFICATION, EXECUTION_CONTEXT);

    verify(surface).withSingleAdditiveShift(x, y, shiftAmount);
  }

  @Test
  public void absoluteMultiple() {
    double x1 = 1.1;
    double y1 = 1.5;
    double shiftAmount1 = 0.1;

    double x2 = 2.1;
    double y2 = 2.5;
    double shiftAmount2 = 0.2;

    VolatilitySurfaceShift shift1 = new VolatilitySurfaceShift(x1, y1, shiftAmount1);
    VolatilitySurfaceShift shift2 = new VolatilitySurfaceShift(x2, y2, shiftAmount2);

    VolatilitySurface surface = mock(VolatilitySurface.class);
    VolatilitySurfaceShiftManipulator manipulator =
        VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.ABSOLUTE, Lists.newArrayList(shift1, shift2));
    manipulator.execute(surface, VALUE_SPECIFICATION, EXECUTION_CONTEXT);

    verify(surface).withMultipleAdditiveShifts(new double[]{x1, x2},
                                               new double[]{y1, y2},
                                               new double[]{shiftAmount1, shiftAmount2});
  }
}
