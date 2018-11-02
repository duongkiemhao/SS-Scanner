package com.siliconstack.carscanner.view.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.databinding.MainActivityBinding
import com.siliconstack.carscanner.view.ui.base.BaseActivity
import com.siliconstack.carscanner.view.ui.scan.ScanResultActivity
import com.siliconstack.carscanner.view.ui.search.SearchActivity
import com.siliconstack.carscanner.view.ui.setting.SettingActivity
import com.siliconstack.carscanner.view.utility.Utility
import com.siliconstack.carscanner.viewmodel.MainViewModel
import dagger.android.AndroidInjection
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.startActivity


class MainActivity : BaseActivity() , MainActivityListener {

//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var mainActivityBinding: MainActivityBinding

    var doubleBackToExitPressedOnce: Boolean = false



//    override fun supportFragmentInjector() = dispatchingAndroidInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        initViewBinding()
        setListener()
        init()

    }

    private fun initViewBinding() {
        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
    }

    private fun init() {
        isLoginActivity = true

        setSupportActionBar(mainActivityBinding.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        mainActivityBinding.txtAppVersion.text="App Version v"+Utility.getAppVersionName()
    }

    private fun setListener() {
        mainActivityBinding.btnScanVin.setOnClickListener {
            startActivity<ScanResultActivity>("scanEnum" to ScanResultActivity.SCAN_ENUM.VIN.ordinal)
        }
        mainActivityBinding.btnScanRego.setOnClickListener {
            startActivity<ScanResultActivity>("scanEnum" to ScanResultActivity.SCAN_ENUM.REGO.ordinal)
        }
        mainActivityBinding.btnScanQrcode.setOnClickListener {
            startActivity<ScanResultActivity>("scanEnum" to ScanResultActivity.SCAN_ENUM.QRCODE.ordinal)
        }

        mainActivityBinding.btnScanBarcode.setOnClickListener {
            startActivity<ScanResultActivity>("scanEnum" to ScanResultActivity.SCAN_ENUM.BARCODE.ordinal)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()){
            R.id.btn_view -> {
                startActivity<SearchActivity>()
                return true
            }
            R.id.btn_setting -> {
                startActivity<SettingActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
                finish()
            }
            doubleBackToExitPressedOnce = true
            Toasty.info(this, getString(R.string.msg_exit)).show()
            AppApplication.handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()

    }



}

interface MainActivityListener{

}