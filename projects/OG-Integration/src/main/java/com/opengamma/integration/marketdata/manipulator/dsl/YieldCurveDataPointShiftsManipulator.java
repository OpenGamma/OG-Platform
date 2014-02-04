/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class YieldCurveDataPointShiftsManipulator implements StructureManipulator<YieldCurveData> {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveDataPointShiftsManipulator.class);

  private final ScenarioShiftType _shiftType;
  private final List<YieldCurvePointShift> _shifts;

  /* package */ YieldCurveDataPointShiftsManipulator(ScenarioShiftType shiftType, List<YieldCurvePointShift> shifts) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shifts = ImmutableList.copyOf(ArgumentChecker.notEmpty(shifts, "shiftList"));
  }

  @Override
  public YieldCurveData execute(YieldCurveData curveData,
                                ValueSpecification valueSpecification,
                                FunctionExecutionContext executionContext) {
    ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    Map<ExternalIdBundle, Double> data = Maps.newHashMap(curveData.getDataPoints());
    Map<ExternalId, ExternalIdBundle> index = curveData.getIndex();
    for (YieldCurvePointShift shift : _shifts) {
      for (FixedIncomeStripWithSecurity strip : curveData.getCurveSpecification().getStrips()) {
        Period stripPeriod = strip.getTenor().getPeriod();
        Period shiftPeriod = shift.getTenor();
        ZonedDateTime stripTime = valuationTime.plus(stripPeriod);
        ZonedDateTime shiftStartTime = valuationTime.plus(shiftPeriod);

        if (stripTime.compareTo(shiftStartTime) == 0) {
          ExternalIdBundle bundle = index.get(strip.getSecurityIdentifier());
          boolean future = (strip.getInstrumentType() == StripInstrumentType.FUTURE);
          Double originalData = data.get(bundle);
          Double stripData;

          // futures are quoted the other way round from other instruments
          if (future) {
            stripData = 1 - originalData;
          } else {
            stripData = originalData;
          }
          Double shiftedData;

          if (_shiftType == ScenarioShiftType.RELATIVE) {
            // add shift amount to 1. i.e. 10.pc actualy means 'value * 1.1' and -10.pc means 'value * 0.9'
            shiftedData = stripData * (shift.getShift() + 1);
          } else {
            shiftedData = stripData + shift.getShift();
          }
          Double shiftedStripData;

          if (future) {
            shiftedStripData = 1 - shiftedData;
          } else {
            shiftedStripData = shiftedData;
          }
          data.put(bundle, shiftedStripData);
          s_logger.debug("Shifting data {}, tenor {} by {} from {} to {}",
                         strip.getSecurityIdentifier(), strip.getTenor(), shift.getShift(), originalData, shiftedStripData);
        }
      }
    }
    return new YieldCurveData(curveData.getCurveSpecification(), data);
  }

  @Override
  public Class<YieldCurveData> getExpectedType() {
    return YieldCurveData.class;
  }
}
