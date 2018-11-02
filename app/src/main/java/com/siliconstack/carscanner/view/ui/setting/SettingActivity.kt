package com.siliconstack.carscanner.view.ui.setting

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.adapters.ViewGroupBindingAdapter.setListener
import android.os.Bundle
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.databinding.SettingActivityBinding
import com.siliconstack.carscanner.viewmodel.MainViewModel
import dagger.android.AndroidInjection
import org.jetbrains.anko.startActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.siliconstack.carscanner.model.FloorModel
import com.siliconstack.carscanner.model.LocationModel
import com.siliconstack.carscanner.model.OperatorModel
import com.siliconstack.carscanner.model.SettingDTO
import com.siliconstack.carscanner.view.ui.base.BaseActivity
import com.siliconstack.carscanner.view.ui.MainActivity
import com.siliconstack.carscanner.view.ui.MainActivityListener


class SettingActivity: BaseActivity() , MainActivityListener, SettingListener {

//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var settingActivityBinding: SettingActivityBinding
    lateinit var materialDialog: MaterialDialog
    lateinit var adapter: SettingAdapter
    val spinnerArr= arrayListOf("Location", "Floor", "Name")


   // override fun supportFragmentInjector() = dispatchingAndroidInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        initViewBinding()
        setListener()
        init()

    }

    private fun initViewBinding() {
        settingActivityBinding = DataBindingUtil.setContentView(this, R.layout.setting_activity)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
    }

    private fun init() {

        settingActivityBinding.spinner.setItems(spinnerArr)
        settingActivityBinding.spinner.setOnItemSelectedListener { view, position, id, item ->
            when(position){
                0 -> {
                    refreshListLocation()
                }
                1 -> {
                    refreshListFloor()
                }
                2 -> {
                    refreshListName()
                }
            }

        }

        settingActivityBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
            this@SettingActivity.adapter = SettingAdapter(this@SettingActivity)
            adapter = this@SettingActivity.adapter
            var divider = DividerItemDecoration(context, RecyclerView.VERTICAL)
            divider.setDrawable(resources.getDrawable(R.drawable.list_divider_transparent))
            addItemDecoration(divider)
            refreshListLocation()
        }

    }

    fun refreshListLocation(){
        val items= arrayListOf<SettingDTO>()
        mainViewModel.locationDAO.getAll().forEach {
            items.add(SettingDTO(it.name,it.id, SettingAdapter.SettingEnum.LOCATION.ordinal))
        }
        adapter.items= items
        adapter.notifyDataSetChanged()
    }


    fun refreshListFloor(){
        val items= arrayListOf<SettingDTO>()
        mainViewModel.floorDAO.getAll().forEach {
            items.add(SettingDTO(it.name,it.id, SettingAdapter.SettingEnum.FLOOR.ordinal))
        }
        adapter.items= items
        adapter.notifyDataSetChanged()
    }

    fun refreshListName(){
        val items= arrayListOf<SettingDTO>()
        mainViewModel.nameDAO.getAll().forEach {
            items.add(SettingDTO(it.name,it.id, SettingAdapter.SettingEnum.NAME.ordinal))
        }
        adapter.items= items
        adapter.notifyDataSetChanged()
    }

    fun createNewDialog(){
        materialDialog=MaterialDialog.Builder(this)
                .content("Enter value")
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .contentGravity(GravityEnum.CENTER)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("","", MaterialDialog.InputCallback { dialog, input ->
                }).positiveText("Save").negativeText("Cancel").onPositive { dialog, which ->
                    if(!dialog.inputEditText?.text.toString().trim().isEmpty()) {
                        when (settingActivityBinding.spinner.selectedIndex) {
                            0 -> {
                                mainViewModel.locationDAO.addRow(LocationModel(dialog.inputEditText?.text.toString(), 0))
                                refreshListLocation()

                            }
                            1 -> {
                                mainViewModel.floorDAO.addRow(FloorModel(dialog.inputEditText?.text.toString(), 0))
                                refreshListFloor()
                            }
                            2 -> {
                                mainViewModel.nameDAO.addRow(OperatorModel(dialog.inputEditText?.text.toString(), 0))
                                refreshListName()
                            }
                        }
                    }
                }.onNegative { dialog, which ->
                    dialog.dismiss()
                }
                .build()
    }
    private fun setListener() {
        settingActivityBinding.btnAdd.setOnClickListener { view->
            createNewDialog()
            materialDialog.show()
        }
    }


    override fun onBackPressed() {
        finish()
        startActivity<MainActivity>()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onRemove(settingDTO: SettingDTO) {
        when(settingDTO.settingEnum){
            SettingAdapter.SettingEnum.LOCATION.ordinal -> {
                mainViewModel.locationDAO.deleteById(settingDTO.id)
                refreshListLocation()
            }
            SettingAdapter.SettingEnum.FLOOR.ordinal -> {
                mainViewModel.floorDAO.deleteById(settingDTO.id)
                refreshListFloor()
            }
            SettingAdapter.SettingEnum.NAME.ordinal -> {
                mainViewModel.nameDAO.deleteById(settingDTO.id)
                refreshListName()
            }
        }
    }


}

interface SettingListener{
    fun onRemove(settingDTO: SettingDTO)
}