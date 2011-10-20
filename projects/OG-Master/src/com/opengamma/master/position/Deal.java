/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;
import com.opengamma.util.money.Currency;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;

import com.opengamma.util.PublicSPI;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.time.calendar.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * An empty interface for a deal.
 * <p>
 * The trade interface and class in OpenGamma is intended to model a trade
 * from the perspective of risk analytics.
 * The deal interface provides a hook to add more detail about the trade,
 * or "deal", from another perspective, such as trade booking.
 */
@PublicSPI
public interface Deal extends Bean {

  public static final String DEAL_CLASSNAME = "Deal~JavaClass";
  public static final String DEAL_TYPE = "Deal~dealType";
  public static final String DEAL_PREFIX = "Deal~";

  /**
   * Provides helper methods to store a {@link Deal}'s data in a map of strings and reload it into a
   * {@link Deal} instance.  Intended to be used for storing a {@link Deal} in a {@link Trade}'s
   * {@link Trade#getAttributes() attributes}.
   */
  // TODO this is temporary until we decide how trade attributes will be handled
  public static class AttributeEncoder {

    private static final Logger s_logger = LoggerFactory.getLogger(AttributeEncoder.class);

    private AttributeEncoder() {
    }

    @SuppressWarnings({"unchecked"})
    public static Deal read(Map<String, String> tradeAttributes) {
      String dealClass = tradeAttributes.get(DEAL_CLASSNAME);
      Deal deal = null;
      if (dealClass != null) {
        Class<?> cls;
        try {
          cls = AttributeEncoder.class.getClassLoader().loadClass(dealClass);
        } catch (ClassNotFoundException ex) {
          throw new OpenGammaRuntimeException("Unable to load deal class", ex);
        }
        MetaBean metaBean = JodaBeanUtils.metaBean(cls);
        deal = (Deal) metaBean.builder().build();
        for (Map.Entry<String, String> entry : tradeAttributes.entrySet()) {
          String key = entry.getKey();
          if (key.startsWith(DEAL_PREFIX) && !key.equals(DEAL_CLASSNAME) && !key.equals(DEAL_TYPE)) {
            MetaProperty<?> mp = metaBean.metaProperty(StringUtils.substringAfter(key, DEAL_PREFIX));
            // TODO should these be done using a Joda converter?
            String value = entry.getValue();
            s_logger.debug("Setting property {}({}) with value {}", new Object[]{mp, mp.propertyType(), value});
            if (mp.propertyType() == LocalDate.class) {
              ((MetaProperty) mp).set(deal, LocalDate.parse(value));
            } else if (mp.propertyType() == Currency.class) {
              ((MetaProperty) mp).set(deal, Currency.of(value));
            } else {
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
      for (MetaProperty<Object> mp : deal.metaBean().metaPropertyIterable()) {
        Object value = mp.get(deal);
        if (value != null) {
          attributes.put(DEAL_PREFIX + mp.name(), value.toString());
        }
      }
      return attributes;
    }
  }
}
