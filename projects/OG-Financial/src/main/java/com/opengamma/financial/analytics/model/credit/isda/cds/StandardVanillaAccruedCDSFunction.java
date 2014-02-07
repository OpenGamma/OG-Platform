/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
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
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverter;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToRecoveryRateVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class StandardVanillaAccruedCDSFunction extends AbstractFunction.NonCompiledInvoker {

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(executionContext);
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final CreditDefaultSwapSecurity security = (CreditDefaultSwapSecurity) target.getSecurity();
    final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, FinancialSecurityUtils.getCurrency(
        security));
    final CdsRecoveryRateIdentifier recoveryRateIdentifier = security.accept(new CreditSecurityToRecoveryRateVisitor(securitySource));
    Object recoveryRateObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateIdentifier.getExternalId()));
    if (recoveryRateObject == null) {
      throw new OpenGammaRuntimeException("Could not get recovery rate");
      //s_logger.warn("Could not get recovery rate, defaulting to 0.4: " + recoveryRateIdentifier);
      //recoveryRateObject = 0.4;
    }
    final double recoveryRate = (Double) recoveryRateObject;
    final CreditDefaultSwapSecurityConverter converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource, legalEntitySource, recoveryRate, valuationTime);
    LegacyVanillaCreditDefaultSwapDefinition definition = (LegacyVanillaCreditDefaultSwapDefinition) security.accept(converter);
    //definition = definition.withEffectiveDate(FOLLOWING.adjustDate(calendar, valuationTime.withHour(0).withMinute(0).withSecond(0).withNano(0)));
    //definition = getStartDate(definition, security, valuationTime);
    //definition = definition.withRecoveryRate(recoveryRate);
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

  public static LegacyVanillaCreditDefaultSwapDefinition getStartDate(final LegacyVanillaCreditDefaultSwapDefinition definition, AbstractCreditDefaultSwapSecurity security, ZonedDateTime now) {
    if (security instanceof LegacyVanillaCDSSecurity) {
      return definition.withStartDate(now);
    } else if (security instanceof StandardVanillaCDSSecurity) {
      LocalDate start = IMMDateGenerator.getPreviousIMMDate(now.toLocalDate());
      return definition.withStartDate(ZonedDateTime.of(start, LocalTime.of(0, 0, 0, 0), ZoneId.systemDefault()));
    } else if (security instanceof CreditDefaultSwapIndexSecurity) {
      LocalDate start = IMMDateGenerator.getPreviousIMMDate(now.toLocalDate());
      return definition.withStartDate(ZonedDateTime.of(start, LocalTime.of(0, 0, 0, 0), ZoneId.systemDefault()));
    }
    throw new OpenGammaRuntimeException("Unexpected cds type: " + security);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY.or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    ValueSpecification daysSpec = new ValueSpecification(ValueRequirementNames.ACCRUED_DAYS, target.toSpecification(), createValueProperties().get());
    ValueSpecification interestSpec = new ValueSpecification(ValueRequirementNames.ACCRUED_PREMIUM, target.toSpecification(),
                                                             createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).get());
    return Sets.newHashSet(daysSpec, interestSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(1);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CdsRecoveryRateIdentifier recoveryRateIdentifier = security.accept(new CreditSecurityToRecoveryRateVisitor(securitySource));
    final ValueRequirement recoveryRateRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateIdentifier.getExternalId());
    requirements.add(recoveryRateRequirement);
    return requirements;
  }

}
