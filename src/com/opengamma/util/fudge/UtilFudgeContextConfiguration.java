/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.time.ExpiryFieldType;

/**
 * Configuration for Fudge including common elements for all projects.
 * <p>
 * This configures the {@code ExtendedFudgeBuilderFactory}.
 */
public final class UtilFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * The singleton configuration.
   */
  public static final FudgeContextConfiguration INSTANCE = new UtilFudgeContextConfiguration();

  /**
   * Restricted constructor.
   */
  private UtilFudgeContextConfiguration() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    ExtendedFudgeBuilderFactory.init(dictionary);
    dictionary.addBuilder(FlexiBean.class, FlexiBeanBuilder.INSTANCE);
  }

  @Override
  public void configureFudgeTypeDictionary(final FudgeTypeDictionary dictionary) {
    dictionary.addType(ExpiryFieldType.INSTANCE);
  }

}
