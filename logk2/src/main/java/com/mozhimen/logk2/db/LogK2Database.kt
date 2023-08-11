package com.mozhimen.logk2.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mozhimen.basick.utilk.android.app.UtilKApplication
import com.mozhimen.logk2.mos.MLogK2


/**
 * @ClassName LogK2Database
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/11 14:44
 * @Version 1.0
 */
@Database(entities = [MLogK2::class], version = 1, exportSchema = false)
abstract class LogK2Database : RoomDatabase() {
    abstract val logk2Dao: ILogK2Dao

    companion object {
        @Volatile
        private var _db: LogK2Database =
            Room.databaseBuilder(UtilKApplication.instance.applicationContext, LogK2Database::class.java, "logk2_db").allowMainThreadQueries().build()

        val db: LogK2Database
            get() = _db

        val dao: ILogK2Dao =
            _db.logk2Dao
    }
}