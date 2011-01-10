/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
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
import com.opengamma.financial.analytics.bond.BondSecurityToBondConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.bond.BondFutureCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureImpliedRepoRateCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
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
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"); //TODO this needs to be pulled from a convention
    final double deliveryDate = dayCount.getDayCountFraction(now, firstDeliveryDate);
    ValueRequirement priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
    Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + priceRequirement);
    }
    final double futurePrice = (Double) priceObject;
    s_logger.error("Future: {}", security.getName());
    final List<BondFutureDeliverable> bonds = security.getBasket();
    final Set<ComputedValue> values = new HashSet<ComputedValue>();
    final List<Double> deliveryDates = new ArrayList<Double>();
    final List<Double> cleanPrices = new ArrayList<Double>();
    final List<Double> accruedInterest = new ArrayList<Double>();
    final List<Double> repoRates = new ArrayList<Double>();
    final int n = bonds.size();
    final Bond[] deliverables = new Bond[n];
    final BondSecurity[] securities = new BondSecurity[n];
    final double[] conversionFactors = new double[n];
    int i = 0;
    for (final BondFutureDeliverable del : bonds) {
      final IdentifierBundle id = del.getIdentifiers();
      final Security sec = executionContext.getSecuritySource().getSecurity(id);
      if (sec instanceof BondSecurity) {
        final BondSecurity bondSec = (BondSecurity) sec;
        final Bond bond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", now);
        priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, bondSec.getUniqueId());
        priceObject = inputs.getValue(priceRequirement);
        if (priceObject == null) {
          s_logger.warn("Cannot get clean price for {} in basket of {}.", bond, security);
          continue;
          //throw new NullPointerException("Could not get " + priceRequirement + " for " + bondSec);
        }
        final double cleanPrice = (Double) priceObject;
        final Bond fwdBond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", firstDeliveryDate);
        final double fwdAI = fwdBond.getAccruedInterest();
        securities[i] = bondSec;
        deliverables[i] = bond;
        conversionFactors[i++] = del.getConversionFactor();
        deliveryDates.add(deliveryDate);
        cleanPrices.add(cleanPrice);
        accruedInterest.add(fwdAI);
        repoRates.add(0.);

        //final double irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, deliveryDates, cleanPrices, accruedInterest, repoRates, futurePrice)
        //(bond, deliveryDate, cleanPrice/100.0, futurePrice/100.0, del.getConversionFactor(), fwdAI);
      } else {
        throw new IllegalArgumentException("Object of type " + sec.getClass() + " not a BondSecurity");
      }
    }
    //    s_logger.error("{} IRR: {}", sec.getName(), irr);
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(deliveryDates, cleanPrices, accruedInterest, repoRates);
    final double[] impliedRepos = IMPLIED_REPO_CALCULATOR.calculate(new BondFuture(deliverables, conversionFactors), basketData, futurePrice);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, position), getUniqueId());
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
      final HashSet<ValueRequirement> requirements = Sets.newHashSet(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY,
          target.getPosition().getSecurity().getUniqueId()));
      final SecuritySource secSource = context.getSecuritySource();
      final Position position = target.getPosition();
      final BondFutureSecurity security = (BondFutureSecurity) position.getSecurity();
      final List<BondFutureDeliverable> bonds = security.getBasket();
      for (final BondFutureDeliverable del : bonds) {
        final IdentifierBundle ids = del.getIdentifiers();
        final Security deliverableBond = secSource.getSecurity(ids);
        if (deliverableBond != null) {
          requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, deliverableBond.getUniqueId()));
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
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, target.getPosition()), getUniqueId()));

    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
