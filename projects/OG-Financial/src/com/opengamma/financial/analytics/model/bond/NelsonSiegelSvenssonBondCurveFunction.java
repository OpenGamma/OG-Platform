/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.LastDateCalculator;
import com.opengamma.financial.interestrate.NelsonSiegelSvennsonBondCurveModel;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class NelsonSiegelSvenssonBondCurveFunction extends AbstractFunction {
  /** Name of the property type*/
  public static final String PROPERTY_CURVE_CALCULATION_TYPE = "Nelson_Siegel_Svennson_Bond_Curve";
  /** Name of the property*/
  public static final String PROPERTY_PREFIX = "Nelson-Siegel-Svennson";
  private static final Logger s_logger = LoggerFactory.getLogger(NelsonSiegelSvenssonBondCurveFunction.class);
  private static final NonLinearLeastSquare MINIMISER = new NonLinearLeastSquare();
  private static final LastDateCalculator LAST_DATE = LastDateCalculator.getInstance();
  private static final NelsonSiegelSvennsonBondCurveModel MODEL = new NelsonSiegelSvennsonBondCurveModel();
  //private static final ParameterLimitsTransform[] TRANSFORMS = new ParameterLimitsTransform[] {new SingleRangeLimitTransform(0, LimitType.GREATER_THAN), new NullTransform(), new NullTransform(),
  //  new NullTransform(), new NullTransform(), new NullTransform()};
  private static final BitSet FIXED_PARAMETERS = new BitSet(6);

  static {
    FIXED_PARAMETERS.set(0);
  }

  private final Currency _currency;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;

  public NelsonSiegelSvenssonBondCurveFunction(final String currencyName) {
    Validate.notNull(currencyName, "currency name");
    _currency = Currency.of(currencyName);
  }

  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _result = new ValueSpecification(ValueRequirementNames.NSS_BOND_CURVE, new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, _currency.getUniqueId()), createValueProperties().with(
        PROPERTY_CURVE_CALCULATION_TYPE, PROPERTY_PREFIX + "_" + _currency.getCode()).get());
    _results = Sets.newHashSet(_result);
  }

  @Override
  public String getShortName() {
    return "NelsonSiegelSvennsonBondCurveFunction";
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    return new AbstractInvokingCompiledFunction() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
        final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final BondSecurityConverter converter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
        final FinancialSecuritySource securitySource = executionContext.getSecuritySource(FinancialSecuritySource.class);
        final Collection<Security> allBonds = new ArrayList<Security>(securitySource.getBondsWithIssuerName("US TREASURY N/B"));
        final Iterator<Security> iter = allBonds.iterator();
        while (iter.hasNext()) {
          final Security sec = iter.next();
          if (sec instanceof BondSecurity) {
            final BondSecurity bond = (BondSecurity) sec;
            if (bond.getLastTradeDate().getExpiry().isBefore(now)) {
              iter.remove();
            }
            s_logger.info(bond.getLastTradeDate().toString());
          } else {
            throw new OpenGammaRuntimeException("non-bond security " + sec + " returned by getAllBondsOfIssuerType()");
          }
        }
        final int n = allBonds.size();
        final double[] t = new double[n];
        final double[] ytm = new double[n];
        int i = 0;
        for (final Security security : allBonds) {
          final GovernmentBondSecurity bondSec = (GovernmentBondSecurity) security;
          final Object ytmObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YTM, ComputationTargetType.SECURITY, security.getUniqueId()));
          if (ytmObject == null) {
            s_logger.warn("Could not get YTM for " + security.getUniqueId());
            continue;
          }
          if (!(ytmObject instanceof Double)) {
            throw new IllegalArgumentException("YTM should be a double");
          }
          final InstrumentDefinition<?> definition = converter.visitGovernmentBondSecurity(bondSec);
          final String bondStringName = PROPERTY_PREFIX + "_" + _currency.getCode();
          final InstrumentDerivative bond = definition.toDerivative(now, bondStringName);
          t[i] = LAST_DATE.visit(bond);
          ytm[i++] = ((Double) ytmObject / 100);
        }
        final DoubleMatrix1D initialValues = new DoubleMatrix1D(new double[] {1, 2, 3, 4, 2, 3 });
        final ParameterizedFunction<Double, DoubleMatrix1D, Double> parameterizedFunction = MODEL.getParameterizedFunction();
        final LeastSquareResults result = MINIMISER.solve(new DoubleMatrix1D(t), new DoubleMatrix1D(ytm), parameterizedFunction, initialValues);
        final DoubleMatrix1D parameters = result.getFitParameters();
        final FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(parameterizedFunction.asFunctionOfArguments(parameters));
        final YieldCurve yieldCurve = new YieldCurve(curve);
        return Sets.newHashSet(new ComputedValue(_result, yieldCurve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (canApplyTo(context, target)) {
          final FinancialSecuritySource securitySource = context.getSecuritySource(FinancialSecuritySource.class);
          final Collection<Security> allBonds = new ArrayList<Security>(securitySource.getBondsWithIssuerName("US TREASURY N/B"));
          final Iterator<Security> iter = allBonds.iterator();
          while (iter.hasNext()) {
            final Security sec = iter.next();
            if (sec instanceof BondSecurity) {
              final BondSecurity bond = (BondSecurity) sec;
              if (bond.getLastTradeDate().getExpiry().toInstant().isBefore(atInstant.toInstant())) {
                iter.remove();
              }
              s_logger.info(bond.getLastTradeDate().toString());
            } else {
              throw new OpenGammaRuntimeException("non-bond security " + sec + " returned by getAllBondsOfIssuerType()");
            }
          }
          final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
          for (final Security sec : allBonds) {
            if (sec instanceof BondSecurity) {
              final BondSecurity bond = (BondSecurity) sec;
              if (!bond.getCurrency().equals(_currency)) {
                throw new OpenGammaRuntimeException("Currency for bond " + bond.getUniqueId() + " (" + bond.getCurrency() + ") did not match that required (" + _currency + ")");
              }
              requirements.add(new ValueRequirement(ValueRequirementNames.YTM, ComputationTargetType.SECURITY, bond.getUniqueId()));
            } else {
              throw new OpenGammaRuntimeException("non-bond security " + sec + " returned with bonds of issuer type");
            }
          }
          return requirements;
        }
        return Sets.newHashSet();
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return _results;
      }

    };
  }
}
