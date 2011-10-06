/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering.expression;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.filtering.AbstractFilteringFunction;

/**
 * Filters a portfolio according to a provided {@link ExpressionParser}
 */
public class ExpressionPortfolioFilter extends AbstractFilteringFunction {

  private final UserExpression _expression;

  private static Object visitSecurityIdentifier(final String identifier, final Security security) {
    if (security == null) {
      return null;
    }
    if ("Name".equals(identifier)) {
      return security.getName();
    } else if ("Type".equals(identifier)) {
      return security.getSecurityType();
    }
    return null;
  }

  private static Object visitPositionAttribute(final String attribute, final Position position) {
    return position.getAttributes().get(attribute);
  }

  private static Object visitPositionIdentifier(final String identifier, final Position position) {
    if (identifier.startsWith("Attribute.")) {
      return visitPositionAttribute(identifier.substring(10), position);
    } else if ("Quantity".equals(identifier)) {
      return position.getQuantity();
    }
    return visitPositionAttribute(identifier, position);
  }

  private static Object visitTradeAttribute(final String attribute, final Position position) {
    for (Trade trade : position.getTrades()) {
      final String value = trade.getAttributes().get(attribute);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static Object visitTradeIdentifier(final String identifier, final Position position) {
    if (identifier.startsWith("Attribute.")) {
      return visitTradeAttribute(identifier.substring(10), position);
    } else if ("Counterparty".equals(identifier)) {
      for (Trade trade : position.getTrades()) {
        if (trade.getCounterparty() != null) {
          return trade.getCounterparty();
        }
      }
      return null;
    } else if ("Premium".equals(identifier)) {
      for (Trade trade : position.getTrades()) {
        if (trade.getPremium() != null) {
          return trade.getPremium();
        }
      }
      return null;
    } else if ("PremiumCurrency".equals(identifier)) {
      for (Trade trade : position.getTrades()) {
        if (trade.getPremiumCurrency() != null) {
          return trade.getPremiumCurrency();
        }
      }
      return null;
    } else if ("PremiumDate".equals(identifier)) {
      for (Trade trade : position.getTrades()) {
        if (trade.getPremiumDate() != null) {
          return trade.getPremiumDate();
        }
      }
      return null;
    } else if ("PremiumTime".equals(identifier)) {
      for (Trade trade : position.getTrades()) {
        if (trade.getPremiumTime() != null) {
          return trade.getPremiumTime();
        }
      }
      return null;
    }
    return visitTradeAttribute(identifier, position);
  }

  private static UserExpressionVisitor<Object, Position> s_evaluator = new AbstractExpressionEvaluator<Position>() {
    @Override
    public Object visitIdentifier(final String identifier, final Position position) {
      Object value;
      if (identifier.startsWith("Position.")) {
        return visitPositionIdentifier(identifier.substring(9), position);
      } else if (identifier.startsWith("Security.")) {
        return visitSecurityIdentifier(identifier.substring(9), position.getSecurity());
      } else if (identifier.startsWith("Trade.")) {
        return visitTradeIdentifier(identifier.substring(6), position);
      }
      value = visitPositionIdentifier(identifier, position);
      if (value != null) {
        return value;
      }
      value = visitSecurityIdentifier(identifier, position.getSecurity());
      if (value != null) {
        return value;
      }
      return visitTradeIdentifier(identifier, position);
    }
  };

  public static UserExpressionVisitor<Object, Position> getPositionEvaluator() {
    return s_evaluator;
  }

  public ExpressionPortfolioFilter(final String expression) {
    super("User expression");
    final UserExpression ue = ExpressionParser.parse(expression);
    if (ue == null) {
      throw new IllegalArgumentException("Invalid expression");
    }
    _expression = ue;
  }

  private UserExpression getExpression() {
    return _expression;
  }

  @Override
  public boolean acceptPosition(final Position position) {
    final Object result = getExpression().accept(getPositionEvaluator(), position);
    return Boolean.TRUE.equals(result);
  }

}
