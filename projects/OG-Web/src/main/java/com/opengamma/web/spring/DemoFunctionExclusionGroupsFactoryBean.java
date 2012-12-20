/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.exclusion.AbstractFunctionExclusionGroups;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates FunctionExclusionGroups appropriate for the {@link DemoStandardFunctionConfiguration} functions.
 */
public class DemoFunctionExclusionGroupsFactoryBean extends SingletonFactoryBean<FunctionExclusionGroups> {

  @Override
  protected FunctionExclusionGroups createObject() {
    return new AbstractFunctionExclusionGroups<String>() {
      @Override
      protected String getKey(final FunctionDefinition function) {
        if (function instanceof DefaultPropertyFunction) {
          return ((DefaultPropertyFunction) function).getMutualExclusionGroup();
        } else {
          return null;
        }
      }
    };
  }

}
