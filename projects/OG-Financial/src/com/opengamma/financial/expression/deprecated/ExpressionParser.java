/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression.deprecated;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.expression.UserExpression;
import com.opengamma.financial.expression.UserExpressionParser;

/**
 * Parses a user expression into an AST. User expressions are a VB/Excel style function syntax to allow
 * custom code to be executed within the Java stack. For example portolfio filtering and aggregation.
 * 
 * @deprecated Use the EL based form instead
 */
@Deprecated
public final class ExpressionParser extends UserExpressionParser {

  private static final Logger s_logger = LoggerFactory.getLogger(ExpressionParser.class);

  private final Map<String, Object> _constants = new HashMap<String, Object>();

  public ExpressionParser() {
  }

  @Override
  public void setConstant(final String var, final Object value) {
    _constants.put(var, value);
  }

  private static String extractStringFromToken(final String tokenText) {
    return StringEscapeUtils.unescapeJava(tokenText.substring(1, tokenText.length() - 1));
  }

  private Expression build(final Tree tree) {
    switch (tree.getType()) {
      case ExprParser.AND:
        return new Expression.And(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.EQ:
        return new Expression.Eq(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.FLOAT:
        return new Expression.Literal(Double.parseDouble(tree.getText()));
      case ExprParser.GT:
        return new Expression.Gt(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.GTE:
        return new Expression.Gte(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.IDENTIFIER: {
        final String var = tree.getText();
        if (_constants.containsKey(var)) {
          return new Expression.Literal(_constants.get(var));
        } else {
          return new Expression.Identifier(var);
        }
      }
      case ExprParser.INTEGER:
        return new Expression.Literal(Integer.parseInt(tree.getText()));
      case ExprParser.LT:
        return new Expression.Lt(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.LTE:
        return new Expression.Lte(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.NEQ:
        return new Expression.Neq(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.NOT:
        return new Expression.Not(build(tree.getChild(0)));
      case ExprParser.OR:
        return new Expression.Or(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.STRING:
        return new Expression.Literal(extractStringFromToken(tree.getText()));
      case ExprParser.STRING_IDENTIFIER:
        return new Expression.Identifier(extractStringFromToken(tree.getText().substring(1)));
      case ExprParser.TRUE:
        return new Expression.Literal(Boolean.TRUE);
      default:
        throw new IllegalArgumentException("Unexpected token " + tree.getType() + " / " + tree.toString());
    }
  }

  @Override
  public UserExpression parse(final String expression) {
    try {
      final ExprParser parser = new ExprParser(new CommonTokenStream(new ExprLexer(new ANTLRReaderStream(new StringReader(expression))))) {
        @Override
        public void reportError(final RecognitionException e) {
          throw new OpenGammaRuntimeException(e.getMessage());
        }
      };
      final ExprParser.root_return root = parser.root();
      return build(((Tree) root.getTree()).getChild(0));
    } catch (Throwable e) {
      s_logger.warn("Couldn't parse expression {} - {}", expression, e);
      throw new IllegalArgumentException(expression);
    }
  }

}
