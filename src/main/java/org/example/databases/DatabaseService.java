package org.example.databases;

import java.util.List;

public interface DatabaseService<T, G, C> {
    /**
     * Get Database object by id
     * @param id generic id
     * @return object
     */
    C get(T id);

    /**
     * Add object to the Database
     * @param obj Database object
     */
    void post(G obj);

    /**
     * List by filter data
     * @param filterData filter data
     * @return list of DB objects
     */
    List<T> list(T filterData);

    /**
     * Update a DB object with the given data
     * @param id id for DB object
     * @param data generic data to update object with
     */
    void patch(T id, G data);
}
