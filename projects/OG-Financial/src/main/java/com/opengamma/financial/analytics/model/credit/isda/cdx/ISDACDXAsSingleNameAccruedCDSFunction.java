/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultIndexSwapSecurityToProxyConverter;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDACDXAsSingleNameAccruedCDSFunction extends AbstractFunction.NonCompiledInvoker {

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(executionContext);
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final CreditDefaultIndexSwapSecurityToProxyConverter converter = new CreditDefaultIndexSwapSecurityToProxyConverter(holidaySource, regionSource, legalEntitySource, securitySource, valuationTime);
    final CreditDefaultSwapIndexSecurity security = (CreditDefaultSwapIndexSecurity) target.getSecurity();
    final CreditDefaultSwapIndexDefinitionSecurity underlyingDefinition = (CreditDefaultSwapIndexDefinitionSecurity) securitySource.getSingle(ExternalIdBundle.of(security.getReferenceEntity()));
    if (underlyingDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get underlying index definition");
    }
    final double recoveryRate = underlyingDefinition.getRecoveryRate();
    final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, FinancialSecurityUtils.getCurrency(security));
    LegacyVanillaCreditDefaultSwapDefinition definition = (LegacyVanillaCreditDefaultSwapDefinition) security.accept(converter);
    definition = definition.withEffectiveDate(FOLLOWING.adjustDate(calendar, valuationTime.withHour(0).withMinute(0).withSecond(
        0).withNano(0)));
    //definition = StandardVanillaAccruedCDSFunction.getStartDate(definition, security, valuationTime);
    definition = definition.withRecoveryRate(recoveryRate);
    final CDSAnalyticFactory analyticFactory = new CDSAnalyticFactory(recoveryRate, definition.getCouponFrequency().getPeriod())
        .with(definition.getBusinessDayAdjustmentConvention())
        .with(definition.getCalendar()).with(definition.getStubType())
        .withAccrualDCC(definition.getDayCountFractionConvention());
    final CDSAnalytic pricingCDS = analyticFactory.makeCDS(definition.getEffectiveDate().toLocalDate(), definition.getStartDate().toLocalDate(), definition.getMaturityDate().toLocalDate());

    int buySellPremiumFactor = security.isBuy() ? -1 : 1;
    final double coupon = definition.getParSpread() * 1e-4;

    Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desired : desiredValues) {
      switch (desired.getValueName()) {
        case ValueRequirementNames.ACCRUED_DAYS:
          final int accruedDays = pricingCDS.getAccuredDays();
          final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.ACCRUED_DAYS, target.toSpecification(), desired.getConstraints().copy().get());
          results.add(new ComputedValue(spec, accruedDays));
          break;
        case ValueRequirementNames.ACCRUED_PREMIUM:
          final double accruedInterest = pricingCDS.getAccruedPremium(coupon) * definition.getNotional() * buySellPremiumFactor;
          final ValueSpecification spec2 = new ValueSpecification(ValueRequirementNames.ACCRUED_PREMIUM, target.toSpecification(), desired.getConstraints().copy().get());
          results.add(new ComputedValue(spec2, accruedInterest));
          break;
        default:
          throw new OpenGammaRuntimeException("Unexpected ValueRequirement: " + desired.getValueName());
      }
    }
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_INDEX_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    ValueSpecification daysSpec = new ValueSpecification(ValueRequirementNames.ACCRUED_DAYS, target.toSpecification(), createValueProperties().get());
    ValueSpecification interestSpec = new ValueSpecification(ValueRequirementNames.ACCRUED_PREMIUM, target.toSpecification(),
                                                             createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).get());
    return Sets.newHashSet(daysSpec, interestSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  //@Override
  //protected ValueProperties.Builder getCommonResultProperties() {
  //  return createValueProperties()
  //      .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE);
  //}
  //
  //@Override
  //protected boolean labelResultWithCurrency() {
  //  return true;
  //}
}
