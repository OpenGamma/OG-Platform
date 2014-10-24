/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaMultiCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculator for Discounting FRAs.
 */
public class DiscountingFRACalculator implements FRACalculator {

  /**
   * Calculator for present value.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /**
   * Calculator for par rate.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  /**
   * Calculator for PV01
   */
  private static final PV01CurveParametersCalculator<ParameterProviderInterface> PV01C =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());

  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> BUCKETED_PV01_CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);
  /** The calculator which will compute intra-curve gammas */
  private static final CrossGammaMultiCurveCalculator CGC = new CrossGammaMultiCurveCalculator(PVCSDC);
  /** Matrix algebra tooling to permit matrix manipulation */
  private static final CommonsMatrixAlgebra MA = new CommonsMatrixAlgebra();

  /**
   * Provides scaling to/from basis points.
   */
  private static final double BASIS_POINT_FACTOR = 1.0E-4;

  /**
   * Derivative form of the security.
   */
  private final InstrumentDerivative _derivative;

  /**
   * The multicurve bundle.
   */
  private final MulticurveProviderDiscount _bundle;

  /**
   * The curve building block bundle.
   */
  private final CurveBuildingBlockBundle _curveBuildingBlockBundle;

  /**
   * The curve definitions
   */
  private final Map<String, CurveDefinition> _curveDefinitions;

  /**
   * Creates a calculator for a FRA.
   *
   * @param security the fra to calculate values for, not null
   * @param bundle the multicurve bundle, including the curves, not null
   * @param fraConverter converter for transforming a fra into its InstrumentDefinition form, not null
   * @param valuationTime the ZonedDateTime, not null
   * @param curveBuildingBlockBundle the curve block building bundle, not null
   * @param curveDefinitions the curve definitions, not null
   */
  public DiscountingFRACalculator(FRASecurity security,
                                  MulticurveProviderDiscount bundle,
                                  FRASecurityConverter fraConverter,
                                  ZonedDateTime valuationTime,
                                  CurveBuildingBlockBundle curveBuildingBlockBundle,
                                  Map<String, CurveDefinition> curveDefinitions) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(fraConverter, "fraConverter");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _derivative = createInstrumentDerivative(security, fraConverter, valuationTime);
    _bundle = ArgumentChecker.notNull(bundle, "bundle");
    _curveBuildingBlockBundle = ArgumentChecker.notNull(curveBuildingBlockBundle, "curveBuildingBlockBundle");
    _curveDefinitions = ArgumentChecker.notNull(curveDefinitions, "curveDefinitions");
  }

  /**
   * Creates a calculator for a FRA.
   *
   * @param security the fra to calculate values for, not null
   * @param bundle the multicurve bundle, including the curves, not null
   * @param fraConverter converter for transforming a fra into its InstrumentDefinition form, not null
   * @param valuationTime the ZonedDateTime, not null
   * @param definitionConverter converter for transforming the instrumentDefinition into the Derivative, not null
   * @param fixings the HistoricalTimeSeriesBundle, a collection of historical time-series objects
   * @param curveBuildingBlockBundle the curve block building bundle, not null
   * @param curveDefinitions the curve definitions, not null
   */
  public DiscountingFRACalculator(ForwardRateAgreementSecurity security,
                                  MulticurveProviderDiscount bundle,
                                  FRASecurityConverter fraConverter,
                                  ZonedDateTime valuationTime,
                                  FixedIncomeConverterDataProvider definitionConverter,
                                  HistoricalTimeSeriesBundle fixings,
                                  CurveBuildingBlockBundle curveBuildingBlockBundle,
                                  Map<String, CurveDefinition> curveDefinitions) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(fraConverter, "fraConverter");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(definitionConverter, "definitionConverter");
    ArgumentChecker.notNull(fixings, "fixings");
    _derivative = createInstrumentDerivative(security, fraConverter, valuationTime, definitionConverter, fixings);
    _bundle = ArgumentChecker.notNull(bundle, "bundle");
    _curveBuildingBlockBundle = ArgumentChecker.notNull(curveBuildingBlockBundle, "curveBuildingBlockBundle");
    _curveDefinitions = ArgumentChecker.notNull(curveDefinitions, "curveDefinitions");
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV() {
    return Result.success(calculateResult(PVDC));
  }
  
  @Override
  public Result<MultipleCurrencyAmount> calculatePv(MulticurveProviderInterface bundle) {
    ArgumentChecker.notNull(bundle, "curve bundle");
    return Result.success(_derivative.accept(PVDC, bundle));
  }

  @Override
  public Result<Double> calculateRate() {
    return Result.success(calculateResult(PRDC));
  }

  private <T> T calculateResult(InstrumentDerivativeVisitor<ParameterProviderInterface, T> calculator) {
    return _derivative.accept(calculator, _bundle);
  }

  private InstrumentDerivative createInstrumentDerivative(FRASecurity security,
                                                          FRASecurityConverter fraConverter,
                                                          ZonedDateTime valuationTime) {
    InstrumentDefinition<?> definition = security.accept(fraConverter);
    return definition.toDerivative(valuationTime);
  }
  
  private InstrumentDerivative createInstrumentDerivative(ForwardRateAgreementSecurity security,
      FRASecurityConverter fraConverter,
      ZonedDateTime valuationTime,
      FixedIncomeConverterDataProvider definitionConverter,
      HistoricalTimeSeriesBundle fixings) {
    InstrumentDefinition<?> definition = security.accept(fraConverter);
    return definitionConverter.convert(security, definition, valuationTime, fixings);
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01() {
    return Result.success(calculateResult(PV01C));
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01() {
    MultipleCurrencyParameterSensitivity sensitivity = BUCKETED_PV01_CALCULATOR
        .fromInstrument(_derivative, _bundle, _curveBuildingBlockBundle)
        .multipliedBy(BASIS_POINT_FACTOR);
    Map<Pair<String, Currency>, DoubleLabelledMatrix1D> labelledMatrix1DMap = new HashMap<>();
    for (Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivity.getSensitivities().entrySet()) {
      CurveDefinition curveDefinition = _curveDefinitions.get(entry.getKey().getFirst());
      DoubleLabelledMatrix1D matrix = MultiCurveUtils.getLabelledMatrix(entry.getValue(), curveDefinition);
      labelledMatrix1DMap.put(entry.getKey(), matrix);
    }
    return Result.success(BucketedCurveSensitivities.of(labelledMatrix1DMap));
  }

  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedGamma() {
    HashMap<String, DoubleMatrix2D> crossGammas = CGC.calculateCrossGammaIntraCurve(_derivative, _bundle);
    Map<String, DoubleLabelledMatrix2D> labelledMatrix2DMap = new HashMap<>();

    for (Map.Entry<String, DoubleMatrix2D> entry : crossGammas.entrySet()) {
      CurveDefinition curveDefinition = _curveDefinitions.get(entry.getKey());
      //Values returned from analytics need to be scaled appropriately: multiplied by 1 bp ^ 1bp
      DoubleMatrix2D scaledValues = (DoubleMatrix2D) MA.scale(entry.getValue(), BASIS_POINT_FACTOR * BASIS_POINT_FACTOR);
      DoubleLabelledMatrix2D matrix = MultiCurveUtils.getLabelledMatrix2D(scaledValues, curveDefinition);
      labelledMatrix2DMap.put(entry.getKey(), matrix);
    }
    return Result.success(BucketedCrossSensitivities.of(labelledMatrix2DMap));
  }

}
