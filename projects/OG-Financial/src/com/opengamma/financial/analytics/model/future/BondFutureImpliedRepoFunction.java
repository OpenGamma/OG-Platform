/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.bond.BondConvention;
import com.opengamma.financial.bond.BondDefinition;
import com.opengamma.financial.bond.BondForwardDefinition;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.interestrate.bond.BondFutureCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureImpliedRepoRateCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondFutureImpliedRepoFunction extends NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureImpliedRepoFunction.class);
  private static final BondFutureCalculator IMPLIED_REPO_CALCULATOR = BondFutureImpliedRepoRateCalculator.getInstance();

  @Override
  public String getShortName() {
    return "BondFutureImpliedRepoFunction";
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final BondFutureSecurity security = (BondFutureSecurity) position.getSecurity();
    final ZonedDateTime firstDeliveryDate = security.getExpiry().getExpiry();
    final ZonedDateTime lastDeliveryDate = firstDeliveryDate.plusMonths(1);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    //final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"); //TODO this needs to be pulled from a convention
    //final double deliveryDate = dayCount.getDayCountFraction(now, firstDeliveryDate);
    ValueRequirement priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + priceRequirement);
    }
    final double futurePrice = (Double) priceObject;
    s_logger.error("Future: {}", security.getName());
    final List<BondFutureDeliverable> bonds = security.getBasket();
    final Set<ComputedValue> values = new HashSet<ComputedValue>();
    final int n = bonds.size();
    final BondForward[] deliverables = new BondForward[n];
    final BondSecurity[] securities = new BondSecurity[n];
    final double[] cleanPrices = new double[n];
    final double[] repoRates = new double[n];
    final double[] conversionFactors = new double[n];
    final int i = 0;
    final Currency currency = security.getCurrency();
    final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, security.getCurrency());
    final Identifier conventionId = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TREASURY_COUPON_DATE_CONVENTION");
    final ConventionBundle convention = conventionSource.getConventionBundle(conventionId);
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final boolean isEOMConvention = convention.isEOMConvention();
    final int settlementDays = convention.getSettlementDays();
    for (final BondFutureDeliverable del : bonds) {
      final IdentifierBundle id = del.getIdentifiers();
      final Security sec = executionContext.getSecuritySource().getSecurity(id);
      if (sec instanceof BondSecurity) {
        final BondSecurity bondSec = (BondSecurity) sec;
        final DayCount daycount = currency.getISOCode().equals("USD") ? DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA") : bondSec.getDayCountConvention();
        final BondConvention bondConvention = new BondConvention(0, daycount, businessDayConvention, calendar, isEOMConvention, (currency.getISOCode() + "_TREASURY_BOND"),
            convention.getExDividendDays(), SimpleYieldConvention.US_TREASURY_EQUIVALANT);
        final BondDefinition bondDefinition = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource).getBond(bondSec, true);
        final Bond bond = bondDefinition.toDerivative(now.toLocalDate(), "dummy");
        priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, bondSec.getUniqueIdentifier());
        priceObject = inputs.getValue(priceRequirement);
        if (priceObject == null) {
          s_logger.warn("Cannot get clean price for {} in basket of {}.", bondDefinition, security);
          continue;
          //throw new NullPointerException("Could not get " + priceRequirement + " for " + bondSec);
        }
        final double cleanPrice = (Double) priceObject;
        final BondForwardDefinition forwardDefinition = new BondForwardDefinition(bondDefinition, firstDeliveryDate.toLocalDate(), bondConvention); //TODO this shouldn't be done here
        final BondForward forward = forwardDefinition.toDerivative(now.toLocalDate(), "dummy");
        securities[i] = bondSec;
        deliverables[i] = forward;
        conversionFactors[i] = del.getConversionFactor();
        cleanPrices[i] = cleanPrice / 100;
        repoRates[i] = 0;
      } else {
        throw new IllegalArgumentException("Object of type " + sec.getClass() + " not a BondSecurity");
      }
    }
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(cleanPrices, repoRates);
    final double[] impliedRepos = IMPLIED_REPO_CALCULATOR.calculate(new BondFuture(deliverables, conversionFactors), basketData, futurePrice / 100);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, position), getUniqueIdentifier());
    values.add(new ComputedValue(specification, impliedRepos));
    return values;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof BondFutureSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final HashSet<ValueRequirement> requirements = Sets.newHashSet(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getPosition().getSecurity()
          .getUniqueIdentifier()));
      final SecuritySource secSource = context.getSecuritySource();
      final Position position = target.getPosition();
      final BondFutureSecurity security = (BondFutureSecurity) position.getSecurity();
      final List<BondFutureDeliverable> bonds = security.getBasket();
      for (final BondFutureDeliverable del : bonds) {
        final IdentifierBundle ids = del.getIdentifiers();
        final Security deliverableBond = secSource.getSecurity(ids);
        if (deliverableBond != null) {
          requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, deliverableBond.getUniqueIdentifier()));
          ;
        } else {
          s_logger.warn("bond {} in deliverable basket of {} has empty IdentifierBundle, skipping", del, security);
          continue;
        }

      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, target.getPosition()), getUniqueIdentifier()));

    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
