/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts ExternalSchemeRank to/from an array of strings.
 */
public class ExternalSchemeRankConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<String[]> STRING_ARRAY = JavaTypeInfo.builder(String[].class).allowNull().get();
  private static final JavaTypeInfo<ExternalSchemeRank> EXTERNAL_SCHEME_RANK = JavaTypeInfo.builder(ExternalSchemeRank.class).allowNull().get();

  /**
   * Default instance.
   */
  public static final ExternalSchemeRankConverter INSTANCE = new ExternalSchemeRankConverter();

  protected ExternalSchemeRankConverter() {
    conversion(TypeMap.ZERO_LOSS, STRING_ARRAY, EXTERNAL_SCHEME_RANK, new Action<String[], ExternalSchemeRank>() {
      @Override
      protected ExternalSchemeRank convert(final String[] value) {
        return ExternalSchemeRank.ofStrings(value);
      }
    });
    conversion(TypeMap.MINOR_LOSS, EXTERNAL_SCHEME_RANK, STRING_ARRAY, new Action<ExternalSchemeRank, String[]>() {
      @Override
      protected String[] convert(final ExternalSchemeRank value) {
        return value.asStrings();
      }
    });
  }

}
