/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AttributeMapAdapter extends XmlAdapter<AdditionalAttributes, Map<String, String>> {

  @Override
  public Map<String, String> unmarshal(AdditionalAttributes attrs) throws Exception {

    Map<String, String> map = Maps.newHashMap();

    for (Attribute attribute : attrs.getAttributes()) {
      map.put(attribute.getName(), attribute.getValue());
    }
    return map;
  }

  @Override
  public AdditionalAttributes marshal(Map<String, String> map) throws Exception {

    List<Attribute> attrs = Lists.newArrayList();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      Attribute attribute = new Attribute();
      attribute.setName(entry.getKey());
      attribute.setValue(entry.getValue());
      attrs.add(attribute);
    }

    AdditionalAttributes attributes = new AdditionalAttributes();
    attributes.setAttributes(attrs);
    return attributes;
  }
}
