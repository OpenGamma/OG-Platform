/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.cds.CDSApproxISDAMethod;
import com.opengamma.analytics.financial.credit.cds.CDSDerivative;
import com.opengamma.analytics.financial.credit.cds.CDSSimpleMethod;
import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.util.money.CurrencyAmount;

/**
 * CDSPresentValueFunction currently contains initial work on CDS model only
 * 
 * @author Martin Traverse
 * @see CDSSecurity
 */
public class CDSApproxISDAPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    
    // Only CDS securities associated with a particular underlying bond can be priced by this method
    if (target.getSecurity() instanceof CDSSecurity) {
      CDSSecurity cds = (CDSSecurity) target.getSecurity();
      
      if (cds.getUnderlying() != null) {
        return true;
      }
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
        throw new OpenGammaRuntimeException("Failed to retrieve underlying security");
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
      
      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        cds.getCurrency().getUniqueId(),
        ValueProperties
          .with(ValuePropertyNames.CURVE, "CDS_" + bond.getIssuerName())
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
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    
    // Time point to price for
    final ZonedDateTime pricingDate = executionContext.getValuationClock().zonedDateTime();
    
    // Security being priced
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final BondSecurity bond = (BondSecurity) securitySource.getSecurity(ExternalIdBundle.of(cds.getUnderlying()));
    
    if (bond == null) {
      throw new OpenGammaRuntimeException("Failed to retrieve underlying security");
    }

    // Curves
    final YieldCurve cdsCcyCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with("FundingCurve", "SECONDARY").with("ForwardCurve", "SECONDARY").with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate").get()
    ));
    
    final YieldCurve bondCcyCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, bond.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with("FundingCurve", "SECONDARY").with("ForwardCurve", "SECONDARY").with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate").get()
    ));
    
    final YieldCurve spreadCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "CDS_" + bond.getIssuerName()).get()
    ));
    
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve(cdsCcyCurve.getName(), cdsCcyCurve);
    curveBundle.setCurve(bondCcyCurve.getName(), bondCcyCurve);
    curveBundle.setCurve(spreadCurve.getName(), spreadCurve);
    
    // Convert security in to format suitable for pricing
    final CDSSecurityConverter converter = new CDSSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
    final CDSDefinition cdsDefinition = (CDSDefinition) cds.accept(converter);
    final CDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, cdsCcyCurve.getName(), spreadCurve.getName(), bondCcyCurve.getName());
    
    // Go price!
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(cdsDerivative, curveBundle);
    
    // Package the result
    final ComputedValue marketPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, cds.getUniqueId(),
          ValueProperties.with(ValuePropertyNames.CURRENCY, result.getCurrency().getCode()).get()
        ),
        getUniqueId()
      ),
      result.getAmount()
    );

    return Collections.<ComputedValue>singleton(marketPriceValue);
  }

}
