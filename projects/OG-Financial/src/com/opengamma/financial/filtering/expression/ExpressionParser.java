/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering.expression;

import java.io.StringReader;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Parses a user expression into an AST. User expressions are a VB/Excel style function syntax to allow
 * custom code to be executed within the Java stack. For example portolfio filtering and aggregation.
 * 
 * @deprecated Use com.opengamma.financial.expression.UserExpressionParser instead
 */
@Deprecated
public final class ExpressionParser {

  private static final Logger s_logger = LoggerFactory.getLogger(ExpressionParser.class);

  /**
   * Prevent instantiation.
   */
  private ExpressionParser() {
  }

  private static String extractStringFromToken(final String tokenText) {
    return StringEscapeUtils.unescapeJava(tokenText.substring(1, tokenText.length() - 1));
  }

  private static UserExpression build(final Tree tree) {
    switch (tree.getType()) {
      case ExprParser.AND:
        return new UserExpression.And(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.EQ:
        return new UserExpression.Eq(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.FLOAT:
        return new UserExpression.Literal(Double.parseDouble(tree.getText()));
      case ExprParser.GT:
        return new UserExpression.Gt(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.GTE:
        return new UserExpression.Gte(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.IDENTIFIER:
        return new UserExpression.Identifier(tree.getText());
      case ExprParser.INTEGER:
        return new UserExpression.Literal(Integer.parseInt(tree.getText()));
      case ExprParser.LT:
        return new UserExpression.Lt(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.LTE:
        return new UserExpression.Lte(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.NEQ:
        return new UserExpression.Neq(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.NOT:
        return new UserExpression.Not(build(tree.getChild(0)));
      case ExprParser.OR:
        return new UserExpression.Or(build(tree.getChild(0)), build(tree.getChild(1)));
      case ExprParser.STRING:
        return new UserExpression.Literal(extractStringFromToken(tree.getText()));
      case ExprParser.STRING_IDENTIFIER:
        return new UserExpression.Identifier(extractStringFromToken(tree.getText().substring(1)));
      case ExprParser.TRUE:
        return new UserExpression.Literal(Boolean.TRUE);
      default:
        throw new IllegalArgumentException("Unexpected token " + tree.getType() + " / " + tree.toString());
    }
  }

  public static UserExpression parse(final String expression) {
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
      return null;
    }
  }

}
