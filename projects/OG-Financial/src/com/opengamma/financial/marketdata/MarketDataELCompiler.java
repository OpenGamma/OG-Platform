/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.expression.ELExpressionParser;
import com.opengamma.financial.expression.UserExpression;
import com.opengamma.financial.expression.UserExpressionParser;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link OverrideOperationCompiler} that allows market data
 * overrides to be expressed as EL expressions.
 */
public class MarketDataELCompiler implements OverrideOperationCompiler {

  private final class Evaluator implements OverrideOperation {

    private final UserExpression _expr;

    public Evaluator(final UserExpression expr) {
      _expr = expr;
    }

    private UserExpression getExpr() {
      return _expr;
    }

    @Override
    public Object apply(final ValueRequirement requirement, final Object original) {
      final UserExpression.Evaluator eval = getExpr().evaluator();
      eval.setVariable("x", original);
      switch (requirement.getTargetSpecification().getType()) {
        case SECURITY:
          eval.setVariable("security", getSecuritySource().getSecurity(requirement.getTargetSpecification().getUniqueId()));
          break;
      }
      final Object result = eval.evaluate();
      if (result == UserExpression.NA) {
        return original;
      } else {
        return result;
      }
    }

  }

  private final SecuritySource _securitySource;
  private final UserExpressionParser _parser;

  public MarketDataELCompiler(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _parser = new ELExpressionParser();
  }

  @Override
  public OverrideOperation compile(final String operation) {
    final UserExpression expr = getParser().parse(operation);
    return new Evaluator(expr);
  }

  private SecuritySource getSecuritySource() {
    return _securitySource;
  }

  private UserExpressionParser getParser() {
    return _parser;
  }

}
