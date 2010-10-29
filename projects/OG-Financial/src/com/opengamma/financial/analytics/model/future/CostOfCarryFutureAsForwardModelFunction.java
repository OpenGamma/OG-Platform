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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.future.definition.FutureDefinition;
import com.opengamma.financial.model.future.definition.StandardFutureDataBundle;
import com.opengamma.financial.model.future.pricing.CostOfCarryFutureAsForwardModel;
import com.opengamma.financial.model.future.pricing.FutureModel;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class CostOfCarryFutureAsForwardModelFunction extends AbstractFunction.NonCompiledInvoker {

  private final FutureModel<StandardFutureDataBundle> _model = new CostOfCarryFutureAsForwardModel();
  private final FutureSecurityVisitor<Identifier> _visitor = new UnderlyingFutureSecurityVisitor();
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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FutureSecurity future = (FutureSecurity) target.getSecurity();
    final Identifier underlying = future.accept(_visitor);
    if (underlying == null) {
      throw new IllegalArgumentException("Could not get underlying identity key for " + future);
    }
    @SuppressWarnings("unused")
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    @SuppressWarnings("unused")
    final double spot = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(underlying));
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
      if (greek == null) {
        throw new IllegalArgumentException("Told to calculate " + v + " but could not be mapped to a greek");
      }
      requiredGreeks.add(greek);
    }
    final GreekResultCollection greeks = _model.getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement v : desiredValues) {
      greek = AVAILABLE_GREEKS.get(v.getValueName());
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final Double greekResult = greeks.get(greek);
      final ValueSpecification resultSpecification = new ValueSpecification(
          new ValueRequirement(v.getValueName(), ComputationTargetType.SECURITY, future.getUniqueIdentifier()),
          getUniqueIdentifier());
      final ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult);
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (SUPPORTED_FUTURES.contains(target.getSecurity().getClass())) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final FutureSecurity future = (FutureSecurity) target.getSecurity();
      final Identifier underlying = future.accept(_visitor);
      if (underlying == null) {
        throw new IllegalArgumentException("Could not get underlying identity key for " + future);
      }
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
        results.add(new ValueSpecification(
            new ValueRequirement(name, ComputationTargetType.SECURITY, future.getUniqueIdentifier()),
            getUniqueIdentifier()));
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

  private ValueRequirement getUnderlyingMarketDataRequirement(final Identifier id) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, id);
  }

  private class UnderlyingFutureSecurityVisitor implements FutureSecurityVisitor<Identifier> {

    @Override
    public Identifier visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      return null;
    }

    @Override
    public Identifier visitBondFutureSecurity(final BondFutureSecurity security) {
      return null;
    }

    @Override
    public Identifier visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      return security.getUnderlyingIdentifier();
    }

    @Override
    public Identifier visitFXFutureSecurity(final FXFutureSecurity security) {
      return null;
    }

    @Override
    public Identifier visitIndexFutureSecurity(final IndexFutureSecurity security) {
      return security.getUnderlyingIdentifier();
    }

    @Override
    public Identifier visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return null;
    }

    @Override
    public Identifier visitMetalFutureSecurity(final MetalFutureSecurity security) {
      return security.getUnderlyingIdentifier();
    }

    @Override
    public Identifier visitStockFutureSecurity(final StockFutureSecurity security) {
      return security.getUnderlyingIdentifier();
    }

  }
}
