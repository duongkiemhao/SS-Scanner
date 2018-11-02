package com.siliconstack.carscanner.view.ui.base
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.PreferenceSetting
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.view.helper.DialogHelper
import com.siliconstack.carscanner.view.ui.MainActivity
import com.siliconstack.carscanner.viewmodel.MainViewModel
import io.fabric.sdk.android.Fabric
import javax.inject.Inject


open class BaseActivity : AppCompatActivity(){



    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var mainViewModel: MainViewModel
    lateinit var progressDialog: MaterialDialog

    var isLoginActivity=false
    var isIdleTimeOut=false
    var isResume=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)

        progressDialog = DialogHelper.materialProgressDialog(this)



    }

    open fun setTranslucentToolbar(){
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color_1));

    }
    open fun setTranslucentBarNoScrollView(){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()

    }

    fun showIdleTimeOutDialog(){
        DialogHelper.materialDialog("Auto Logoff has occurred due to inactivity","Close", MaterialDialog.SingleButtonCallback { dialog, which ->
            dialog.dismiss()
            backToLogin()
        },this@BaseActivity).show()
    }


    override fun onResume() {
        super.onResume()
        isResume=true
        if(isIdleTimeOut)
            showIdleTimeOutDialog()

    }

    override fun onStop() {
        super.onStop()
        isResume=false

    }


    fun backToLogin(){
        PreferenceSetting.UserSetting!!.password=""
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }



}



