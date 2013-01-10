/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ abstract class AbstractTradeBuilder {

  // TODO where should these live? they're duplicated in FungibleTradeBuilder
  protected static final ExternalScheme CPTY_SCHEME = ExternalScheme.of("Cpty");
  protected static final String COUNTERPARTY = "counterparty";

  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final MetaBeanFactory _metaBeanFactory;

  /* package */ AbstractTradeBuilder(PositionMaster positionMaster,
                                     SecurityMaster securityMaster,
                                     Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(securityMaster, "securityManager");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notEmpty(metaBeans, "metaBeans");
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _metaBeanFactory = new MapMetaBeanFactory(metaBeans);
  }

  protected static Map<String, Object> property(String name,
                                                boolean optional,
                                                boolean readOnly,
                                                Map<String, Object> typeInfo) {
    return ImmutableMap.<String, Object>of("name", name,
                                           "type", "single",
                                           "optional", optional,
                                           "readOnly", readOnly,
                                           "types", ImmutableList.of(typeInfo));
  }

  protected static Map<String, Object> attributesProperty() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("name", "attributes");
    map.put("type", "map");
    map.put("optional", true); // can't be null but have a default value so client doesn't need to specify
    map.put("readOnly", false);
    map.put("types", ImmutableList.of(typeInfo("string", "")));
    map.put("valueTypes", ImmutableList.of(typeInfo("string", "")));
    return map;
  }

  protected static Map<String, Object> typeInfo(String expectedType, String actualType) {
    return ImmutableMap.<String, Object>of("beanType", false, "expectedType", expectedType, "actualType", actualType);
  }

  /**
   * Saves a position to the position master.
   * @param position The position
   * @return The saved position
   */
  /* package */ abstract ManageablePosition savePosition(ManageablePosition position);

  /* package */ abstract ManageablePosition getPosition(ManageableTrade trade);

  /* package */  SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /* package */ PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /* package */ MetaBeanFactory getMetaBeanFactory() {
    return _metaBeanFactory;
  }
}
