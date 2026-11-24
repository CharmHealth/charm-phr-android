package com.charm.phr.mphr.model.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

/**
 * Created by deepaj-6611 on 10/11/22.
 */
interface BaseDao<T> {
    //
    /**
     * Insert an object in the database.
     *
     * /*@param obj the object to be inserted.*/
     */
//    @Insert
//    fun insert(obj: T)

    /**
     * Insert an array of objects in the database.
     *
     * /*@param obj the objects to be inserted.*/
     */
//    @Insert
//    fun insert(vararg obj: T)

    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     */
    @Insert
    fun insert(obj: T) : Long

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     */
    @Insert
    fun insert(vararg obj: T) : List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    fun update(obj: T)

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    fun delete(obj: T)

    /**
     * Delete an array of objects from the database.
     *
     * @param obj the objects to be deleted.
     */
    @Delete
    fun delete(vararg obj: T)
}