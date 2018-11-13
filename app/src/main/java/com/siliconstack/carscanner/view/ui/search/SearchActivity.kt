package com.siliconstack.carscanner.view.ui.search

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.adapters.ViewGroupBindingAdapter.setListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import com.afollestad.materialdialogs.MaterialDialog
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.config.Config
import com.siliconstack.carscanner.config.Constant
import com.siliconstack.carscanner.databinding.SearchActivityBinding
import com.siliconstack.carscanner.model.MainDTO
import com.siliconstack.carscanner.model.SearchDTO
import com.siliconstack.carscanner.view.control.CSVWriter
import com.siliconstack.carscanner.view.helper.DialogHelper
import com.siliconstack.carscanner.view.ui.base.BaseActivity
import com.siliconstack.carscanner.view.utility.DateUtility
import com.siliconstack.carscanner.view.utility.Utility
import com.siliconstack.carscanner.viewmodel.MainViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.collections.forEachReversedWithIndex
import org.jetbrains.anko.startActivity
import org.joda.time.Days
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SearchActivity : BaseActivity(), SearchListener, HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var searchActivityBinding: SearchActivityBinding
    var isListView=true

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        initViewBinding()
        init()
        setListener()
    }

    private fun initViewBinding() {
        searchActivityBinding = DataBindingUtil.setContentView(this, R.layout.search_activity)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
    }

    fun setListener(){
        searchActivityBinding.btnBack.setOnClickListener {
            onBackPressed()
        }
        searchActivityBinding.btnSwitch.setOnClickListener {
            isListView=!isListView
            if(isListView) {
                supportFragmentManager.beginTransaction().replace(R.id.content, ListViewFragment.newInstance()).commit()
                searchActivityBinding.btnSwitch.text="MapView"
            }
            else{
                supportFragmentManager.beginTransaction().replace(R.id.content,MapViewFragment.newInstance()).commit()
                searchActivityBinding.btnSwitch.text="ListView"
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    fun init(){
        setTranslucentBarNoScrollView()
        supportFragmentManager.beginTransaction().replace(R.id.content,ListViewFragment.newInstance()).commit()
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector


}

interface SearchListener {

}
