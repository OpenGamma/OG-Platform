/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.IndicatorType;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.IndicatorFieldType;
import org.joda.beans.impl.flexi.FlexiBean;

/**
 * Builder to convert FlexiBean to and from Fudge.
 */
@FudgeBuilderFor(FlexiBean.class)
public final class FlexiBeanBuilder implements FudgeBuilder<FlexiBean> {

  /**
   * Singleton instance.
   */
  public static final FlexiBeanBuilder INSTANCE = new FlexiBeanBuilder();

  /**
   * Constructor. Must have default no-arg for support as part of the FudgeBuilderFor contract.
   */
  public FlexiBeanBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FlexiBean bean) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    Map<String, Object> data = bean.toMap();
    for (Entry<String, Object> entry : data.entrySet()) {
      Object value = entry.getValue();
      if (value == null) {
        msg.add(entry.getKey(), null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsg(msg, entry.getKey(), null, value);
      }
    }
    return msg;
  }

  @Override
  public FlexiBean buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    final FlexiBean bean = new FlexiBean();
    List<FudgeField> fields = msg.getAllFields();
    for (FudgeField field : fields) {
      Object value = context.fieldValueToObject(field);
      value = (value instanceof IndicatorType) ? null : value;
      bean.set(field.getName(), value);
    }
    return bean;
  }

}
