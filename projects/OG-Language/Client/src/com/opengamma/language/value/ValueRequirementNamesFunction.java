/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.Parameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns the set of all requirement names defined in the {@link ValueRequirementNames} class.
 */
public class ValueRequirementNamesFunction implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ValueRequirementNamesFunction INSTANCE = new ValueRequirementNamesFunction();

  private static final Set<String> s_valueRequirementNames;

  static {
    final List<String> list = new ArrayList<String>();
    for (Field field : ValueRequirementNames.class.getDeclaredFields()) {
      try {
        list.add((String) field.get(null));
      } catch (Exception e) {
        // Ignore
      }
    }
    Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    s_valueRequirementNames = new LinkedHashSet<String>(list);
  }

  public static Set<String> getValueRequirementNames() {
    return Collections.unmodifiableSet(s_valueRequirementNames);
  }

  @Override
  public MetaFunction getMetaFunction() {
    final MetaFunction meta = new MetaFunction(Categories.VALUE, "ValueRequirementNames", Collections.<Parameter>emptyList(), new AbstractFunctionInvoker(Collections.<MetaParameter>emptyList()) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return getValueRequirementNames();
      }
    });
    meta.setDescription("Returns the set of standard Value Requirement Names defined within the system. Note that the Value Requirements available from the current function repository may differ");
    return meta;
  }

}
