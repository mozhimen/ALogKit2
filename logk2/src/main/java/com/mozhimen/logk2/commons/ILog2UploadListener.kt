package com.mozhimen.logk2.commons


/**
 * @ClassName IUploadListener
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/11 14:33
 * @Version 1.0
 */
interface ILog2UploadListener {
    fun onUpload(timeMillis: Long, priority: Int, tag: String, msg: String): Boolean
}