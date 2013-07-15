/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tutorial;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.master.security.RawSecurity;

/**
 * Implementation of a function to calculate {@link ValueRequirementNames#PRESENT_VALUE} for the tutorial security.
 * <p>
 * This example code demonstrates how to extract reference data from a target security instance that is encoded with {@link RawSecurity} and use values produced by other functions in the analytics
 * library. This function does not containing a pricing algorithm - it is an example integration point between the OpenGamma engine and an external analytics library. The code in this function
 * implements API methods which allow the engine to build a dependency graph or execution plan using this function alongside others from the repository, and translates data (if necessary) to/from the
 * transport objects used by other parts of the system into the forms specific to the external analytics library.
 */
public class TutorialValueFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Identifies the function as operating on the {@link ComputationTargetType#SECURITY} type. The {@link #canApplyTo} check will further enforce that the target is a {@link Tutorial1Security}
   * instance.
   *
   * @return the target type, always {@code ComputationTargetType.SECURITY}
   */
  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.RAW_SECURITY;
  }

  /**
   * Checks that the target is a {@link Tutorial1Security}. The target will always be a security - the result from {@link #getTargetType} will be used by the graph builder. As we implemented the
   * tutorial asset class using {@link RawSecurity} we are checking that it contains a valid {@code Tutorial1Security} encoding (see {@link Tutorial1Security#isInstance}).
   *
   * @param context the function compilation context, not used in this example
   * @param target the target to consider
   * @return true if the target describes a {@code Tutorial1Security} and this function can be used for it
   */
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return Tutorial1Security.isInstance((RawSecurity) target.getSecurity());
  }

  /**
   * Unpacks the {@link Tutorial1Security} instance from the computation target. The target provided by the engine will always be a {@link RawSecurity} instance - the
   * {@link Tutorial1Security#fromRawSecurity} helper method will be used to unpack the content of the target. The {@link #canApplyTo} check must already have been passed before any code which uses
   * this will be used so there is no need to check the target (again) for validity.
   *
   * @param target the computation target as passed to the function
   * @return the unpacked {@code Tutorial1Security} instance
   */
  protected Tutorial1Security getSecurity(final ComputationTarget target) {
    return Tutorial1Security.fromRawSecurity((RawSecurity) target.getSecurity());
  }

  /**
   * Creates the property set that the output value of this function will be decorated with. In this example we are producing a financial value which should be decorated with the currency the value is
   * in. We could include other properties about the security or the pricing method we are using, but the currency is the minimum required for this tutorial to work. For example, the currency property
   * will be used to determine whether an aggregate price for the portfolio is possible by just adding the position level prices together, or whether the position level values must first be converted
   * to a common or default currency using a suitable spot rate.
   * <p>
   * The inherited {@link AbstractFunction#createValueProperties} method will add the function identifier metadata, we will supplement it with the currency that the output value is in (taken from the
   * security definition).
   *
   * @param security the security target the function is operating on
   * @return the value properties builder
   */
  protected ValueProperties.Builder createValueProperties(final Tutorial1Security security) {
    final ValueProperties.Builder builder = createValueProperties();
    builder.with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode());
    return builder;
  }

  /**
   * Describes the values that this function can calculate for the given target. This example produces a single output value for the security.
   *
   * @param context the function compilation context, not used in this example
   * @param target the target to consider - a {@link RawSecurity} containing a {@link Tutorial1Security}
   * @return the results - a set containing a single value specification for a {@link ValueRequirementNames#PRESENT_VALUE} for the security.
   */
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Tutorial1Security security = getSecurity(target);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties(security).get()));
  }

  /**
   * Describes the inputs that this function needs in order to produce its output. In this example, we will need a yield curve for the currency the asset is in and a value for the underlying/component
   * instrument that is referenced by it. These requirements are specified in the minimal form needed for the calculation to work. We do not specify precisely how the curves or surfaces are
   * constructed, nor how the underlying instrument is to be priced. This keeps the implementation here cleaner and specific to just the asset class we are working with. The system configuration
   * database will describe how the curves and surfaces for the currency are to be constructed. Functions specific to the underlying instrument will either provide a calculated price for that, use a
   * value calculated by a system external to the OpenGamma installation, or inform the engine that a suitable value can be sourced directly from the market data provider.
   *
   * @param context the function compilation context, not used in this example
   * @param target the target to consider - a {@link RawSecurity} containing a {@link Tutorial1Security}
   * @param desiredValue the output value this function must produce, not used in this example as this function only produces one result (as returned by {@link #getResults}) so it is not necessary to
   *          distinguish between them
   * @return a set describing the input values the function will need to execute
   */
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Tutorial1Security security = getSecurity(target);
    final ValueRequirement yieldCurve = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(security.getCurrency()), ValueProperties.with(
        ValuePropertyNames.CURVE, "SECONDARY").get());
    final ValueRequirement underlyingPrice = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUnderlying(), ValueProperties.with(
        ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get());
    return ImmutableSet.of(yieldCurve, underlyingPrice);
  }

  /**
   * Executes the function, using the inputs supplied by the other nodes in the dependency graph to produce an output value for the target security.
   * <p>
   * The execution behavior is split between this and another method. The code in this method deals with extracting the input values from the structures passed as parameters by the execution engine
   * and packaging up the output value into the structure required by the engine. The unpacked "transport" level objects will be passed to
   * {@link #execute(Tutorial1Security, Instant, YieldCurve, double)} which may in turn translate these objects into ones specific to the analytic library that is being linked to and call into that
   * library.
   *
   * @param executionContext the function execution context
   * @param inputs the execution results of other nodes in the dependency graph, giving the values requested by {@link #getRequirements}
   * @param target the target to price - a {@link RawSecurity} containing a {@link Tutorial1Security}
   * @param desiredValues the output values this function must produce, not used in this example as this function only produces one result (as returned by {@link #getResults}) so it is not necessary
   *          to distinguish between them
   * @return the execution results - a set containing a single {@link ComputedValue} containing the calculated price
   */
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Tutorial1Security security = getSecurity(target);
    final Instant valuationTime = executionContext.getValuationTime();
    final YieldCurve yieldCurve = (YieldCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    final double underlyingPrice = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    final double myResult = execute(security, valuationTime, yieldCurve, underlyingPrice);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties(security).get()), myResult));
  }

  /**
   * Executes the function, using the values unpacked by {@link #execute(FunctionExecutionContext, FunctionInputs, ComputationTarget, Set)}.
   * <p>
   * Splitting the execution behavior between this and the API method makes it easier to identify code that is mostly specific to the OpenGamma API (the other {@code execute} method) and code that is
   * mostly specific to the analytics that this integration wrapper is exposing to the OpenGamma engine.
   *
   * @param security the target security instance, not null
   * @param valuationTime the evaluation date/time, not null
   * @param yieldCurve the yield curve input produced by other nodes in the dependency graph, not null
   * @param underlyingPrice the price of the underlying instrument produced by other nodes in the dependency graph
   * @return the price calculated by the underlying analytics
   */
  protected double execute(final Tutorial1Security security, final Instant valuationTime, final YieldCurve yieldCurve, final double underlyingPrice) {
    // ... call out to an analytics library to calculate the price
    // The next line produces a (fairly meaningless) value so that this function can be used in a view
    return yieldCurve.getDiscountFactor(1d) * underlyingPrice;
  }

}
