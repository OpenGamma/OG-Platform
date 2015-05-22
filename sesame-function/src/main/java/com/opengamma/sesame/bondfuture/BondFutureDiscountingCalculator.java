/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Discounting calculator for bond futures.
 */
public class BondFutureDiscountingCalculator implements BondFutureCalculator {

  /* calculators */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC =
      PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final FuturesPriceIssuerCalculator FPIC = FuturesPriceIssuerCalculator.getInstance();
    private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PVCSIC);
  private static final PV01CurveParametersCalculator<ParameterIssuerProviderInterface> PV01PC =
      new PV01CurveParametersCalculator<>(PVCSIC);
  
  private final InstrumentDerivative _derivative;
  private final ParameterIssuerProviderInterface _curves;
  
  public BondFutureDiscountingCalculator(BondFutureTrade bondFutureTrade,
                                         ParameterIssuerProviderInterface curves,
                                         BondAndBondFutureTradeConverter bondFutureTradeConverter,
                                         ZonedDateTime valuationTime) {
    _curves = curves;
    _derivative = createInstrumentDerivative(bondFutureTrade, bondFutureTradeConverter, valuationTime);
  }
  
  @Override
  public Result<MultipleCurrencyAmount> calculatePV() {
    MultipleCurrencyAmount pv = _derivative.accept(PVIC, _curves);
    return Result.success(pv);
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01() {
    ReferenceAmount<Pair<String, Currency>> pv01 = _derivative.accept(PV01PC, _curves);
    return Result.success(pv01);
  }

  @Override
  public Result<MultipleCurrencyParameterSensitivity> calculateBucketedPV01() {
    MultipleCurrencyParameterSensitivity sensitivity = PSSFC.calculateSensitivity(_derivative, _curves);
    return Result.success(sensitivity);
  }

  @Override
  public Result<Double> calculateSecurityModelPrice() {
    BondFuturesSecurity security = ((BondFuturesTransaction) _derivative).getUnderlyingSecurity();
    Double price = security.accept(FPIC, _curves);
    return Result.success(price);
  }

  private InstrumentDerivative createInstrumentDerivative(BondFutureTrade bondFutureTrade,
                                                          BondAndBondFutureTradeConverter converter,
                                                          ZonedDateTime valuationTime) {
    double lastPrice = 0;
    InstrumentDefinition<?> definition = converter.convert(bondFutureTrade.getTrade());
    return ((BondFuturesTransactionDefinition) definition).toDerivative(valuationTime, lastPrice);
  }
}
