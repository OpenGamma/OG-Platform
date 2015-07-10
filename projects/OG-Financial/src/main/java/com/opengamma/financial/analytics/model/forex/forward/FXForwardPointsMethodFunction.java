/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.fx.FXForwardPointsFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Base class for functions that calculate risk for FX forwards that use the FX forward rates directly.
 * 
 * @deprecated Use {@link FXForwardPointsFunction}
 */
@Deprecated
public abstract class FXForwardPointsMethodFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ComputationTargetType TYPE = FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY);

  /** The value requirement produced by this function */
  private final String _valueRequirementName;

  private ConfigDBFXForwardCurveSpecificationSource _fxForwardCurveSpecificationSource;
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  /**
   * @param valueRequirementName The value requirement name, not null
   */
  public FXForwardPointsMethodFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _fxForwardCurveSpecificationSource = ConfigDBFXForwardCurveSpecificationSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    if (now.isAfter(security.accept(ForexVisitors.getExpiryVisitor()))) {
      throw new OpenGammaRuntimeException("FX forward " + payCurrency.getCode() + "/" + receiveCurrency + " has expired");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String forwardCurveName = desiredValue.getConstraint(ValuePropertyNames.FORWARD_CURVE_NAME);
    final String fullPayCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullReceiveCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(payCurrency, receiveCurrency);
    final FXForwardCurveDefinition forwardCurveDefinition = _fxForwardCurveDefinitionSource.getDefinition(forwardCurveName, currencyPair.toString());
    if (forwardCurveDefinition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + forwardCurveName + " for target " + currencyPair);
    }
    final FXForwardCurveSpecification forwardCurveSpecification = _fxForwardCurveSpecificationSource.getSpecification(forwardCurveName, currencyPair.toString());
    if (forwardCurveSpecification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + forwardCurveName + " for target " + currencyPair);
    }
    final YieldAndDiscountCurve payCurve = getPayCurve(inputs, payCurrency, payCurveName, payCurveConfig);
    final YieldAndDiscountCurve receiveCurve = getReceiveCurve(inputs, receiveCurrency, receiveCurveName, receiveCurveConfig);
    final DoublesCurve fxForwardCurve = getFXForwardCurve(inputs, forwardCurveDefinition, forwardCurveSpecification, now.toLocalDate());
    final Map<String, Currency> curveCurrency = new HashMap<>();
    curveCurrency.put(fullPayCurveName, payCurrency);
    curveCurrency.put(fullReceiveCurveName, receiveCurrency);
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final YieldAndDiscountCurve[] curves;
    final String[] allCurveNames;
    curves = new YieldAndDiscountCurve[] {payCurve, receiveCurve };
    allCurveNames = new String[] {fullPayCurveName, fullReceiveCurveName };
    // Implementation note: The ForexSecurityConverter create the Forex with currency order pay/receive. The curve are passed in the same order.
    final ForexSecurityConverter converter = new ForexSecurityConverter(baseQuotePairs);
    final InstrumentDefinition<?> definition = security.accept(converter);
    final Forex forex = (Forex) definition.toDerivative(now);
    final FXForwardCurveInstrumentProvider provider = forwardCurveSpecification.getCurveInstrumentProvider();
    final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, provider.getSpotInstrument());
    final double spotFX;
    if (baseQuotePairs.getCurrencyPair(receiveCurrency, payCurrency).getBase().equals(receiveCurrency)) {
      spotFX = (Double) inputs.getValue(spotRequirement);
    } else {
      spotFX = 1. / (Double) inputs.getValue(spotRequirement);
    }
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(receiveCurrency, payCurrency, spotFX);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(fxMatrix, allCurveNames, curves);
    return getResult(forex, yieldCurves, fxForwardCurve, target, desiredValues, inputs, executionContext, forwardCurveDefinition);
  }

  /**
   * Performs the calculation.
   * 
   * @param fxForward The FX forward
   * @param data The yield curve data
   * @param fxForwardPoints A curve containing FX forward rates
   * @param target The computation target
   * @param desiredValues The desired values
   * @param inputs The function inputs
   * @param executionContext The execution context
   * @param fxForwardCurveDefinition The definition of the FX forward curve
   * @return A set of computed values
   */
  protected abstract Set<ComputedValue> getResult(Forex fxForward, YieldCurveBundle data, DoublesCurve fxForwardPoints, ComputationTarget target, Set<ValueRequirement> desiredValues,
      FunctionInputs inputs, FunctionExecutionContext executionContext, FXForwardCurveDefinition fxForwardCurveDefinition);

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String calculationMethod = desiredValue.getConstraint(ValuePropertyNames.CALCULATION_METHOD);
    if (!CalculationPropertyNamesAndValues.FORWARD_POINTS.equals(calculationMethod)) {
      return null;
    }
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveConfigNames = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveConfigNames == null || payCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveConfigNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveConfigNames == null || receiveCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.FORWARD_CURVE_NAME);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(payCurrency, receiveCurrency);
    final FXForwardCurveSpecification specification = _fxForwardCurveSpecificationSource.getSpecification(forwardCurveName, currencyPair.toString());
    if (specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + forwardCurveName + " for target " + currencyPair);
    }
    final String payCurveName = payCurveNames.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final String payCurveCalculationConfig = payCurveConfigNames.iterator().next();
    final String receiveCurveCalculationConfig = receiveCurveConfigNames.iterator().next();
    final ValueProperties fxForwardCurveProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, forwardCurveName).get();
    final ValueRequirement fxForwardCurveRequirement = new ValueRequirement(ValueRequirementNames.FX_FORWARD_POINTS_CURVE_MARKET_DATA,
        ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair), fxForwardCurveProperties);
    final ValueRequirement payFundingCurve = getPayCurveRequirement(payCurveName, payCurrency, payCurveCalculationConfig);
    final ValueRequirement receiveFundingCurve = getReceiveCurveRequirement(receiveCurveName, receiveCurrency, receiveCurveCalculationConfig);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL);
    final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
    final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, provider.getSpotInstrument());
    return Sets.newHashSet(payFundingCurve, receiveFundingCurve, pairQuoteRequirement, fxForwardCurveRequirement, spotRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    String forwardCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement requirement = entry.getValue();
      final ValueProperties constraints = requirement.getConstraints();
      if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        if (constraints.getProperties().contains(ValuePropertyNames.PAY_CURVE)) {
          payCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        } else if (constraints.getProperties().contains(ValuePropertyNames.RECEIVE_CURVE)) {
          receiveCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        }
      } else if (requirement.getValueName().equals(ValueRequirementNames.FX_FORWARD_POINTS_CURVE_MARKET_DATA)) {
        forwardCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
      }
    }
    assert payCurveName != null;
    assert receiveCurveName != null;
    assert forwardCurveName != null;
    final ValueProperties properties = getResultProperties(target, payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig, forwardCurveName).get();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final String forwardCurveName);

  protected abstract ValueProperties.Builder getResultProperties(final ValueRequirement desiredValue, final ComputationTarget target);

  /**
   * Gets the value requirement name.
   * 
   * @return The value requirement name
   */
  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  /**
   * Gets the requirement for the pay curve.
   * 
   * @param curveName The pay curve name
   * @param currency The pay currency
   * @param curveCalculationConfigName The pay curve calculation configuration name
   * @return The pay curve requirement
   */
  protected static ValueRequirement getPayCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.PAY_CURVE);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  /**
   * Gets the pay curve.
   * 
   * @param inputs The function inputs
   * @param currency The pay currency
   * @param curveName The pay curve name
   * @param curveCalculationConfig The pay curve calculation configuration name
   * @return The pay curve
   */
  protected static YieldAndDiscountCurve getPayCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getPayCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }

  /**
   * Gets the requirement for the receive curve.
   * 
   * @param curveName The receive curve name
   * @param currency The receive currency
   * @param curveCalculationConfigName The receive curve calculation configuration name
   * @return The receive curve requirement
   */
  protected static ValueRequirement getReceiveCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.RECEIVE_CURVE);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  /**
   * Gets the receive curve.
   * 
   * @param inputs The function inputs
   * @param currency The receive currency
   * @param curveName The receive curve name
   * @param curveCalculationConfig The receive curve calculation configuration name
   * @return The receive curve
   */
  protected static YieldAndDiscountCurve getReceiveCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getReceiveCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }

  protected static DoublesCurve getFXForwardCurve(final FunctionInputs inputs, final FXForwardCurveDefinition definition, final FXForwardCurveSpecification specification, final LocalDate now) {
    final Object fxForwardCurveObject = inputs.getValue(ValueRequirementNames.FX_FORWARD_POINTS_CURVE_MARKET_DATA);
    if (fxForwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get FX forward curve data");
    }
    @SuppressWarnings("unchecked")
    final Map<ExternalId, Double> dataPoints = (Map<ExternalId, Double>) fxForwardCurveObject;
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList fxList = new DoubleArrayList();
    for (final Tenor tenor : definition.getTenors()) {
      final Double fxForward = dataPoints.get(specification.getCurveInstrumentProvider().getInstrument(now, tenor));
      if (fxForward == null) {
        throw new OpenGammaRuntimeException("Could not get FX forward rate for " + tenor);
      }
      tList.add(DateUtils.getDifferenceInYears(now, now.plus(tenor.getPeriod())));
      fxList.add(fxForward);
    }
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    return InterpolatedDoublesCurve.from(tList.toDoubleArray(), fxList.toDoubleArray(), interpolator);
  }

}
