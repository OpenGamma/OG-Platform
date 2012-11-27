/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.trade;

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
 * Function to encode a {@link Deal} object into a set of deal attributes.
 */
public class EncodeDealFunction implements PublishedFunction {

  private final MetaFunction _metaFunction;
  
  public EncodeDealFunction() {
    MetaParameter dealParam = new MetaParameter("deal", JavaTypeInfo.builder(Deal.class).get());
    List<MetaParameter> params = ImmutableList.of(dealParam);
    _metaFunction = new MetaFunction(Categories.SECURITY, "EncodeDeal", params, new AbstractFunctionInvoker(params) {
      
      @Override
      protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
        Deal deal = (Deal) parameters[0];
        if (deal == null) {
          return null;
        }
        Map<String, String> attributeMap = DealAttributeEncoder.write(deal);
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> attributeEntry : attributeMap.entrySet()) {
          if (isFirst) {
            isFirst = false;
          } else {
            sb.append(',');
          }
          sb.append(attributeEntry.getKey()).append('=').append(attributeEntry.getValue());
        }
        return sb.toString();
      }
      
    });
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _metaFunction;
  }

}
