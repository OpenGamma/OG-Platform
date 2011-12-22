/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.security;

import java.util.Collections;
import java.util.List;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.object.CreateObjectFunction;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A function which creates a security from its public constructor.
 * 
 * @param <T> the class being constructed
 */
public class CreateSecurityFunction<T extends ManageableSecurity> extends CreateObjectFunction<T> {

  public CreateSecurityFunction(final Class<T> clazz, final String description, final String[] parameterNames, final String[] parameterDescriptions) {
    super(Categories.SECURITY, clazz.getSimpleName(), clazz, description, parameterNames, parameterDescriptions);
  }

  protected List<MetaParameter> getPrependedParameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    name.setDescription("The display name or label of the security");
    return Collections.singletonList(name);
  }

  protected Object postConstruction(final SessionContext context, final T newInstance, final Object[] parameters) {
    newInstance.setName((String) parameters[0]);
    return newInstance;
  }

}
