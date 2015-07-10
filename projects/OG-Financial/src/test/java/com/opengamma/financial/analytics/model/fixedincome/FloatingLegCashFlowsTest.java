/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link FloatingLegCashFlows}.
 */
public class FloatingLegCashFlowsTest {

  @Test
  public void testSerialisation() {
    
    LocalDate valDate = LocalDate.of(2014, 3, 4);
    
    List<LocalDate> accrualStartDates = Lists.newArrayList(LocalDate.of(2014, 3, 18));
    List<LocalDate> accrualEndDates = Lists.newArrayList(LocalDate.of(2014, 6, 18));
    List<Double> discountFactors = Lists.newArrayList(0.9995);
    List<LocalDate> paymentDates = accrualEndDates;
    List<Double> paymentTimes = Lists.newArrayList(TimeCalculator.getTimeBetween(valDate, paymentDates.get(0)));
    List<Double> paymentFractions = Lists.newArrayList(TimeCalculator.getTimeBetween(accrualStartDates.get(0),
                                                                                     accrualEndDates.get(0)));
    List<Double> fixedRates = Lists.newArrayList(0.02);
    List<CurrencyAmount> notionals = Lists.newArrayList(CurrencyAmount.of(Currency.USD, 1_000_000));
    List<CurrencyAmount> paymentAmounts = Lists.newArrayList(notionals.get(0)
                                                             .multipliedBy(fixedRates.get(0))
                                                             .multipliedBy(paymentFractions.get(0)));
    
    List<Double> spreads = new ArrayList<>();
    spreads.add(null);
    List<Double> gearing = new ArrayList<>();
    gearing.add(null);
    List<Set<Tenor>> indexTenors = new ArrayList<>();
    indexTenors.add(Sets.newHashSet(Tenor.THREE_MONTHS));
    FloatingLegCashFlows flows = new FloatingLegCashFlows(accrualStartDates,
                                                          accrualEndDates,
                                                          paymentFractions,
                                                          accrualStartDates,
                                                          accrualEndDates,
                                                          paymentFractions,
                                                          fixedRates,
                                                          fixedRates,
                                                          paymentDates,
                                                          paymentTimes,
                                                          discountFactors,
                                                          paymentAmounts,
                                                          notionals,
                                                          spreads,
                                                          gearing,
                                                          indexTenors);

    FudgeMsg msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(flows).getMessage();
    OpenGammaFudgeContext.getInstance().fromFudgeMsg(msg);
  }
}
