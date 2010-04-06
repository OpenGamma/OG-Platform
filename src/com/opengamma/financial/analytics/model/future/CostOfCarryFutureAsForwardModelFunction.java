/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.MarketDataFieldNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.future.definition.FutureDefinition;
import com.opengamma.financial.model.future.definition.StandardFutureDataBundle;
import com.opengamma.financial.model.future.pricing.CostOfCarryFutureAsForwardModel;
import com.opengamma.financial.model.future.pricing.FutureModel;
import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.FutureSecurityVisitor;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.StockFutureSecurity;
import com.opengamma.id.DomainSpecificIdentifier;

/**
 * 
 *
 * @author emcleod
 */
public class CostOfCarryFutureAsForwardModelFunction extends AbstractFunction implements FunctionInvoker {
  private final FutureModel<StandardFutureDataBundle> _model = new CostOfCarryFutureAsForwardModel();
  private final FutureSecurityVisitor<DomainSpecificIdentifier> _visitor = new UnderlyingFutureSecurityVisitor();
  private static final Set<Class<? extends FutureSecurity>> SUPPORTED_FUTURES = new HashSet<Class<? extends FutureSecurity>>();
  private static final Map<String, Greek> AVAILABLE_GREEKS;

  static {
    SUPPORTED_FUTURES.add(MetalFutureSecurity.class);
    SUPPORTED_FUTURES.add(IndexFutureSecurity.class);
    SUPPORTED_FUTURES.add(StockFutureSecurity.class);
    AVAILABLE_GREEKS = new TreeMap<String, Greek>();
    AVAILABLE_GREEKS.put(ValueRequirementNames.FAIR_VALUE, Greek.FAIR_PRICE);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA, Greek.DELTA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FutureSecurity future = (FutureSecurity) target.getSecurity();
    final DomainSpecificIdentifier underlying = future.accept(_visitor);
    if (underlying == null)
      throw new IllegalArgumentException("Could not get underlying identity key for " + future);
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final double spot = ((FudgeFieldContainer) inputs.getValue(getUnderlyingMarketDataRequirement(underlying))).getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);
    // final double yield = getYield();
    // final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement());
    // final double storageCost = getStorageCost();
    // final StandardFutureDataBundle data = new StandardFutureDataBundle(yield, discountCurve, spot, now, storageCost);
    final FutureDefinition definition = new FutureDefinition(future.getExpiry());
    final StandardFutureDataBundle data = null;
    final Set<Greek> requiredGreeks = new HashSet<Greek>();
    Greek greek;
    for (final ValueRequirement v : desiredValues) {
      greek = AVAILABLE_GREEKS.get(v.getValueName());
      if (greek == null)
        throw new IllegalArgumentException("Told to calculate " + v + " but could not be mapped to a greek");
      requiredGreeks.add(greek);
    }
    final GreekResultCollection greeks = _model.getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement v : desiredValues) {
      greek = AVAILABLE_GREEKS.get(v.getValueName());
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final GreekResult<?> greekResult = greeks.get(greek);
      final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(v.getValueName(), ComputationTargetType.SECURITY, future.getIdentityKey()));
      final ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult.getResult());
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY)
      return false;
    if (SUPPORTED_FUTURES.contains(target.getSecurity().getClass()))
      return true;
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final FutureSecurity future = (FutureSecurity) target.getSecurity();
      final DomainSpecificIdentifier underlying = future.accept(_visitor);
      if (underlying == null)
        throw new IllegalArgumentException("Could not get underlying identity key for " + future);
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(getUnderlyingMarketDataRequirement(underlying));
      // requirements.add(getDiscountCurveMarketDataRequirement(future.getCurrency()));
      // requirements.add(getYield());
      // requirements.add(getStorageCost());
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final FutureSecurity future = (FutureSecurity) target.getSecurity();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final String name : AVAILABLE_GREEKS.keySet()) {
        results.add(new ValueSpecification(new ValueRequirement(name, ComputationTargetType.SECURITY, future.getIdentityKey())));
      }
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "CostOfCarryFutureAsForwardModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private ValueRequirement getUnderlyingMarketDataRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, id);
  }

  private class UnderlyingFutureSecurityVisitor implements FutureSecurityVisitor<DomainSpecificIdentifier> {

    @Override
    public DomainSpecificIdentifier visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      return null;
    }

    @Override
    public DomainSpecificIdentifier visitBondFutureSecurity(final BondFutureSecurity security) {
      return null;
    }

    @Override
    public DomainSpecificIdentifier visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      return null;
    }

    @Override
    public DomainSpecificIdentifier visitFXFutureSecurity(final FXFutureSecurity security) {
      return null;
    }

    @Override
    public DomainSpecificIdentifier visitIndexFutureSecurity(final IndexFutureSecurity security) {
      return security.getUnderlyingIdentityKey();
    }

    @Override
    public DomainSpecificIdentifier visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return null;
    }

    @Override
    public DomainSpecificIdentifier visitMetalFutureSecurity(final MetalFutureSecurity security) {
      return security.getUnderlyingIdentityKey();
    }

    @Override
    public DomainSpecificIdentifier visitStockFutureSecurity(final StockFutureSecurity security) {
      return security.getUnderlyingIdentityKey();
    }

  }
}
