/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.util.PublicSPI;

/**
 * Utilities around the configuration master.
 */
@PublicSPI
public final class ConfigMasterUtils {

  /**
   * Stores the item in the database ensuring a unique name.
   * <p>
   * This will read the current item with the specified name and
   * either add or update as necessary. Since the read and modify are two
   * separate steps, there is a race condition, thus this method is intended
   * for sensible setup purposes rather than ensuring uniqueness.
   * 
   * @param <T>  the configuration element type
   * @param master  the config master, not null
   * @param item  the item to store, not null
   * @return the updated result, not null
   */
  public static <T> ConfigItem<T> storeByName(final ConfigMaster master, final ConfigItem<T> item) {

    final int maxRetries = 10; //IGN-101 This is so high because the tests hammer this function with the same name
    int retries = 0;

    if (item.getUniqueId() == null) {
      while (true) {
        try {
          return storeByNameInner(master, item);
        } catch (IllegalArgumentException ex) {
          if (++retries == maxRetries) {
            throw ex;
          }
        } catch (IncorrectUpdateSemanticsDataAccessException ex) {
          if (++retries == maxRetries) {
            throw ex;
          }
        }

        item.setUniqueId(null);
      }
    } else {
      return storeByNameInner(master, item);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> ConfigItem<T> storeByNameInner(final ConfigMaster master, final ConfigItem<T> item) {
    ConfigSearchRequest<T> searchRequest = new ConfigSearchRequest<T>();
    searchRequest.setType(item.getType());
    searchRequest.setName(item.getName());
    ConfigSearchResult<T> searchResult = master.search(searchRequest);
    for (ConfigItem<T> existingItem : searchResult.getValues()) {
      if (existingItem.getValue().equals(item.getValue())) {
        return existingItem;
      }
    }
    ConfigItem<T> firstExistingItem = searchResult.getFirstValue();
    if (firstExistingItem == null) {
      return (ConfigItem<T>) master.add(new ConfigDocument(item)).getConfig();
    } else {
      if (item.getUniqueId() == null) {
        item.setUniqueId(firstExistingItem.getUniqueId());
      }
      return (ConfigItem<T>) master.update(new ConfigDocument(item)).getConfig();
    }
  }

}
