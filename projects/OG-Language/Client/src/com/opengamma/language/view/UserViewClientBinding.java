/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.language.context.UserContext;

/**
 * Language binding specific data extension to {@link UserViewClient} instances.
 * 
 * @param <T> user specified data type
 */
public abstract class UserViewClientBinding<T extends UserViewClientData> {

  /**
   * Creates a new user data object.
   * 
   * @param userContext the user context
   * @param viewClient the view client
   * @param viewClientKey the client key that identifies this client
   * @return the new user data object, not null
   */
  protected abstract T create(UserContext userContext, ViewClient viewClient, ViewClientKey viewClientKey);

  /**
   * Fetch the data associated with the view client through this binding, creating a new data object if there is none.
   * 
   * @param viewClient view client object to fetch data for
   * @return the user data, not null
   */
  public final T get(final UserViewClient viewClient) {
    T data = viewClient.getData(getClass());
    if (data == null) {
      synchronized (this) {
        data = viewClient.getData(getClass());
        if (data != null) {
          return data;
        }
        data = create(viewClient.getUserContext(), viewClient.getViewClient(), viewClient.getViewClientKey());
        if (data == null) {
          throw new NullPointerException();
        }
        viewClient.setData(getClass(), data);
      }
    }
    return data;
  }
}
