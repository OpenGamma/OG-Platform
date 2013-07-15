/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDAApproxCDSPricingMethod;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.ISDACDSSecurityConverter;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Price CDS contracts according to the ISDA model using a hazard rate term structure.
 *
 * This is provided as a placeholder only and more work is required before the ISDA approximate
 * pricer can be used with a hazard rate term structure. In particular a hazard curve function
 * is required to fulfill the hazard curve requirement, which will require adding an extra method
 * in the pricing method class. For an example of how to do this see the version of
 * {@link ISDAApproxCDSPricingMethod#calculateUpfrontCharge} which takes a flat spread as input
 * for the hazard rate solver.
 *
 * For a complete ISDA pricing implementation use {@link ISDAApproxCDSPriceFlatSpreadFunction}.
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPricingMethod
 */
public class ISDAApproxCDSPriceHazardCurveFunction extends ISDAApproxCDSPriceFunction {

  private static final ISDAApproxCDSPricingMethod ISDA_APPROX_METHOD = new ISDAApproxCDSPricingMethod();

  @Override
  protected String getHazardRateStructure() {
    return ISDAFunctionConstants.ISDA_HAZARD_RATE_TERM;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {

      final CDSSecurity cds = (CDSSecurity) target.getSecurity();

      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();

      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
          ComputationTargetSpecification.of(cds.getCurrency()),
        ValueProperties
          .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get()));

      requirements.add(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
          ComputationTargetSpecification.of(cds.getCurrency()),
        ValueProperties
          .with(ValuePropertyNames.CURVE, "HAZARD_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
          .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get()));

      return requirements;
    }
    return null;
  }

  @Override
  public DoublesPair executeImpl(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // Set up converter (could this be compiled?)
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ISDACDSSecurityConverter converter = new ISDACDSSecurityConverter(holidaySource);

    // Security being priced
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final ISDACDSDefinition cdsDefinition = (ISDACDSDefinition) cds.accept(converter);

    // Time point to price for
    // TODO: Supply an option for the user to specify non-standard step-in and settlement dates
    final ZonedDateTime pricingDate = ZonedDateTime.now(executionContext.getValuationClock());
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = findSettlementDate(pricingDate, cdsDefinition.getConvention());

    // Discount curve
    final ISDACurve discountCurve = (ISDACurve) inputs.getValue(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(cds.getCurrency()),
      ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get()));

    // Hazard rate curve
    final ISDACurve hazardRateCurve = (ISDACurve) inputs.getValue(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(cds.getCurrency()),
      ValueProperties
        .with(ValuePropertyNames.CURVE, "HAZARD_" + cds.getUnderlyingIssuer() + "_" + cds.getUnderlyingSeniority() + "_" + cds.getRestructuringClause())
        .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get()));

    // Convert security in to format suitable for pricing
    final ISDACDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, stepinDate, settlementDate, discountCurve.getName(), hazardRateCurve.getName());

    // Go price!
    final double dirtyPrice = ISDA_APPROX_METHOD.calculateUpfrontCharge(cdsDerivative, discountCurve, hazardRateCurve, false);
    final double cleanPrice = dirtyPrice - cdsDerivative.getAccruedInterest();

    return DoublesPair.of(cleanPrice, dirtyPrice);
  }



}
