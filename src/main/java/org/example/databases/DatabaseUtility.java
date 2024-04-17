package org.example.databases;

import java.util.List;

public interface DatabaseUtility<T, G> {

  /**
   * Get DB entry
   *
   * @param id entry id
   * @return db entry
   */
  T get(String id);

  /**
   * Create a DB entry
   *
   * @param item item request data
   */
  void post(T item);

  /**
   * List entries by filter
   *
   * @return list of entries
   */
  List<T> list(G filter);

  /**
   * Update a DB entry
   *
   * @param id id for item to be updated
   * @param filter filter used for updating the item
   */
  void patch(String id, G filter);

  /**
   * Delete DB entry
   *
   * @param id id
   */
  void delete(String id);
}
