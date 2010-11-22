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
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
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
import com.opengamma.financial.interestrate.bond.definition.Bond;
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
   
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"); //TODO this needs to be pulled from a convention
      
    double deliveryDate = dayCount.getDayCountFraction(now, firstDeliveryDate);
    ValueRequirement priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + priceRequirement);
    }
    final double futurePrice = (Double) priceObject;
      
    s_logger.error("Future: {}", security.getName());
        
    List<BondFutureDeliverable> bonds = security.getBasket();
    Set<ComputedValue> values = new HashSet<ComputedValue>(); 
    for (BondFutureDeliverable del : bonds) {
      IdentifierBundle id = del.getIdentifiers();
      Security sec = executionContext.getSecuritySource().getSecurity(id);
      if (sec instanceof BondSecurity) {
        BondSecurity bondSec = (BondSecurity) sec;
        Bond bond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", now);
        priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, bondSec.getUniqueIdentifier());
        priceObject = inputs.getValue(priceRequirement);
        if (priceObject == null) {
          s_logger.warn("Cannot get clean price for {} in basket of {}.", bond, security);
          continue;
          //throw new NullPointerException("Could not get " + priceRequirement + " for " + bondSec);
        }
        final double cleanPrice = (Double) priceObject;
       
        
        Bond fwdBond = new BondSecurityToBondConverter(holidaySource, conventionSource).getBond(bondSec, "dummy", firstDeliveryDate);
        double fwdAI = fwdBond.getAccruedInterest();
        
        double irr = BondFutureCalculator.impliedRepoRate(bond, deliveryDate, cleanPrice/100.0, futurePrice/100.0, del.getConversionFactor(), fwdAI);
        
        s_logger.error("{} IRR: {}", sec.getName(), irr);
        
        ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.IMPLIED_REPO, position), getUniqueIdentifier());
        values.add(new ComputedValue(specification, irr*100.0));
        
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
      HashSet<ValueRequirement> requirements = Sets.newHashSet(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, 
                                                                                    ComputationTargetType.SECURITY, 
                                                                                    target.getPosition().getSecurity().getUniqueIdentifier()));
      final SecuritySource secSource = context.getSecuritySource();
      final Position position = target.getPosition();
      final BondFutureSecurity security = (BondFutureSecurity) position.getSecurity();
      List<BondFutureDeliverable> bonds = security.getBasket();
      for (BondFutureDeliverable del : bonds) {
        IdentifierBundle ids = del.getIdentifiers();
        Security deliverableBond = secSource.getSecurity(ids);
        if (deliverableBond != null) {
          requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, deliverableBond.getUniqueIdentifier()));;
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
