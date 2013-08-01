/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Extends existing Greek Functions, reported at Security levels to sum over Positions. <p>
 * e.g. If a view asks for POSITION_DELTA, this will create a requirement for DELTA. <p>
 * NOTE! DELTA in the example, is the mathematical dV/dS, and does not contain any unit contract size.
 * The POSITION_DELTA *does* include any contract multiplier. <p>
 * The properties of the position-level requirement will match those of the security level requirement.
 * TODO Review the scope of this Function. e.g. by creating a canApplyTo().
 */
public class PositionGreeksFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _positionReqName;
  private final String _securityReqName;
  
  private static final PositionGreekContractMultiplier s_contractMultiplier = PositionGreekContractMultiplier.getInstance();
  
  public PositionGreeksFunction(final String positionReqName, final String securityReqName) {
    _positionReqName = positionReqName;
    _securityReqName = securityReqName;
  }
  
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    // 1. Get Security Greek
    // Confirm the desired Value is in our advertised set
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    String desiredName = desiredValue.getValueName();
          
    // Ensure the securityGreek's value was successfully obtained 
    Double secGreekValue = null;
    ComputedValue inputVal = inputs.getComputedValue(getSecurityReqName());
    if (inputVal != null) {
      secGreekValue = (Double) inputVal.getValue();
    } else {
      s_logger.error("Did not satisfy requirement," + getSecurityReqName() + ", for trade " + target.getPositionOrTrade().getUniqueId());
    }

    // 2a. Scale to mathematical Greek by point value for a single contract (unit Notional)
    final FinancialSecurity security = (FinancialSecurity) target.getPositionOrTrade().getSecurity();
    final Double contractGreekValue = secGreekValue * security.accept(s_contractMultiplier);
    // 2b. Scale by the position quantity
    final Double posGreekValue = contractGreekValue * target.getPositionOrTrade().getQuantity().doubleValue();
    
    // 3. Create specification and return
    final ValueSpecification valueSpecification = new ValueSpecification(desiredName, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, posGreekValue);
    return Sets.newHashSet(result);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getPositionReqName(), target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // inputs provide the properties of the required security greek. These we pass through to the position
    final ValueSpecification secGreekSpec = inputs.keySet().iterator().next();
    if (secGreekSpec.getValueName() != getSecurityReqName()) {
      return null;
    }
    Security security = target.getPositionOrTrade().getSecurity();
    String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties properties = secGreekSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId())
        .withoutAny(ValuePropertyNames.CURRENCY).with(ValuePropertyNames.CURRENCY, currency)
        .get();
    return Collections.singleton(new ValueSpecification(getPositionReqName(), target.toSpecification(), properties));
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (desiredValue.getValueName() != getPositionReqName()) {
      return null;
    }
    
    final ValueRequirement secGreekReq = new ValueRequirement(getSecurityReqName(), ComputationTargetSpecification.of(target.getPositionOrTrade().getSecurity()),
          desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION));
    final Set<ValueRequirement> requirements = Sets.newHashSet(secGreekReq);
    return requirements;
  }
  
  public String getPositionReqName() {
    return _positionReqName;
  }

  public String getSecurityReqName() {
    return _securityReqName;
  }

  private static final Logger s_logger = LoggerFactory.getLogger(PositionGreeksFunction.class);

}
