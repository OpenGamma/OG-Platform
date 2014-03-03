/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.ExternalId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * Constructs a single yield curve and its Jacobian from exogenously-supplied yield curves and a {@link FXForwardCurveDefinition} and {@link FXForwardCurveSpecification}.
 * 
 * @deprecated This function uses configuration objects that have been superseded. Use functions that descend from {@link MultiCurveFunction}. Curves that use FX forwards directly in
 *             {@link CurveDefinition} (see {@link FXForwardNode}) are constructed in these classes.
 */
@Deprecated
public class FXImpliedYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the calculation method */
  public static final String FX_IMPLIED = "FXImplied";
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXImpliedYieldCurveFunction.class);
  /** Calculates the par rate */
  private static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  /** Calculates the sensitivity of the par rate to the curves */
  private static final ParRateCurveSensitivityCalculator PAR_RATE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  /** The matrix algebra used for matrix inversion. */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new ColtMatrixAlgebra();
  /** The business day convention used for FX forward dates computation **/
  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** The curve calculation configuration source */
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;
  /** The FX forward curve specification source */
  private ConfigDBFXForwardCurveSpecificationSource _fxForwardCurveSpecificationSource;
  /** The FX forward curve definition source */
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
    _fxForwardCurveSpecificationSource = ConfigDBFXForwardCurveSpecificationSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    String domesticCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Currency domesticCurrency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    Object foreignCurveObject = null;
    Currency foreignCurrency = null;
    String foreignCurveName = null;
    for (final ComputedValue values : inputs.getAllValues()) {
      final ValueSpecification specification = values.getSpecification();
      if (specification.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        foreignCurveObject = values.getValue();
        foreignCurrency = Currency.of(specification.getTargetSpecification().getUniqueId().getValue());
        foreignCurveName = specification.getProperty(ValuePropertyNames.CURVE);
        break;
      }
    }
    if (foreignCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get foreign yield curve");
    }
    final Object foreignJacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (foreignJacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get foreign Jacobian");
    }
    final double[][] arrayForeignJacobian = FunctionUtils.decodeJacobian(foreignJacobianObject);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final String relativeToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final String iterationsName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final int iterations = Integer.parseInt(iterationsName);
    final String decompositionName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Currency baseCurrency = currencyPairs.getCurrencyPair(domesticCurrency, foreignCurrency).getBase();
    boolean invertFXQuotes;
    if (baseCurrency.equals(foreignCurrency)) {
      invertFXQuotes = false;
    } else {
      invertFXQuotes = true;
    }
    if (domesticCurveName == null) {
      final String[] curveNames = _curveCalculationConfigSource.getConfig(curveCalculationConfigName).getYieldCurveNames();
      if (curveNames.length != 1) {
        throw new OpenGammaRuntimeException("Can only handle a single curve at the moment");
      }
      domesticCurveName = curveNames[0];
    }
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + domesticCurveName + " for target " + currencyPair);
    }
    final FXForwardCurveSpecification specification = _fxForwardCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + domesticCurveName + " for target " + currencyPair);
    }
    final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
    final ValueRequirement spotRequirement = getSpotRequirement(provider, currencyPair);
    if (inputs.getValue(spotRequirement) == null) {
      throw new OpenGammaRuntimeException("Could not get value for spot; requirement was " + spotRequirement);
    }
    final double spotFX = invertFXQuotes ? 1 / (Double) inputs.getValue(spotRequirement) : (Double) inputs.getValue(spotRequirement);
    final Object dataObject = inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA);
    if (dataObject == null) {
      throw new OpenGammaRuntimeException("Could not get FX forward market data");
    }
    final YieldAndDiscountCurve foreignCurve = (YieldAndDiscountCurve) foreignCurveObject;
    @SuppressWarnings("unchecked")
    final Map<ExternalId, Double> fxForwardData = (Map<ExternalId, Double>) dataObject;
    final DoubleArrayList marketValues = new DoubleArrayList();
    final DoubleArrayList nodeTimes = new DoubleArrayList();
    final DoubleArrayList initialRatesGuess = new DoubleArrayList();
    final String fullDomesticCurveName = domesticCurveName + "_" + domesticCurrency.getCode();
    final String fullForeignCurveName = foreignCurveName + "_" + foreignCurrency.getCode();
    final List<InstrumentDerivative> derivatives = new ArrayList<>();
    int nInstruments = 0;
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Calendar calendar = CalendarUtils.getCalendar(holidaySource, domesticCurrency, foreignCurrency);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
    final FXSpotConvention fxSpotConvention = conventionSource.getSingle(ExternalId.of("CONVENTION", "FX Spot"), FXSpotConvention.class);
    final int spotLag = fxSpotConvention.getSettlementDays();
    final ExternalId conventionSettlementRegion = fxSpotConvention.getSettlementRegion();
    ZonedDateTime spotDate;
    if (spotLag == 0 && conventionSettlementRegion == null) {
      spotDate = now; //This preserves the old behaviour that ignored holidays and settlement days.
    } else {
      spotDate = ScheduleCalculator.getAdjustedDate(now, spotLag, calendar);
    }
    for (final Tenor tenor : definition.getTenors()) {
      final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
      if (fxForwardData.containsKey(identifier)) {
        final ZonedDateTime paymentDate;
        if (spotLag == 0 && conventionSettlementRegion == null) {
          paymentDate = now.plus(tenor.getPeriod()); //This preserves the old behaviour that ignored holidays and settlement days.
        } else {
          paymentDate = ScheduleCalculator.getAdjustedDate(spotDate, tenor.getPeriod(), MOD_FOL, calendar, true);
        }
        final double paymentTime = TimeCalculator.getTimeBetween(now, paymentDate);
        final double forwardFX = invertFXQuotes ? 1 / fxForwardData.get(identifier) : fxForwardData.get(identifier);
        derivatives.add(getFXForward(domesticCurrency, foreignCurrency, paymentTime, spotFX, forwardFX, fullDomesticCurveName, fullForeignCurveName));
        marketValues.add(forwardFX);
        nodeTimes.add(paymentTime);
        if (nInstruments > 1 && CompareUtils.closeEquals(nodeTimes.get(nInstruments - 1), paymentTime, 1e-12)) {
          throw new OpenGammaRuntimeException("FX forward with tenor " + tenor + " has already been added - will lead to equal nodes in the curve. Remove one of these tenors.");
        }
        nInstruments++;
        initialRatesGuess.add(0.02);
      }
    }
    final YieldCurveBundle knownCurve = new YieldCurveBundle(new String[] {fullForeignCurveName }, new YieldAndDiscountCurve[] {foreignCurve });
    final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<>();
    curveKnots.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    curveNodes.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
    interpolators.put(fullDomesticCurveName, interpolator);
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(foreignCurrency, domesticCurrency, spotFX);
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurve, curveNodes, interpolators,
        useFiniteDifference, fxMatrix);
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_RATE_CALCULATOR);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PAR_RATE_SENSITIVITY_CALCULATOR);
    final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
    final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
    final YieldCurve curve = YieldCurve.from(InterpolatedDoublesCurve.from(nodeTimes.toDoubleArray(), fittedYields, interpolator));
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, domesticCurveName, absoluteToleranceName, relativeToleranceName, iterationsName,
        decompositionName, useFiniteDifferenceName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final ValueProperties properties = getProperties(curveCalculationConfigName, absoluteToleranceName, relativeToleranceName, iterationsName, decompositionName, useFiniteDifferenceName,
        interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    // Implementation note: computes transition Dpf pd: derivative of the current (domestic) curve parameter to the previous (foreign) currency curves
    // For details on the computation, see "Multi-curve Framework with Collateral-3.5 Keeping track of transition matrices", OpenGamma Quantitative Research 13, May 2013.
    final MultipleYieldCurveFinderDataBundle dataAllCurves = MultipleYieldCurveFinderDataBundle.withAllCurves(derivatives, marketValues.toDoubleArray(), knownCurve, curveNodes,
        interpolators, useFiniteDifference, fxMatrix);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculatorAllCurves = new MultipleYieldCurveFinderJacobian(dataAllCurves, PAR_RATE_SENSITIVITY_CALCULATOR);
    final DoubleMatrix2D jacobianMatrixAllCurves = jacobianCalculatorAllCurves.evaluate(new DoubleMatrix1D(fittedYields));
    // Order is: previous curves (domestic), current curves (foreign)
    final DoubleMatrix2D jacobianMatrixInverse = MATRIX_ALGEBRA.getInverse(jacobianMatrix);
    final int nbLine = nodeTimes.size();
    final int nbCol = dataAllCurves.getTotalNodes() - nodeTimes.size();
    final double[][] sensiPreviousCurves1 = new double[nbLine][nbCol];
    for (int loopline = 0; loopline < nbLine; loopline++) {
      for (int loopcol = 0; loopcol < nbCol; loopcol++) {
        sensiPreviousCurves1[loopline][loopcol] = -jacobianMatrixAllCurves.getData()[loopline][loopcol];
      }
    }
    final DoubleMatrix2D sensiQuoteToParameterPreviousCurvesMat = new DoubleMatrix2D(sensiPreviousCurves1);
    final DoubleMatrix2D sensiParameterToParameterPreviousCurvesMat = (DoubleMatrix2D) MATRIX_ALGEBRA.multiply(jacobianMatrixInverse, sensiQuoteToParameterPreviousCurvesMat);
    final int startIndex = 0;
    final int nbIndex = sensiParameterToParameterPreviousCurvesMat.getNumberOfColumns();
    final double[][] arrayForeignJacobianDiscount = new double[nbIndex][nbIndex];
    for (int loopline = 0; loopline < nbIndex; loopline++) {
      for (int loopcol = 0; loopcol < nbIndex; loopcol++) {
        arrayForeignJacobianDiscount[loopline][loopcol] = arrayForeignJacobian[startIndex + loopline][startIndex + loopcol];
      }
    }
    final DoubleMatrix2D foreignJacobian = new DoubleMatrix2D(arrayForeignJacobianDiscount);
    final DoubleMatrix2D foreignJacobianInverse = MATRIX_ALGEBRA.getInverse(foreignJacobian);
    final DoubleMatrix2D fxImpliedTransitionMatrix = (DoubleMatrix2D) MATRIX_ALGEBRA.multiply(sensiParameterToParameterPreviousCurvesMat, foreignJacobianInverse);
    final Set<ComputedValue> result = new HashSet<>();
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties), jacobianMatrix.getData()));
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties), curve));
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, targetSpec, properties), fxImpliedTransitionMatrix.getData()));
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveProperties = getCurveProperties();
    final ValueProperties properties = getProperties();
    final ValueSpecification curve = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), properties);
    final ValueSpecification fxImpliedTransitionMatrix = new ValueSpecification(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, target.toSpecification(), properties);
    return Sets.newHashSet(curve, jacobian, fxImpliedTransitionMatrix);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String domesticCurveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (domesticCurveCalculationConfigName == null) {
      return null;
    }
    final MultiCurveCalculationConfig domesticCurveCalculationConfig = _curveCalculationConfigSource.getConfig(domesticCurveCalculationConfigName);
    if (domesticCurveCalculationConfig == null) {
      s_logger.error("Could not get domestic curve calculation config called {}", domesticCurveCalculationConfigName);
      return null;
    }
    if (!domesticCurveCalculationConfig.getCalculationMethod().equals(FX_IMPLIED)) {
      return null;
    }
    final String rootFinderAbsoluteTolerance = constraints.getStrictValue(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (rootFinderAbsoluteTolerance == null) {
      return null;
    }
    final String rootFinderRelativeTolerance = constraints.getStrictValue(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    if (rootFinderRelativeTolerance == null) {
      return null;
    }
    final String maxIterations = constraints.getStrictValue(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    if (maxIterations == null) {
      return null;
    }
    final String decomposition = constraints.getStrictValue(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
    if (decomposition == null) {
      return null;
    }
    final String useFiniteDifference = constraints.getStrictValue(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
    if (useFiniteDifference == null) {
      return null;
    }
    final String interpolatorName = constraints.getStrictValue(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorName == null) {
      return null;
    }
    final String leftExtrapolatorName = constraints.getStrictValue(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorName == null) {
      return null;
    }
    final String rightExtrapolatorName = constraints.getStrictValue(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorName == null) {
      return null;
    }
    if (domesticCurveCalculationConfig.getExogenousConfigData() == null) {
      s_logger.error("Need an externally-supplied curve to imply data; tried {}", domesticCurveCalculationConfigName);
      return null;
    }
    if (domesticCurveCalculationConfig.getYieldCurveNames().length != 1) {
      s_logger.error("Can only handle one curve at the moment");
      return null;
    }
    if (!domesticCurveCalculationConfig.getTarget().equals(target.toSpecification())) {
      s_logger.info("Invalid target, was {} - expected {}", target, domesticCurveCalculationConfig.getTarget());
      return null;
    }
    final Map<String, String[]> exogenousConfigs = domesticCurveCalculationConfig.getExogenousConfigData();
    if (exogenousConfigs.size() != 1) {
      s_logger.error("Can only handle curves with one foreign curve config");
      return null;
    }
    final Map.Entry<String, String[]> foreignCurveConfigNames = exogenousConfigs.entrySet().iterator().next();
    final MultiCurveCalculationConfig foreignConfig = _curveCalculationConfigSource.getConfig(foreignCurveConfigNames.getKey());
    if (foreignConfig == null) {
      s_logger.error("Foreign config was null; tried {}", foreignCurveConfigNames.getKey());
      return null;
    }
    final ComputationTargetSpecification foreignCurrencySpec = foreignConfig.getTarget();
    if (!foreignCurrencySpec.getType().isTargetType(ComputationTargetType.CURRENCY)) {
      s_logger.error("Can only handle curves with currencies as ids at the moment");
      return null;
    }
    final String domesticCurveName = domesticCurveCalculationConfig.getYieldCurveNames()[0];
    final Currency domesticCurrency = target.getValue(ComputationTargetType.CURRENCY);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final Currency foreignCurrency = ComputationTargetType.CURRENCY.resolve(foreignCurrencySpec.getUniqueId());
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      s_logger.error("Couldn't find FX forward curve definition called " + domesticCurveName + " with target " + currencyPair);
      return null;
    }
    final FXForwardCurveSpecification fxForwardCurveSpec = _fxForwardCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (fxForwardCurveSpec == null) {
      s_logger.error("Couldn't find FX forward curve specification called " + domesticCurveName + " with target " + currencyPair);
      return null;
    }
    final ValueProperties fxForwardCurveProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, domesticCurveName).get();
    final String foreignCurveName = foreignCurveConfigNames.getValue()[0];
    final ValueProperties foreignCurveProperties = getForeignCurveProperties(foreignConfig, foreignCurveName);
    final ValueProperties foreignJacobianProperties = getForeignJacobianProperties(foreignConfig);
    final FXForwardCurveInstrumentProvider provider = fxForwardCurveSpec.getCurveInstrumentProvider();
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(foreignCurrency);
    requirements.add(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair),
        fxForwardCurveProperties));
    requirements.add(getSpotRequirement(provider, currencyPair));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, currencyTarget, foreignCurveProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, currencyTarget, foreignJacobianProperties));
    return requirements;
  }

  /**
   * Gets the properties for the foreign curve i.e. the fixed yield curve that is being used to imply the yield curve.
   * 
   * @param foreignConfig The foreign curve configuration name
   * @param foreignCurveName The foreign curve name
   * @return The foreign curve properties
   */
  private static ValueProperties getForeignCurveProperties(final MultiCurveCalculationConfig foreignConfig, final String foreignCurveName) {
    return ValueProperties.builder().with(ValuePropertyNames.CURVE, foreignCurveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, foreignConfig.getCalculationConfigName())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, foreignConfig.getCalculationMethod()).get();
  }

  /**
   * Gets the properties for the foreign curve configuration Jacobian i.e. the Jacobian that is being used to imply the yield curve.
   * 
   * @param foreignConfig The foreign curve configuration name
   * @return The foreign Jacobian properties
   */
  private static ValueProperties getForeignJacobianProperties(final MultiCurveCalculationConfig foreignConfig) {
    return ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, foreignConfig.getCalculationConfigName())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, foreignConfig.getCalculationMethod()).get();
  }

  /**
   * Gets the properties of the implied yield curve with no values set.
   * 
   * @return The properties
   */
  private ValueProperties getCurveProperties() {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE).withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME).get();
  }

  /**
   * Gets the properties of the Jacobian with no values set.
   * 
   * @return The properties.
   */
  private ValueProperties getProperties() {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE).withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME).get();
  }

  /**
   * Gets the properties of the implied yield curve.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param curveName The curve name
   * @param absoluteTolerance The absolute tolerance
   * @param relativeTolerance The relative tolerance
   * @param maxIterations The maximum number of iterations
   * @param decomposition The decomposition
   * @param useFiniteDifference True if finite difference was used to calculate derivatives
   * @param interpolatorName The interpolator name
   * @param leftExtrapolatorName The left extrapolator name
   * @param rightExtrapolatorName The right extrapolator name
   * @return The curve properties
   */
  private ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION, decomposition)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
  }

  /**
   * Gets the properties of the Jacobian for the implied yield curve.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param absoluteTolerance The absolute tolerance
   * @param relativeTolerance The relative tolerance
   * @param maxIterations The maximum number of iterations
   * @param decomposition The decomposition
   * @param useFiniteDifference True if finite difference was used to calculate derivatives
   * @param interpolatorName The interpolator name
   * @param leftExtrapolatorName The left extrapolator name
   * @param rightExtrapolatorName The right extrapolator name
   * @return The Jacobian properties
   */
  private ValueProperties getProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance, final String maxIterations,
      final String decomposition, final String useFiniteDifference, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION, decomposition)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
  }

  /**
   * @param ccy1 The domestic currency
   * @param ccy2 The foreign currency
   * @param paymentTime The payment time
   * @param spotFX The spot FX rate
   * @param forwardFX The forward FX rate
   * @param curveName1 The domestic curve name
   * @param curveName2 The foreign curve name
   * @return The FX forward instrument
   */
  //TODO determine domestic and notional from dominance data
  private static ForexForward getFXForward(final Currency ccy1, final Currency ccy2, final double paymentTime, final double spotFX, final double forwardFX, final String curveName1,
      final String curveName2) {
    final PaymentFixed paymentCurrency1 = new PaymentFixed(ccy1, paymentTime, 1, curveName1);
    final PaymentFixed paymentCurrency2 = new PaymentFixed(ccy2, paymentTime, -1. / forwardFX, curveName2);
    return new ForexForward(paymentCurrency1, paymentCurrency2, spotFX);
  }

  /**
   * Gets the FX spot rate requirement.
   * @param provider The FX forward curve instrument provider
   * @param currencies The currency pair
   * @return The spot requirement
   */
  private static ValueRequirement getSpotRequirement(final FXForwardCurveInstrumentProvider provider, final UnorderedCurrencyPair currencies) {
    if (provider.useSpotRateFromGraph()) {
      return ConventionBasedFXRateFunction.getSpotRateRequirement(currencies);
    }
    return new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, provider.getSpotInstrument());
  }

}
