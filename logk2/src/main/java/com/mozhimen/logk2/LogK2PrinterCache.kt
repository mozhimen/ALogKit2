package com.mozhimen.logk2

import androidx.lifecycle.ProcessLifecycleOwner
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.taskk.temps.TaskKPollInfinite
import com.mozhimen.basick.utilk.java.util.hour2millis
import com.mozhimen.basick.utilk.java.util.minute2millis
import com.mozhimen.logk2.commons.ILog2UploadListener
import com.mozhimen.logk2.db.LogK2Database
import com.mozhimen.logk2.mos.MLogK2
import com.mozhimen.underlayk.logk.bases.BaseLogKConfig
import com.mozhimen.underlayk.logk.temps.printer.LogKPrinterFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * @ClassName LogK2PrinterFile
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/11 13:59
 * @Version 1.0
 */
@OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
class LogK2PrinterCache : LogKPrinterFile {
    private var _uploadListener: ILog2UploadListener? = null

    @OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
    private val _uploadTask: TaskKPollInfinite by lazy { TaskKPollInfinite().apply { bindLifecycle(ProcessLifecycleOwner.get()) } }

    /**
     * retentionMillis log文件的有效时长，单位ms，<=0表示一直有效
     */
    constructor(uploadListener: ILog2UploadListener, retentionMillis: Long) : super(retentionMillis) {
        _uploadListener = uploadListener
    }

    /**
     * retentionDay log文件的有效时长，单位天，<=0表示一直有效
     */
    constructor(uploadListener: ILog2UploadListener, retentionDay: Int) : super(retentionDay) {
        _uploadListener = uploadListener
    }

    init {
        _uploadTask.start(10L.minute2millis()) {
            withContext(Dispatchers.IO) {
                uploadCacheLog()
            }
        }
    }

    override fun print(config: BaseLogKConfig, priority: Int, tag: String, msg: String) {
        if (_uploadListener != null) {
            val currentTimeMillis = System.currentTimeMillis()
            val uploadRes = _uploadListener!!.onUpload(currentTimeMillis, priority, tag, msg)
            if (!uploadRes)
                LogK2Database.dao.addMLog(MLogK2(currentTimeMillis, priority, tag, msg))
        }
        super.print(config, priority, tag, msg)
    }

    private fun uploadCacheLog() {
        if (_uploadListener != null) {
            val logs = LogK2Database.dao.selectMLogs()
            for (log in logs) {
                val uploadRes = _uploadListener!!.onUpload(log.timeMillis, log.priority, log.tag, log.msg)
                if (uploadRes)
                    LogK2Database.dao.deleteById(log.id)
            }
        }
    }
}