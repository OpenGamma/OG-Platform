/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public abstract class ForexLocalVolatilityPDEGridFunction extends LocalVolatilityPDEGridFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexLocalVolatilityPDEGridFunction.class);

  public ForexLocalVolatilityPDEGridFunction() {
    super(InstrumentTypeProperties.FOREX);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  protected ComputationTargetReference getTargetForUnderlyings(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency()));
  }

  @Override
  protected EuropeanVanillaOption getOption(final FinancialSecurity security, final ZonedDateTime date, final CurrencyPair currencyPair) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) security;
    final Currency putCurrency = fxOption.getPutCurrency();
    double strike;
    if (putCurrency.equals(currencyPair.getBase())) {
      strike = fxOption.getCallAmount() / fxOption.getPutAmount();
    } else {
      strike = fxOption.getPutAmount() / fxOption.getCallAmount();
    }
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double t = actAct.getDayCountFraction(date, fxOption.getExpiry().getExpiry());
    return new EuropeanVanillaOption(strike, t, true); //TODO this shouldn't be hard coded to a call
  }

  @Override
  protected ValueRequirement getUnderlyingVolatilityDataRequirement(final String surfaceName, final ComputationTargetReference target) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target,
        ValueProperties
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE).get());
  }

  //TODO
  @Override
  protected SmileSurfaceDataBundle getData(final FunctionInputs inputs, final ValueRequirement volDataRequirement, final ValueRequirement forwardCurveRequirement) {
    final Object volatilitySurfaceObject = inputs.getValue(volDataRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + volDataRequirement);
    }
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    final Tenor[] tenors = fxVolatilitySurface.getXs();
    Arrays.sort(tenors);
    final Pair<Number, FXVolQuoteType>[] quotes = fxVolatilitySurface.getYs();
    final Number[] deltaValues = getDeltaValues(quotes);
    final int nExpiries = tenors.length;
    final int nDeltas = deltaValues.length - 1;
    final double[] expiries = new double[nExpiries];
    final double[] deltas = new double[nDeltas];
    final double[] atms = new double[nExpiries];
    final double[][] riskReversals = new double[nDeltas][nExpiries];
    final double[][] strangle = new double[nDeltas][nExpiries];
    for (int i = 0; i < nExpiries; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      final Double atm = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(deltaValues[0], FXVolQuoteType.ATM));
      if (atm == null) {
        throw new OpenGammaRuntimeException("Could not get ATM volatility data for surface");
      }
      expiries[i] = t;
      atms[i] = atm;
    }
    for (int i = 0; i < nDeltas; i++) {
      final Number delta = deltaValues[i + 1];
      if (delta != null) {
        deltas[i] = delta.doubleValue() / 100.;
        final DoubleArrayList riskReversalList = new DoubleArrayList();
        final DoubleArrayList strangleList = new DoubleArrayList();
        for (int j = 0; j < nExpiries; j++) {
          final Double rr = fxVolatilitySurface.getVolatility(tenors[j], ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL));
          final Double s = fxVolatilitySurface.getVolatility(tenors[j], ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY));
          if (rr != null && s != null) {
            riskReversalList.add(rr);
            strangleList.add(s);
          } else {
            s_logger.info("Had a null value for tenor number " + j);
          }
        }
        riskReversals[i] = riskReversalList.toDoubleArray();
        strangle[i] = strangleList.toDoubleArray();
      }
    }
    final boolean isCallData = true; //TODO this shouldn't be hard-coded
    return new ForexSmileDeltaSurfaceDataBundle(forwardCurve, expiries, deltas, atms, riskReversals, strangle, isCallData);
  }

  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return ((double) period.getMonths()) / 12;
    }
    if (period.getDays() != 0) {
      return ((double) period.getDays()) / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  private Number[] getDeltaValues(final Pair<Number, FXVolQuoteType>[] quotes) {
    final TreeSet<Number> values = new TreeSet<Number>();
    for (final Pair<Number, FXVolQuoteType> pair : quotes) {
      values.add(pair.getFirst());
    }
    return values.toArray((Number[]) Array.newInstance(Number.class, values.size()));
  }
}
