/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.interestrate.LastDateCalculator;
import com.opengamma.financial.interestrate.NelsonSiegelBondCurveModel;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class NelsonSiegelBondCurveFunction extends AbstractFunction {
  /** Name of the property type*/
  public static final String PROPERTY_CURVE_CALCULATION_TYPE = "Nelson_Siegel_Bond_Curve";
  /** Name of the property*/
  public static final String PROPERTY_PREFIX = "Nelson-Siegel";
  private static final Logger s_logger = LoggerFactory.getLogger(NelsonSiegelBondCurveFunction.class);
  private static final NelsonSiegelBondCurveModel MODEL = new NelsonSiegelBondCurveModel();
  private static final NonLinearLeastSquare MINIMISER = new NonLinearLeastSquare();
  private static final LastDateCalculator LAST_DATE = LastDateCalculator.getInstance();
  private final Currency _currency;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;

  public NelsonSiegelBondCurveFunction(final String currencyName) {
    Validate.notNull(currencyName, "currency name");
    _currency = Currency.getInstance(currencyName);
  }

  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _result = new ValueSpecification(ValueRequirementNames.NS_BOND_CURVE, new ComputationTargetSpecification(_currency), createValueProperties().with(PROPERTY_CURVE_CALCULATION_TYPE,
        PROPERTY_PREFIX + "_" + _currency.getISOCode()).get());
    _results = Sets.newHashSet(_result);
  }

  @Override
  public String getShortName() {
    return "NelsonSiegelBondCurveFunction";
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    return new AbstractInvokingCompiledFunction() {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
        final Clock snapshotClock = executionContext.getSnapshotClock();
        final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
        final BondSecurityToBondDefinitionConverter converter = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource);
        final Collection<BondSecurity> allBonds = null; //TODO
        final int n = allBonds.size();
        final double[] t = new double[n];
        final double[] ytm = new double[n];
        int i = 0;
        for (final BondSecurity security : allBonds) {
          final Object ytmObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YTM, ComputationTargetType.SECURITY, security.getUniqueId()));
          if (ytmObject == null) {
            s_logger.warn("Could not get YTM for " + security.getUniqueId());
            continue;
          }
          if (!(ytmObject instanceof Double)) {
            throw new IllegalArgumentException("YTM should be a double");
          }
          final BondDefinition definition = converter.getBond(security, true);
          final Bond bond = definition.toDerivative(now, PROPERTY_PREFIX + "_" + _currency.getISOCode());
          t[i] = LAST_DATE.visit(bond);
          ytm[i++] = (Double) ytmObject;
        }
        final ParameterizedFunction<Double, DoubleMatrix1D, Double> parameterizedFunction = MODEL.getParameterizedFunction();
        final LeastSquareResults result = MINIMISER.solve(new DoubleMatrix1D(t), new DoubleMatrix1D(ytm), parameterizedFunction, new DoubleMatrix1D(new double[] {1, 2, 3, 4}));
        final DoubleMatrix1D parameters = result.getParameters();
        final Function1D<Double, Double> function = parameterizedFunction.asFunctionOfArguments(parameters);
        final FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(function);
        final YieldCurve yieldCurve = new YieldCurve(curve);
        return Sets.newHashSet(new ComputedValue(_result, yieldCurve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (canApplyTo(context, target)) {
          final Collection<BondSecurity> allBonds = null; // TODO use _currency
          final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
          for (final BondSecurity bond : allBonds) {
            if (!bond.getCurrency().equals(_currency)) {
              throw new IllegalArgumentException("Currency for bond " + bond.getUniqueId() + " (" + bond.getCurrency() + ") did not match that required (" + _currency + ")");
            }
            requirements.add(new ValueRequirement(ValueRequirementNames.YTM, ComputationTargetType.SECURITY, bond.getUniqueId()));
          }
          return requirements;
        }
        return Sets.newHashSet();
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

    };
  }
}
