package com.mozhimen.logk2.mos

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mozhimen.underlayk.logk.bases.BaseLogKRecord
import java.io.Serializable

/**
 * @ClassName MLogK2
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/11 14:45
 * @Version 1.0
 */
@Entity(tableName = "m_logk2")
class MLogK2(
    timeMillis: Long,
    priority: Int,
    tag: String,
    msg: String
) : BaseLogKRecord(timeMillis, priority, tag, msg), Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}