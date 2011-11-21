/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataELCompiler.class);
  
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
      synchronized (MarketDataELFunctions.class) {
        MarketDataELFunctions.setCompiler(MarketDataELCompiler.this);
        s_logger.debug("Applying {} to {}", _expr, requirement);
        final UserExpression.Evaluator eval = getExpr().evaluator();
        eval.setVariable("x", original);
        switch (requirement.getTargetSpecification().getType()) {
          case SECURITY:
            eval.setVariable("security", getSecuritySource().getSecurity(requirement.getTargetSpecification().getUniqueId()));
            break;
          case PRIMITIVE:
            if (requirement.getTargetSpecification().getIdentifier() != null) {
              eval.setVariable("externalId", requirement.getTargetSpecification().getIdentifier());
            }
            if (requirement.getTargetSpecification().getUniqueId() != null) {
              eval.setVariable("uniqueId", requirement.getTargetSpecification().getUniqueId());
            }
            break;
        }
        eval.setVariable("value", requirement.getValueName());
        final Object result = eval.evaluate();
        if (result == UserExpression.NA) {
          s_logger.debug("Evaluation failed - using original {}", original);
          return original;
        } else {
          s_logger.debug("Evaluation of {} to {}", original, result);
          return result;
        }
      }
    }

  }

  private final SecuritySource _securitySource;
  private final UserExpressionParser _parser;

  public MarketDataELCompiler(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _parser = new ELExpressionParser();
    try {
      _parser.setFunction("Security", "get", MarketDataELFunctions.class.getMethod("getSecurity", Object.class));
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Caught", ex);
    }
  }

  @Override
  public OverrideOperation compile(final String operation) {
    final UserExpression expr = getParser().parse(operation);
    return new Evaluator(expr);
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  private UserExpressionParser getParser() {
    return _parser;
  }

}
