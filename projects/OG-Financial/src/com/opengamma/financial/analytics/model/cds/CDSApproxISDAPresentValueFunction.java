/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.CDSApproxISDAMethod;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
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
import com.opengamma.financial.analytics.conversion.ISDACDSSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * CDSPresentValueFunction currently contains initial work on CDS model only
 * 
 * @author Martin Traverse
 * @see CDSSecurity
 */
public class CDSApproxISDAPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  private static final CDSApproxISDAMethod ISDA_APPROX_METHOD = new CDSApproxISDAMethod();
  private static final String ISDA_METHOD_NAME = "ISDA";

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    
    // ISDA can price any CDS
    if (target.getSecurity() instanceof CDSSecurity) {
      return true;
    }
    
    return false;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {

      final CDSSecurity cds = (CDSSecurity) target.getSecurity();

      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();

//      requirements.add(new ValueRequirement(
//        ValueRequirementNames.ISDA_CURVE,
//        ComputationTargetType.PRIMITIVE,
//        cds.getCurrency().getUniqueId(),
//        ValueProperties
//          .with("Curve", "SECONDARY")
//          .with("FundingCurve", "SECONDARY")
//          .with("ForwardCurve", "SECONDARY")
//          .with("CurveCalculationMethod", "ParRate")
//          .get()
//      ));
      
//      requirements.add(new ValueRequirement(
//        ValueRequirementNames.YIELD_CURVE,
//        ComputationTargetType.PRIMITIVE,
//        cds.getCurrency().getUniqueId(),
//        ValueProperties
//          .with(ValuePropertyNames.CURVE, "CDS_" + bond.getIssuerName())
//          .get()
//      ));

      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final CDSSecurity cds = (CDSSecurity) target.getSecurity();
      
      final ValueSpecification cleanPriceSpec = new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.CLEAN_PRICE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      );
      
      final ValueSpecification dirtyPriceSpec = new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.DIRTY_PRICE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      );
      
      final ValueSpecification presentValueSpec = new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      );
      
      Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      results.add(cleanPriceSpec);
      results.add(dirtyPriceSpec);
      results.add(presentValueSpec);
      
      return results;
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

    // Curves
//    final YieldCurve discountCurve = (YieldCurve) inputs.getValue(new ValueRequirement(
//      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
//      ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with("FundingCurve", "SECONDARY").with("ForwardCurve", "SECONDARY").with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ParRate").get()
//    ));
    
    final YieldCurve spreadCurve = null; /*(YieldCurve) inputs.getValue(new ValueRequirement(
      ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, cds.getCurrency().getUniqueId(),
      ValueProperties.with(ValuePropertyNames.CURVE, "CDS_" + bond.getIssuerName()).get()
    ));*/
    
    // Convert security in to format suitable for pricing
    final ISDACDSSecurityConverter converter = new ISDACDSSecurityConverter(holidaySource);
    final ISDACDSDefinition cdsDefinition = (ISDACDSDefinition) cds.accept(converter);
    final ISDACDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, "IR_CURVE");  // Sets step-in = T+1 calendar days, settlement = T+3 working days
    
    String name = "IR_CURVE";
    double offset = 0.0;
    double[] yData = {0.0};
    double[] xData = {0};
    ISDACurve isdaDiscountCurve = new ISDACurve(name, xData, yData, offset);
    
    

    
    double flatSpread = 100.0;
    
    
    double dirtyPrice = ISDA_APPROX_METHOD.calculateUpfrontCharge(cdsDerivative, isdaDiscountCurve, flatSpread, false);
    
    final ComputedValue cleanPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.CLEAN_PRICE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      ),
      dirtyPrice - cdsDerivative.getAccruedInterest()
    );
    
    final ComputedValue dirtyPriceValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.DIRTY_PRICE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      ),
      dirtyPrice
    );
    
    final ComputedValue presentValue = new ComputedValue(
      new ValueSpecification(
        new ValueRequirement(
          ValueRequirementNames.PRESENT_VALUE,
          ComputationTargetType.SECURITY,
          cds.getUniqueId(),
          ValueProperties
            .with(ValuePropertyNames.CURRENCY, cds.getCurrency().getCode())
            .with(ValuePropertyNames.CALCULATION_METHOD, ISDA_METHOD_NAME)
            .get()
        ),
        getUniqueId()
      ),
      cleanPriceValue.getValue()
    );
    
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.add(cleanPriceValue);
    results.add(dirtyPriceValue);
    results.add(presentValue);
    
    return results;
  }

}
