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

import org.hamcrest.core.IsInstanceOf;

import com.google.common.collect.Sets;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.BondFutureCalculator;
import com.opengamma.financial.interestrate.bond.BondPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondZSpreadCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BondFutureImpliedRepoFunction extends NonCompiledInvoker {

  @Override
  public String getShortName() {
    return "BondFutureImpliedRepoFunction";
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final BondFutureSecurity security = (BondFutureSecurity) position.getSecurity();
    ZonedDateTime firstDeliveryDate = security.getExpiry().getExpiry();
    ZonedDateTime lastDeliveryDate = firstDeliveryDate.plusMonths(1);
    
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
   
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO this needs to be pulled from a convention
      
    double deliveryDate = dayCount.getDayCountFraction(now, firstDeliveryDate);
    ValueRequirement priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + priceRequirement);
    }
    final double futurePrice = (Double) priceObject;
       
        
    List<BondFutureDeliverable> bonds = security.getBasket();
    Set<ComputedValue> values = new HashSet<ComputedValue>(); 
    for (BondFutureDeliverable del : bonds) {
      IdentifierBundle id = del.getIdentifiers();
      Security sec = executionContext.getSecuritySource().getSecurity(id);
      if (sec instanceof BondSecurity) {
        BondSecurity bondSec = (BondSecurity) sec;
        Bond bond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", now);
        priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
        priceObject = inputs.getValue(priceRequirement);
        if (priceObject == null) {
          throw new NullPointerException("Could not get " + priceRequirement);
        }
        final double cleanPrice = (Double) priceObject;
        //bondSec.getName()
        
        Bond fwdBond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", firstDeliveryDate);
        double fwdAI = fwdBond.getAccruedInterestFraction();
        
        double irr = BondFutureCalculator.impliedRepoRate(bond, deliveryDate, cleanPrice, futurePrice, del.getConversionFactor(), fwdAI);
        
        ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, position), getUniqueIdentifier());
        values.add(new ComputedValue(specification, irr));
        
      } else {
        throw new IllegalArgumentException("Object of type " + sec.getClass() + " not a BondSecurity");
      }
    }
    return values;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof BondFutureSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
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
