package com.mozhimen.logk2.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mozhimen.logk2.mos.MLogK2


/**
 * @ClassName ILogK2Dao
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/11 14:51
 * @Version 1.0
 */
@Dao
interface ILogK2Dao {
    @Query("select * from m_logk2")
    fun selectMLogs(): List<MLogK2>

    @Query("delete from m_logk2 where id = :id")
    fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMLog(log: MLogK2)
}