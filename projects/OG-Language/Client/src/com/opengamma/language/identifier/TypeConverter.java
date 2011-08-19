/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts identifier types. For example the ExternalId to/from a singleton bundle.
 */
public class TypeConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<ExternalId> EXTERNAL_ID = JavaTypeInfo.builder(ExternalId.class).get();
  private static final JavaTypeInfo<ExternalIdBundle> EXTERNAL_ID_BUNDLE = JavaTypeInfo.builder(ExternalIdBundle.class).get();

  public TypeConverter() {
    conversion(TypeMap.ZERO_LOSS, EXTERNAL_ID, EXTERNAL_ID_BUNDLE, new Action<ExternalId, ExternalIdBundle>() {

      @Override
      public ExternalId cast(final Object value) {
        return (ExternalId) value;
      }

      @Override
      public ExternalIdBundle convert(final ExternalId value) {
        return ExternalIdBundle.of(value);
      }
    }, new Action<ExternalIdBundle, ExternalId>() {

      @Override
      public ExternalIdBundle cast(final Object value) {
        final ExternalIdBundle bundle = (ExternalIdBundle) value;
        if (bundle.getExternalIds().size() == 1) {
          return bundle;
        } else {
          return null;
        }
      }

      @Override
      public ExternalId convert(final ExternalIdBundle value) {
        return value.iterator().next();
      }
    });
  }

}
