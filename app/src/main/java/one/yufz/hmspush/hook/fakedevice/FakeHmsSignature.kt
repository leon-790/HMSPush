package one.yufz.hmspush.hook.fakedevice

import android.content.pm.PackageInfo
import android.util.Base64
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.common.HMS_CORE_SIGNATURE
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.xposed.*

object FakeHmsSignature {
    private const val TAG = "FakeHmsSignature"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            lpparam.classLoader.findClass("com.huawei.hms.utils.ReadApkFileUtil")
                .hookMethod("verifyApkHash", String::class.java) { replace { true } }
        } catch (e: Throwable) {
            //ignored
        }

        val classApplicationPackageManager = lpparam.classLoader.findClass("android.app.ApplicationPackageManager")
        classApplicationPackageManager.hookMethod("getPackageInfo", String::class.java, Int::class.java) {
            doAfter {
                val packageName = args[0] as String
                if (packageName == HMS_PACKAGE_NAME) {
                    val info = result as PackageInfo
                    info.signatures?.firstOrNull()?.let {
                        info.signatures[0]["mSignature"] = Base64.decode(HMS_CORE_SIGNATURE, Base64.NO_WRAP)
                    }
                }
            }
        }
    }
}