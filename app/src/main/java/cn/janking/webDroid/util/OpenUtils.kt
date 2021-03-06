package cn.janking.webDroid.util

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.PathConstants
import cn.janking.webDroid.helper.PermissionHelper
import com.bumptech.glide.Glide
import java.io.File


/**
 * @author Janking
 * 打开分享页面等的工具方法
 */
object OpenUtils {
    /**
     * 分享 内容
     */
    fun shareMessage(message: String?) {
        message?.let {
            ActivityUtils.startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it)
                        type = "text/plain"
                    },
                    Utils.getString(R.string.msg_share_title)
                )
            )
        }
    }

    /**
     * 使用浏览器打开 url
     */
    fun openUrl(url: String?) {
        url?.let {
            ActivityUtils.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(it)
            })
        }
    }

    /**
     * 分享图片到其他应用
     */
    fun shareImage(imageUrl: String?) {
        //下载完成之后再分享
        ThreadUtils.executeByCached(object :
            ThreadUtils.SimpleTask<File>() {
            override fun doInBackground(): File {
                return Glide.with(ActivityUtils.getTopActivity())
                    .asFile()
                    .load(imageUrl)
                    .submit().get()
            }

            override fun onSuccess(result: File?) {
                result?.run {
                    OpenUtils.shareImage(UriUtils.file2Uri(this))
                }
            }
        })
    }

    /**
     * 分享图片到其他应用
     */
    fun shareImage(imageUri: Uri?) {
        imageUri?.let {
            ActivityUtils.startActivity(Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, it)
                type = "image/*"
            })
        }
    }

    /**
     * 复制链接到剪切板中
     */
    fun copyUrl(url: String?) {
        url.let {
            //获取剪贴板管理器：
            val cm: ClipboardManager? =
                Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData: ClipData = ClipData.newPlainText("Label", url)
            // 将ClipData内容放到系统剪贴板里。
            cm?.run {
                primaryClip = mClipData
                Toast.makeText(Utils.getApp(), "已复制到剪切板", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * @param imageUrl 图片url
     * 弹出全屏窗口显示图片
     */
    fun showFullImageDialogWithUrl(imageUrl: String?): Boolean {
        return imageUrl?.let {
            PermissionHelper.checkStorage(Runnable {
                Dialog(ActivityUtils.getTopActivity(), R.style.DialogFullscreen).run {
                    setContentView(R.layout.layout_image_dialog_fullscreen)
                    val imageView: ImageView = findViewById(R.id.img_full_screen_dialog)
                    //使用Glide加载图片
                    Glide.with(ActivityUtils.getTopActivity()).load(imageUrl).into(imageView)
                    val toolBar: Toolbar = findViewById(R.id.toolbar_full_screen_dialog)
                    toolBar.setNavigationOnClickListener { dismiss() }
                    show()
                }
            })
            true
        } ?: false
    }

    /**
     * @param imageFile 图片文件
     * 弹出全屏显示图片的对话框
     */
    fun showFullImageDialogWithFile(imageFile: String?): Boolean {
        return imageFile?.let {
            if (FileUtils.isFileExists(imageFile)) {
                PermissionHelper.checkStorage(Runnable {
                    Dialog(ActivityUtils.getTopActivity(), R.style.DialogFullscreen).run {
                        setContentView(R.layout.layout_image_dialog_fullscreen)
                        val imageView: ImageView = findViewById(R.id.img_full_screen_dialog)
                        //使用Glide加载图片
                        Glide.with(ActivityUtils.getTopActivity()).load(imageFile).into(imageView)
                        val toolBar: Toolbar = findViewById(R.id.toolbar_full_screen_dialog)
                        toolBar.setNavigationOnClickListener { dismiss() }
                        show()
                    }
                })
                true
            } else {
                false
            }
        } ?: false
    }

    /**
     * 弹出全屏显示文字
     */
    fun showFullTextDialog(text: String): Boolean {
        return if (text.isNotBlank()) {
            Dialog(ActivityUtils.getTopActivity(), R.style.DialogFullscreen).run {
                setContentView(R.layout.layout_text_dialog_fullscreen)
                val textView: TextView = findViewById(R.id.text_full_screen_dialog)
                val toolBar: Toolbar = findViewById(R.id.toolbar_full_screen_dialog)
                toolBar.setNavigationOnClickListener { dismiss() }
                textView.text = text
                show()
            }
            true
        } else {
            false
        }
    }

    /**
     * 保存网络图片
     */
    fun saveImage(imageUrl: String?) {
        PermissionHelper.checkStorage(Runnable {
            ThreadUtils.executeByCached(object :
                ThreadUtils.SimpleTask<File>() {
                //保存图片格式
                var imageFormat = "jpeg"

                override fun doInBackground(): File {
                    imageFormat = FileUtils.getFileExtension(imageUrl)?.let {
                        if (it.isNotBlank()) it
                        else imageFormat
                    }
                    return Glide.with(ActivityUtils.getTopActivity())
                        .asFile()
                        .load(imageUrl)
                        .submit().get()
                }

                override fun onSuccess(result: File?) {
                    result?.run {
                        ThreadUtils.executeByCached(object :
                            ThreadUtils.SimpleTask<Unit>() {
                            override fun doInBackground() {
                                FileUtils.copyFileToDir(
                                    result,
                                    PathConstants.dirSaveImage,
                                    imageFormat
                                )
                            }

                            override fun onFail(t: Throwable?) {
                                Toast.makeText(Utils.getApp(), "保存失败", Toast.LENGTH_SHORT)
                                    .show()
                                LogUtils.w(t)
                            }

                            override fun onSuccess(unit: Unit) {
                                Toast.makeText(
                                    Utils.getApp(),
                                    "已保存到${PathConstants.dirSaveImage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }
            })
        })
    }

    /**
     * 从uri获取要跳转的ResolveInfo，从而得知需要跳转的应用
     */
    fun getResolveInfoFromUri(uri: Uri): ResolveInfo? {
        val packageManager =
            ActivityUtils.getTopActivity().packageManager
        val intent: Intent = Intent().apply {
            data = uri
            flags = Intent.URI_INTENT_SCHEME
        }
        return packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
    }

    /**
     * 跳转到选择文件的窗口
     */
    fun toSelectFile(typeString: String, requestCode: Int) {
        /**
         * 需要保证存储权限
         */
        PermissionHelper.checkStorage(Runnable {
            Intent(Intent.ACTION_GET_CONTENT).run {
                type = typeString
                addCategory(Intent.CATEGORY_OPENABLE)
                ActivityUtils.startActivityForResult(
                    ActivityUtils.getTopActivityExceptTrans(),
                    this,
                    requestCode
                )
            }
        })
    }

    private fun safeCast(string: String?, function: (arg1: String) -> Unit) {
        string?.let(function)
    }
}