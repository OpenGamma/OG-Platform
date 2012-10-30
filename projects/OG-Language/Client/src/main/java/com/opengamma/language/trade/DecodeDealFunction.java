/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.master.position.Deal;
import com.opengamma.master.position.DealAttributeEncoder;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function to decode a {@link Deal} object from a set of deal attributes.
 */
public class DecodeDealFunction implements PublishedFunction {

  private final MetaFunction _metaFunction;
  
  public DecodeDealFunction() {
    MetaParameter attributeListParam = new MetaParameter("attributeList", JavaTypeInfo.builder(String.class).get());
    List<MetaParameter> params = ImmutableList.of(attributeListParam);
    _metaFunction = new MetaFunction(Categories.SECURITY, "DecodeDeal", params, new AbstractFunctionInvoker(params) {
      
      @Override
      protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
        String attributeList = (String) parameters[0];
        if (attributeList == null) {
          return null;
        }
        String[] attributes = attributeList.split(",");
        Map<String, String> attributeMap = new HashMap<String, String>(attributes.length);
        for (String attribute : attributes) {
          String[] attributeKeyValue = attribute.split("=");
          if (attributeKeyValue.length != 2) {
            throw new IllegalArgumentException("Unexpected attribute format: " + attributeKeyValue);
          }
          String key = attributeKeyValue[0];
          String value = attributeKeyValue[1];
          if (!key.startsWith(Deal.DEAL_PREFIX)) {
            key = Deal.DEAL_PREFIX + key;
          }
          attributeMap.put(key, value);
        }
        return DealAttributeEncoder.read(attributeMap);
      }
      
    });
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _metaFunction;
  }

}
