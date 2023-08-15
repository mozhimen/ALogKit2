package com.mozhimen.logk2

import androidx.lifecycle.ProcessLifecycleOwner
import com.mozhimen.basick.elemk.android.util.cons.CLogPriority
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.taskk.temps.TaskKPollInfinite
import com.mozhimen.basick.utilk.java.io.UtilKFile
import com.mozhimen.basick.utilk.java.io.UtilKFileFormat
import com.mozhimen.basick.utilk.java.io.file2str
import com.mozhimen.basick.utilk.java.io.getFileCreateTime
import com.mozhimen.basick.utilk.java.io.getFolderFiles
import com.mozhimen.basick.utilk.java.util.minute2millis
import com.mozhimen.basick.utilk.kotlin.UtilKStrPath
import com.mozhimen.logk2.commons.ILog2UploadListener
import com.mozhimen.logk2.db.LogK2Database
import com.mozhimen.logk2.mos.MLogK2
import com.mozhimen.uicorek.adaptk.systembar.cons.CProperty
import com.mozhimen.underlayk.crashk.CrashKMgr
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
    var crashLogPath: String? = null
        get() {
            if (field != null) return field
            val logFullPath = UtilKStrPath.Absolute.External.getCacheDir() + "/logk_printer_file_crash"
            UtilKFile.createFolder(logFullPath)
            return logFullPath.also { field = it }
        }

    private var _uploadListener: ILog2UploadListener? = null

    @Volatile
    private var _isRunning = false

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

    fun onCrashLog(crashLog: String) {
        saveCrashLog2File(crashLog)
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
        if (_isRunning) return
        _isRunning = true
        if (_uploadListener != null) {
            val dbLogs = LogK2Database.dao.selectMLogs()
            for (dbLog in dbLogs) {
                val uploadRes = _uploadListener!!.onUpload(dbLog.timeMillis, dbLog.priority, dbLog.tag, dbLog.msg)
                if (uploadRes)
                    LogK2Database.dao.deleteById(dbLog.id)
            }
            val fileLogs = crashLogPath!!.getFolderFiles()
            for (fileLog in fileLogs) {
                val str = fileLog.file2str()
                val uploadRes = _uploadListener!!.onUpload(fileLog.getFileCreateTime(), CLogPriority.E, TAG, str)
                if (uploadRes)
                    UtilKFile.deleteFile(fileLog)
            }
        }
        _isRunning = false
    }

    ///////////////////////////////////////////////////////////////////////////////

    private fun saveCrashLog2File(log: String) {
        val savePath = crashLogPath + "/${UtilKFile.getStrFileNameForStrNowDate()}.txt"
        UtilKFileFormat.str2file(log, savePath)
    }
}