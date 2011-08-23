/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts identifier types. For example the ExternalId to/from a singleton bundle.
 */
public class TypeConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<ExternalId> EXTERNAL_ID = JavaTypeInfo.builder(ExternalId.class).get();
  private static final JavaTypeInfo<ExternalIdBundle> EXTERNAL_ID_BUNDLE = JavaTypeInfo.builder(ExternalIdBundle.class).get();
  private static final JavaTypeInfo<ObjectId> OBJECT_ID = JavaTypeInfo.builder(ObjectId.class).get();
  private static final JavaTypeInfo<UniqueId> UNIQUE_ID = JavaTypeInfo.builder(UniqueId.class).get();

  public TypeConverter() {
    super(TypeConverter.class.getName());
    conversion(TypeMap.ZERO_LOSS, EXTERNAL_ID, EXTERNAL_ID_BUNDLE, new Action<ExternalId, ExternalIdBundle>() {
      @Override
      protected ExternalIdBundle convert(final ExternalId value) {
        return ExternalIdBundle.of(value);
      }
    }, new Action<ExternalIdBundle, ExternalId>() {

      @Override
      protected ExternalIdBundle cast(final Object value) {
        final ExternalIdBundle bundle = (ExternalIdBundle) value;
        if (bundle.getExternalIds().size() == 1) {
          return bundle;
        } else {
          return null;
        }
      }

      @Override
      protected ExternalId convert(final ExternalIdBundle value) {
        return value.iterator().next();
      }

    });
    conversion(TypeMap.ZERO_LOSS, UNIQUE_ID, OBJECT_ID, new Action<UniqueId, ObjectId>() {
      @Override
      protected ObjectId convert(final UniqueId value) {
        return value.getObjectId();
      }
    }, new Action<ObjectId, UniqueId>() {
      @Override
      protected UniqueId convert(final ObjectId value) {
        return value.atLatestVersion();
      }
    });
  }

}
