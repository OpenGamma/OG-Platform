/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.opengamma.util.tuple.Pair;

import de.odysseus.el.util.SimpleResolver;

/**
 * Representation of an EL expression.
 */
/* package */class ELExpression extends UserExpression {

  private static final Logger s_logger = LoggerFactory.getLogger(ELExpression.class);

  private final ELExpressionParser _parser;
  private final ValueExpression _expr;

  public ELExpression(final ELExpressionParser parser, final ValueExpression expr) {
    _parser = parser;
    _expr = expr;
  }

  /**
   * Returns the original parser.
   * 
   * @return the parser
   */
  protected ELExpressionParser getParser() {
    return _parser;
  }

  protected ValueExpression getExpr() {
    return _expr;
  }

  private static final class Context extends ELContext {

    private final ELResolver _resolver;
    private final FunctionMapper _functionMapper;
    private final VariableMapper _variableMapper;

    public Context(final ELResolver resolver, final FunctionMapper functionMapper, final VariableMapper variableMapper) {
      _resolver = resolver;
      _functionMapper = functionMapper;
      _variableMapper = variableMapper;
    }

    @Override
    public ELResolver getELResolver() {
      return _resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
      return _functionMapper;
    }

    @Override
    public VariableMapper getVariableMapper() {
      return _variableMapper;
    }

  }

  /**
   * Extends the parse context with evaluation state for an evaluation context.
   * 
   * @return the context
   */
  @Override
  protected Object createContext(final Evaluator evaluator) {
    final SimpleResolver resolver = new SimpleResolver(true) {

      @Override
      public Object getValue(final ELContext context, final Object base, final Object property) {
        if (base == null) {
          context.setPropertyResolved(true);
          final Object value = evaluator.evaluateVariable((String) property);
          if (value == NA) {
            throw new PropertyNotFoundException("Variable " + property + " not defined");
          } else {
            return value;
          }
        } else if (property instanceof String) {
          final Pair<Class<?>, Function<Object, Object>> synthetic = getParser().getSynthetic(base, (String) property);
          if (synthetic != null) {
            context.setPropertyResolved(true);
            return synthetic.getSecond().apply(base);
          } else {
            Object value = super.getValue(context, base, property);
            if (!context.isPropertyResolved()) {
              value = evaluator.evaluateAttribute(base, (String) property);
              if (value != NA) {
                context.setPropertyResolved(true);
              }
            }
            return value;
          }
        } else {
          return super.getValue(context, base, property);
        }
      }

      @Override
      public Class<?> getType(final ELContext context, final Object base, final Object property) {
        if (base == null) {
          context.setPropertyResolved(true);
          final Object value = evaluator.evaluateVariable((String) property);
          if (value == NA) {
            throw new PropertyNotFoundException("Variable " + property + " not defined");
          } else {
            return value.getClass();
          }
        } else if (property instanceof String) {
          final Pair<Class<?>, Function<Object, Object>> synthetic = getParser().getSynthetic(base, (String) property);
          if (synthetic != null) {
            context.setPropertyResolved(true);
            return synthetic.getFirst();
          } else {
            Class<?> type = super.getType(context, base, property);
            if (!context.isPropertyResolved()) {
              final Object value = evaluator.evaluateAttribute(base, (String) property);
              if (value != NA) {
                context.setPropertyResolved(true);
                type = value.getClass();
              }
            }
            return type;
          }
        } else {
          return super.getType(context, base, property);
        }
      }

    };
    return new Context(resolver, null, null);
  }

  @Override
  protected Object evaluate(final Evaluator evaluator) {
    try {
      return getExpr().getValue((ELContext) getContext(evaluator));
    } catch (PropertyNotFoundException e) {
      s_logger.debug("Property not found - {}", e.getMessage());
    } catch (ELException e) {
      s_logger.warn("EL exception", e);
    }
    return NA;
  }

  @Override
  public String toString() {
    return _expr.getExpressionString();
  }

}
