/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.financial.expression.UserExpression;
import com.opengamma.financial.expression.UserExpression.DynamicAttributes;
import com.opengamma.financial.expression.UserExpression.DynamicVariables;
import com.opengamma.financial.expression.UserExpression.Evaluator;
import com.opengamma.financial.expression.deprecated.ExpressionParser;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Filters a portfolio according to a provided {@link UserExpression}
 */
public class ExpressionPortfolioFilter extends AbstractFilteringFunction {

  private final UserExpression _expression;

  /**
   * Creates a new filter from a string expression (in the Expr.g form)
   * 
   * @param expression string expression
   * @deprecated Use the alternative constructor so that the parsing dialect is explicit
   */
  @Deprecated
  public ExpressionPortfolioFilter(final String expression) {
    this(new ExpressionParser().parse(expression));
  }

  /**
   * Creates a new filter from an arbitrary user expression
   * 
   * @param expression the parsed user expression
   */
  public ExpressionPortfolioFilter(final UserExpression expression) {
    super("User expression");
    _expression = expression;
  }

  private UserExpression getExpression() {
    return _expression;
  }

  private static class AnyTradeAttribute implements Trade {

    private final Set<Trade> _trades;

    public AnyTradeAttribute(final Set<Trade> trades) {
      _trades = trades;
    }

    public Set<Trade> getTrades() {
      return _trades;
    }

    private Trade getTrade() {
      return getTrades().iterator().next();
    }

    @Override
    public UniqueId getUniqueId() {
      return getTrade().getUniqueId();
    }

    @Override
    public BigDecimal getQuantity() {
      return getTrade().getQuantity();
    }

    @Override
    public SecurityLink getSecurityLink() {
      return getTrade().getSecurityLink();
    }

    @Override
    public Security getSecurity() {
      return getTrade().getSecurity();
    }

    @Override
    public Map<String, String> getAttributes() {
      return getTrade().getAttributes();
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addAttribute(String key, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UniqueId getParentPositionId() {
      return getTrade().getParentPositionId();
    }

    @Override
    public Counterparty getCounterparty() {
      return getTrade().getCounterparty();
    }

    @Override
    public LocalDate getTradeDate() {
      return getTrade().getTradeDate();
    }

    @Override
    public OffsetTime getTradeTime() {
      return getTrade().getTradeTime();
    }

    @Override
    public Double getPremium() {
      return getTrade().getPremium();
    }

    @Override
    public Currency getPremiumCurrency() {
      return getTrade().getPremiumCurrency();
    }

    @Override
    public LocalDate getPremiumDate() {
      return getTrade().getPremiumDate();
    }

    @Override
    public OffsetTime getPremiumTime() {
      return getTrade().getPremiumTime();
    }

  }

  private static final DynamicAttributes s_dynamicAttributes = new DynamicAttributes() {

    @Override
    public Object getValue(final Object object, final String name) {
      if (object instanceof Position) {
        final Position position = (Position) object;
        if ("trade".equals(name)) {
          final Set<Trade> trades = position.getTrades();
          if (trades.size() == 1) {
            return trades.iterator().next();
          } else {
            return new AnyTradeAttribute(trades);
          }
        } else {
          Object value = position.getAttributes().get(name);
          if (value != null) {
            return value;
          } else {
            for (Trade trade : position.getTrades()) {
              value = trade.getAttributes().get(name);
              if (value != null) {
                return value;
              }
            }
            return UserExpression.NA;
          }
        }
      } else if (object instanceof AnyTradeAttribute) {
        final AnyTradeAttribute trades = (AnyTradeAttribute) object;
        for (Trade trade : trades.getTrades()) {
          final Object value = trade.getAttributes().get(name);
          if (value != null) {
            return value;
          }
        }
        return UserExpression.NA;
      } else if (object instanceof Trade) {
        final Trade trade = (Trade) object;
        final Object value = trade.getAttributes().get(name);
        if (value != null) {
          return value;
        } else {
          return UserExpression.NA;
        }
      } else {
        return UserExpression.NA;
      }
    }

  };

  @Override
  public boolean acceptPosition(final Position position) {
    final Evaluator eval = getExpression().evaluator();
    eval.setVariable("position", position);
    eval.setVariable("quantity", position.getQuantity().doubleValue());
    eval.setVariable("security", position.getSecurity());
    eval.setDynamicVariables(new DynamicVariables() {
      @Override
      public Object getValue(final String name) {
        return s_dynamicAttributes.getValue(position, name);
      }
    });
    eval.setDynamicAttributes(s_dynamicAttributes);
    return Boolean.TRUE.equals(eval.evaluate());
  }

}
