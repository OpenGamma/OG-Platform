/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ComputationCacheQuery;

/**
 * Fudge message builder for {@code ComputationCacheQuery}.
 */
@FudgeBuilderFor(ComputationCacheQuery.class)
public class ComputationCacheQueryBuilder implements FudgeBuilder<ComputationCacheQuery>  {

  private static final String CALCULATION_CONFIGURATION_FIELD_NAME = "calculationConfigurationName";
  private static final String VALUE_SPECIFICATIONS_FIELD_NAME = "valueSpecifications";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ComputationCacheQuery object) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(CALCULATION_CONFIGURATION_FIELD_NAME, object.getCalculationConfigurationName());

    final MutableFudgeMsg valueSpecificationsMessage = context.newMessage();
    for (ValueSpecification valueSpecification : object.getValueSpecifications()) {
      context.addToMessage(valueSpecificationsMessage, null, null, valueSpecification);
    }
    msg.add(VALUE_SPECIFICATIONS_FIELD_NAME, valueSpecificationsMessage);

    return msg;
  }

  @Override
  public ComputationCacheQuery buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ComputationCacheQuery computationCacheQuery = new ComputationCacheQuery();
    computationCacheQuery.setCalculationConfigurationName(message.getString(CALCULATION_CONFIGURATION_FIELD_NAME));
    
    Collection<ValueSpecification> specs = Lists.newArrayList();
    
    FudgeMsg valueSpecificationMessage = message.getMessage(VALUE_SPECIFICATIONS_FIELD_NAME);
    for (FudgeField fudgeField : valueSpecificationMessage) {
      specs.add(context.fieldValueToObject(ValueSpecification.class, fudgeField));
    }
    computationCacheQuery.setValueSpecifications(specs);
    
    return computationCacheQuery;
  }

}
