/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.interestrate.cds.CDSDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CDSSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * CDSPresentValueFunction currently contains initial work on CDS model only
 * 
 * @author Martin Traverse
 * @see CDSSecurity
 */
public class CDSPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof CDSSecurity) {
      return true;
    }
    return false;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {

      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      final ExternalIdBundle bundle = ExternalIdBundle.of(cds.getUnderlying());
      final BondSecurity bond = (BondSecurity) context.getSecuritySource().getSecurity(bundle);

      if (bond == null) {
        ; // TODO: handle error
      }

      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();

      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(),
        ValueProperties
          .with("Curve", "SECONDARY")
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with("CurveCalculationMethod", "ParRate")
          .get()
      ));

      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        bond.getCurrency().getUniqueId(),
        ValueProperties
          .with(ValuePropertyNames.CURVE, "SECONDARY")
          .with("FundingCurve", "SECONDARY")
          .with("ForwardCurve", "SECONDARY")
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate")
          .get()
      ));

      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      final ValueSpecification pvSpec = new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .get()
        ),
        getUniqueId()
      );
      return Collections.<ValueSpecification>singleton(pvSpec);
    }
    return null;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    // Get handles to data sources
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    
    // Time point to price for
    final ZonedDateTime pricingDate = executionContext.getValuationClock().zonedDateTime();
    
    // Security being priced
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final BondSecurity bond = (BondSecurity) securitySource.getSecurity(ExternalIdBundle.of(cds.getUnderlying()));

    // Curves
    final YieldCurve cdsCcyCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with("FundingCurve", "SECONDARY").with("ForwardCurve", "SECONDARY").with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate").get()
    ));
    
    final YieldCurve bondCcyCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, bond.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with("FundingCurve", "SECONDARY").with("ForwardCurve", "SECONDARY").with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate").get()
    ));
    
    // TODO: Get credit curve for issuer
    final double[] timePoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    final double[] riskyRates = {
      0.01482679544756000000, 0.01482679544756000000, 0.02277108240490950000, 0.02969454114090150000, 0.03548377040735590000, 0.04023357875426090000,
      0.04389623486790170000, 0.04683365687667970000, 0.04901816538208170000, 0.05099033851262700000, 0.05268070877392120000, 0.05268070877392120000
    };
    final YieldCurve creditCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, riskyRates, new LinearInterpolator1D()));
    
    // Convert security in to format suitable for pricing
    final CDSSecurityConverter converter = new CDSSecurityConverter(securitySource, holidaySource, conventionSource);
    final CDSDefinition cdsDefinition = (CDSDefinition) cds.accept(converter);
    final CDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, cdsCcyCurve.getName(), bondCcyCurve.getName(), creditCurve.getName());
    
    // Go price!
    final double result = CDSSimpleCalculator.calculate(cdsDerivative, cdsCcyCurve, bondCcyCurve, creditCurve);
    
    // Package the result
    final ComputedValue marketPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties.with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode()).get()
        ),
        getUniqueId()
      ),
      result
    );

    return Collections.<ComputedValue>singleton(marketPriceValue);
  }

}
