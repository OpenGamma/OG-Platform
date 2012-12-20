/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Provides the most primitive level of view client type conversion. View clients are converted to/from the {@link ViewClientKey}
 * representation which is least efficient but all that is available in the general case. A specific language binding that can
 * track objects should replace this with a conversion scheme that can work with the references detached from {@link UserViewClients}.
 * <p>
 * It is expected that functions which take a {@link ViewClientHandle} as a parameter should unlock it when they are done. 
 */
public class UserViewClientConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final UserViewClientConverter INSTANCE = new UserViewClientConverter();

  private static final JavaTypeInfo<ViewClientKey> VIEW_CLIENT_KEY = JavaTypeInfo.builder(ViewClientKey.class).get();
  private static final JavaTypeInfo<ViewClientHandle> VIEW_CLIENT_HANDLE = JavaTypeInfo.builder(ViewClientHandle.class).get();

  private static final TypeMap TO_VIEW_CLIENT_KEY = TypeMap.of(ZERO_LOSS, VIEW_CLIENT_HANDLE);
  private static final TypeMap TO_VIEW_CLIENT_HANDLE = TypeMap.of(ZERO_LOSS, VIEW_CLIENT_KEY);

  protected UserViewClientConverter() {
  }

  /**
   * The key used by the default type converter. A binding specific converter should be declared with this key to replace the
   * default.
   * 
   * @return the key
   */
  public static String getTypeConverterKeyImpl() {
    return UserViewClient.class.getSimpleName();
  }

  @Override
  public String getTypeConverterKey() {
    return getTypeConverterKeyImpl();
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == ViewClientKey.class) || (targetType.getRawClass() == ViewClientHandle.class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (type.getRawClass() == ViewClientKey.class) {
      final ViewClientHandle viewClient = (ViewClientHandle) value;
      conversionContext.setResult(viewClient.get().getViewClientKey());
      viewClient.unlock();
    } else {
      final ViewClientKey key = (ViewClientKey) value;
      conversionContext.setResult(conversionContext.getUserContext().getViewClients().lockViewClient(key));
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == ViewClientKey.class) {
      return TO_VIEW_CLIENT_KEY;
    } else {
      return TO_VIEW_CLIENT_HANDLE;
    }
  }

}
