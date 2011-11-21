/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELException;
import javax.el.ExpressionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.tuple.Pair;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * Implementation of {@link UserExpressionParser} using the EL library. An augmented EL
 * grammar is available that supports a sequence of expressions and an "IF" construct.
 */
public class ELExpressionParser extends UserExpressionParser {

  private static final Logger s_logger = LoggerFactory.getLogger(ELExpressionParser.class);

  private static final Pattern s_if = Pattern.compile("^\\s*if\\s*\\(");

  private final ExpressionFactory _factory;
  private final SimpleContext _context;

  public ELExpressionParser() {
    _factory = new ExpressionFactoryImpl();
    _context = new SimpleContext();
  }

  protected ExpressionFactory getFactory() {
    return _factory;
  }

  protected SimpleContext getContext() {
    return _context;
  }

  @Override
  public void setConstant(final String name, final Object value) {
    getContext().getVariableMapper().setVariable(name, getFactory().createValueExpression(value, Object.class));
  }

  @Override
  public void setFunction(final String object, final String name, final Method method) {
    getContext().setFunction(object, name, method);
  }

  private UserExpression elParse(final String fragment) {
    // TODO: escape the string if it contains "${}" characters
    try {
      return new ELExpression(this, getFactory().createValueExpression(getContext(), "${" + fragment + "}", Object.class));
    } catch (ELException e) {
      s_logger.warn("EL exception = {}", e.getMessage());
      throw new IllegalArgumentException(fragment);
    }
  }

  private static int findEndQuote(final String str, final int i) {
    for (int j = i; j < str.length(); j++) {
      switch (str.charAt(j)) {
        case '\"':
          return j;
        case '\\':
          j++;
          break;
      }
    }
    return -1;
  }

  private static int findEndApostrophe(final String str, final int i) {
    for (int j = i; j < str.length(); j++) {
      switch (str.charAt(j)) {
        case '\'':
          return j;
        case '\\':
          j++;
          break;
      }
    }
    return -1;
  }

  private static int findCloseSquareBracket(final String str, final int i) {
    for (int j = i; j < str.length(); j++) {
      switch (str.charAt(j)) {
        case ']':
          return j;
        case '(':
          j = findCloseBracket(str, j + 1);
          break;
        case '\"':
          j = findEndQuote(str, j + 1);
          break;
        case '\'':
          j = findEndApostrophe(str, j + 1);
          break;
      }
      if (j == -1) {
        break;
      }
    }
    return -1;
  }

  private static int findCloseBracket(final String str, final int i) {
    for (int j = i; j < str.length(); j++) {
      switch (str.charAt(j)) {
        case '[':
          j = findCloseSquareBracket(str, j + 1);
          break;
        case '(':
          j = findCloseBracket(str, j + 1);
          break;
        case ')':
          return j;
        case '\"':
          j = findEndQuote(str, j + 1);
          break;
        case '\'':
          j = findEndApostrophe(str, j + 1);
          break;
      }
      if (j == -1) {
        break;
      }
    }
    return -1;
  }

  private static int findSemiColon(final String str, final int i) {
    for (int j = i; j < str.length(); j++) {
      switch (str.charAt(j)) {
        case ';':
          return j;
        case '\"':
          j = findEndQuote(str, j + 1);
          break;
        case '\'':
          j = findEndApostrophe(str, j + 1);
          break;
      }
      if (j == -1) {
        break;
      }
    }
    return -1;
  }

  private Pair<UserExpression, String> ueParse(final String source) {
    final Matcher m = s_if.matcher(source);
    if (m.find()) {
      final int openBracket = m.end() - 1;
      final int closeBracket = findCloseBracket(source, openBracket + 1);
      if (closeBracket < 0) {
        throw new IllegalArgumentException("No closing bracket for IF construct in " + source);
      }
      final UserExpression condition = elParse(source.substring(openBracket + 1, closeBracket));
      final int semiColon = findSemiColon(source, closeBracket + 1);
      final UserExpression operation;
      final String tail;
      if (semiColon != -1) {
        operation = elParse(source.substring(closeBracket + 1, semiColon));
        tail = source.substring(semiColon + 1);
      } else {
        operation = elParse(source.substring(closeBracket + 1));
        tail = "";
      }
      return Pair.<UserExpression, String>of(new IfExpression(condition, operation), tail);
    } else {
      final int semiColon = findSemiColon(source, 0);
      if (semiColon == -1) {
        return Pair.of(elParse(source), "");
      } else {
        return Pair.of(elParse(source.substring(0, semiColon)), source.substring(semiColon + 1));
      }
    }
  }

  @Override
  public UserExpression parse(final String source) {
    Pair<UserExpression, String> parsed = ueParse(source.trim());
    if (parsed.getSecond().length() == 0) {
      return parsed.getFirst();
    }
    List<UserExpression> exprs = new LinkedList<UserExpression>();
    exprs.add(parsed.getFirst());
    do {
      parsed = ueParse(parsed.getSecond());
      exprs.add(parsed.getFirst());
    } while (parsed.getSecond().length() > 0);
    return new FirstValidExpression(exprs);
  }

}
