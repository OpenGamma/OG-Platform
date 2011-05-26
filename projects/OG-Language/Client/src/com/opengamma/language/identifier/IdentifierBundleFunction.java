/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.text.Ordinal;

/**
 * Constructs an identifier bundle from a set of identifiers.
 */
public class IdentifierBundleFunction implements PublishedFunction {

  private static final int MAX_PARAMETERS = 20;

  private static void getIdentifier(final Collection<Identifier> identifiers, final Value value) {
    if (value.getStringValue() != null) {
      identifiers.add(Identifier.parse(value.getStringValue()));
    } else if (value.getMessageValue() != null) {
      identifiers.add(Identifier.fromFudgeMsg(value.getMessageValue()));
    }
  }

  private static void getIdentifiers(final Collection<Identifier> identifiers, final Value[] values) {
    for (Value value : values) {
      getIdentifier(identifiers, value);
    }
  }

  private static void getIdentifiers(final Collection<Identifier> identifiers, final Data data) {
    if (data.getSingle() != null) {
      getIdentifier(identifiers, data.getSingle());
    } else if (data.getLinear() != null) {
      getIdentifiers(identifiers, data.getLinear());
    } else if (data.getMatrix() != null) {
      for (Value[] values : data.getMatrix()) {
        getIdentifiers(identifiers, values);
      }
    }
  }

  public static IdentifierBundle execute(final List<?> parameters) {
    final Collection<Identifier> identifiers = new ArrayList<Identifier>(parameters.size());
    for (Object parameter : parameters) {
      getIdentifiers(identifiers, (Data) parameter);
    }
    return IdentifierBundle.of(identifiers);
  }

  public static IdentifierBundle execute(final Object[] parameters) {
    final Collection<Identifier> identifiers = new ArrayList<Identifier>(parameters.length);
    for (Object parameter : parameters) {
      getIdentifiers(identifiers, (Data) parameter);
    }
    return IdentifierBundle.of(identifiers);
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = new ArrayList<MetaParameter>(MAX_PARAMETERS);
    for (int i = 1; i <= MAX_PARAMETERS; i++) {
      final MetaParameter param = new MetaParameter("id" + i, JavaTypeInfo.builder(Data.class).allowNull().get());
      param.setDescription("The " + Ordinal.get(i) + " identifier (or array of identifiers) to add to the bundle");
      args.add(param);
    }
    final FunctionInvoker invoker = new AbstractFunctionInvoker(args) {
      @Override
      public Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        return execute(parameters);
      }
    };
    final MetaFunction meta = new MetaFunction("IdentifierBundle", args, invoker);
    meta.setDescription("Creates an IdentifierBundle from one or more identifiers");
    return meta;
  }

}
