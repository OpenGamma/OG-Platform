/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondConverter;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public abstract class BondFunction extends NonCompiledInvoker {

  private final String _bondCurveName = "BondCurve";

  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Position position = target.getPosition();
    BondSecurity security = (BondSecurity) position.getSecurity();

    final ValueRequirement cleanPriceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    final Object cleanPriceObject = inputs.getValue(cleanPriceRequirement);
    if (cleanPriceObject == null) {
      throw new NullPointerException("Could not get " + cleanPriceRequirement);
    }
    
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();

    final double cleanPrice = (Double) cleanPriceObject;
    final Bond bond = new BondSecurityToBondConverter(holidaySource).getBond(security, _bondCurveName, now);
   
    return getComputedValues(position, bond, cleanPrice);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }


  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  
  protected Currency getCurrencyForTarget(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getPosition().getSecurity();
    return bond.getCurrency();
  }
  
  protected abstract Set<ComputedValue> getComputedValues(Position position, Bond bound, double cleanPrice);

}
