/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import static com.opengamma.master.position.Deal.DEAL_CLASSNAME;
import static com.opengamma.master.position.Deal.DEAL_PREFIX;
import static com.opengamma.master.position.Deal.DEAL_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;

/**
 * Provides helper methods to store a {@link Deal}'s data in a map of strings and reload it into a
 * {@link Deal} instance.  Intended to be used for storing a {@link Deal} in a {@link Trade}'s
 * {@link Trade#getAttributes() attributes}.
 */
public final class DealAttributeEncoder {

  private static final Logger s_logger = LoggerFactory.getLogger(DealAttributeEncoder.class);
  
  /**
   * Resticted constructor
   */
  private DealAttributeEncoder() {
  }

  public static Deal read(Map<String, String> tradeAttributes) {
    String dealClass = tradeAttributes.get(DEAL_CLASSNAME);
    Deal deal = null;
    if (dealClass != null) {
      Class<?> cls;
      try {
        cls = DealAttributeEncoder.class.getClassLoader().loadClass(dealClass);
      } catch (ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Unable to load deal class", ex);
      }
      MetaBean metaBean = JodaBeanUtils.metaBean(cls);
      deal = (Deal) metaBean.builder().build();
      for (Map.Entry<String, String> entry : tradeAttributes.entrySet()) {
        String key = entry.getKey();
        if (key.startsWith(DEAL_PREFIX) && !key.equals(DEAL_CLASSNAME) && !key.equals(DEAL_TYPE)) {
          String propertyName = StringUtils.substringAfter(key, DEAL_PREFIX);
          if (metaBean.metaPropertyExists(propertyName)) {
            MetaProperty<?> mp = metaBean.metaProperty(propertyName);
            String value = entry.getValue();
            if (s_logger.isDebugEnabled()) {
              s_logger.debug("Setting property {}({}) with value {}", new Object[]{mp, mp.propertyType(), value});
            }
            mp.setString(deal, value);
          }
        }
      }
    }
    return deal;
  }

  public static Map<String, String> write(Deal deal) {
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put(DEAL_CLASSNAME, deal.getClass().getName());
    for (MetaProperty<?> mp : deal.metaBean().metaPropertyIterable()) {
      Object value = mp.get(deal);
      if (value != null) {
        @SuppressWarnings("unchecked")
        StringConverter<Object> stringConverter = (StringConverter<Object>) JodaBeanUtils.stringConverter().findConverter(mp.propertyType());
        attributes.put(DEAL_PREFIX + mp.name(), stringConverter.convertToString(value));
      }
    }
    return attributes;
  }

}
