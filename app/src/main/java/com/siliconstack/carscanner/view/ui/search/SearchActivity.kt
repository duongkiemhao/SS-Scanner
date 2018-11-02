package com.siliconstack.carscanner.view.ui.search

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
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
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.startActivity
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : BaseActivity(), SearchListener {

    lateinit var searchActivityBinding: SearchActivityBinding

    lateinit var adapter: SearchAdapter
    var isDesc=true
    val rxPermissions by lazy {
        RxPermissions(this)
    }

    var offset=0
    var isLoading=false
    var isDateSorting=true


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        initViewBinding()
        setListener()
        init()

    }

    private fun initViewBinding() {
        searchActivityBinding = DataBindingUtil.setContentView(this, R.layout.search_activity)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
    }

    private fun init() {
        mainViewModel.initItems()
        bindAdapter()
        setTranslucentBarNoScrollView()
        searchActivityBinding.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
        searchActivityBinding.recyclerView.postDelayed({
            searchActivityBinding.recyclerView.addOnScrollListener(mScrollListener)
        }
                ,2000)
        if(mainViewModel.items.count()<Config.LIMIT)
            isLoading=true

    }

    private fun setListener() {
        searchActivityBinding.ediKeyword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //adapter.keyword=s.toString()
                mainViewModel.keyword=s.toString()
                offset=0
                mainViewModel.items= mainViewModel.filterListSearch(isDesc,offset,if(isDateSorting) "a.timestamp" else "locationName") as ArrayList<MainDTO>
                bindAdapter()

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        searchActivityBinding.btnExport.setOnClickListener {
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { it: Boolean? ->
                        if (it!!) {
                            export()
                        }
                    }
        }
        searchActivityBinding.btnBack.setOnClickListener {
            onBackPressed()
        }

        searchActivityBinding.txtDate.setOnClickListener {
            isDateSorting=true
            isDesc=!isDesc
            searchActivityBinding.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
            searchActivityBinding.txtDate.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,if(isDesc) R.drawable.ic_down else R.drawable.ic_up,0)
            offset=0
            isLoading=false
            mainViewModel.items= mainViewModel.filterListSearch(isDesc,offset,if(isDateSorting) "a.timestamp" else "locationName") as ArrayList<MainDTO>
            bindAdapter()
            searchActivityBinding.recyclerView.postDelayed({
                searchActivityBinding.recyclerView.scrollToPosition(0)
            },300)


        }
        searchActivityBinding.txtLocation.setOnClickListener {
            isDateSorting=false
            isDesc=!isDesc
            searchActivityBinding.txtDate.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
            searchActivityBinding.txtLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,if(isDesc) R.drawable.ic_down else R.drawable.ic_up,0)
            offset=0
            isLoading=false

            mainViewModel.items= mainViewModel.filterListSearch(isDesc,offset,if(isDateSorting) "a.timestamp" else "locationName") as ArrayList<MainDTO>
            bindAdapter()
            searchActivityBinding.recyclerView.postDelayed({
                searchActivityBinding.recyclerView.scrollToPosition(0)
            },300)


        }
    }

    var mScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            if (isLoading)
                return
            val visibleItemCount = searchActivityBinding.recyclerView.layoutManager.getChildCount()
            val totalItemCount = searchActivityBinding.recyclerView.layoutManager.getItemCount()
            val pastVisibleItems = (searchActivityBinding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()


            if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    searchActivityBinding.recyclerView.removeOnScrollListener(this)

                    Handler().postDelayed({
                        offset+=Config.LIMIT
                        val items=mainViewModel.filterListSearch(isDesc,offset,if(isDateSorting) "a.timestamp" else "locationName")
                        mainViewModel.items.addAll(items)

                        adapter = SearchAdapter(this@SearchActivity, getListExpandGroup())
                        searchActivityBinding.recyclerView.adapter=adapter
                        isLoading=items.count()<Config.LIMIT

                        Handler().postDelayed({
                            searchActivityBinding.recyclerView.addOnScrollListener(this)
                        },1000)


                    },1000)

                    isLoading = true


            }
        }
    }

    fun getListExpandGroup():List<SearchDTO>{
        var items= ArrayList<SearchDTO>()
        var title:String=""
        var listMainDTO:ArrayList<MainDTO> = arrayListOf()
        mainViewModel.items.forEachIndexed { index, model ->
            if(index==0) {
                if(isDateSorting)
                    title=DateUtility.parseDateToDateTimeStr(Constant.UI_DATE_FORMAT, Date(model.timestamp?:0))?:""
                else title=model.locationName?:""
                listMainDTO.add(model)

            }
            else{
                if(isDateSorting) {
                    if (Utility.compare2DatePart(Date(model.timestamp
                                    ?: 0), Date(mainViewModel.items.get(index - 1).timestamp
                                    ?: 0)) == 0) {
                        listMainDTO.add(model)
                    } else {
                        items.add(SearchDTO(title, listMainDTO))
                        title = DateUtility.parseDateToDateTimeStr(Constant.UI_DATE_FORMAT, Date(model.timestamp
                                    ?: 0)) ?: ""
                        listMainDTO = arrayListOf()
                        listMainDTO.add(model)
                    }
                }
                else{
                    if (model.locationName==mainViewModel.items.get(index - 1).locationName) {
                        listMainDTO.add(model)
                    } else {
                        items.add(SearchDTO(title, listMainDTO))
                        title=model.locationName?:""
                        listMainDTO = arrayListOf()
                        listMainDTO.add(model)
                    }
                }
            }
            if(index==mainViewModel.items.count()-1){
                items.add(SearchDTO(title,listMainDTO))
            }
        }

        return items

    }

    fun export() {
        val exportDir = File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        val filePath = exportDir.absolutePath + "/" + resources.getString(R.string.app_name) + ".csv"
        val file = File(filePath)
        try {
            file.createNewFile();
            val csvWrite = CSVWriter(FileWriter(file));
            val columnName = arrayOf("Value","Location","Floor","Bay","Name","Date")
            csvWrite.writeNext(columnName);
            var isFound=false
            adapter.groups?.forEach {
                it.items.forEach {
                    val item=it as MainDTO
                    if(item.isSelected) {
                        isFound=true
                        val arrStr = arrayOf(item.scanText, item.locationName,item.floorName,item.bayNumber,item.operatorName,item.dateString)
                        csvWrite.writeNext(arrStr);
                    }
                }

            }
            csvWrite.close();

            if (!isFound) {
                Toasty.info(this, "No selected records to export").show()
            } else {
                DialogHelper.materialDialog("Exported to " + filePath, "Close", "Mail",
                        MaterialDialog.SingleButtonCallback { dialog, which ->
                            dialog.dismiss()
                        }, MaterialDialog.SingleButtonCallback { dialog, which ->
                            //openFile(file)
                            sendMail(file)
                            dialog.dismiss()
                }, this@SearchActivity).show()
            }
        } catch (exp: java.lang.Exception) {
            Toasty.error(this, exp.message ?: "").show()
        }
    }

    fun sendMail(file: File){
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("email@example.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        if (!file.exists() || !file.canRead()) {
            return
        }
        val uri = Uri.fromFile(file)
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
    }

    fun openFile(file: File) {
        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        val data = Uri.fromFile(file);
        val type = "*/*";
        intent.setDataAndType(data, type);
        startActivity(intent);
    }

    override fun onBackPressed() {
        finish()
    }

    override fun deleteItem(mainDTO: MainDTO) {
        mainViewModel.mainDAO.deleteById(mainDTO.id?:0)
        var removeItem:MainDTO?=null
        mainViewModel.items.forEach {
            if(it.id==mainDTO.id)
                removeItem=it
        }
        mainViewModel.items.remove(removeItem)
        bindAdapter()
    }

    override fun deleteGroup(items: List<MainDTO>) {
        DialogHelper.materialDialog("Are you sure to delete all items in group?","Yes","No",
                MaterialDialog.SingleButtonCallback { dialog, which ->
                    val arrIds:IntArray=IntArray(items.count())
                    items.forEachIndexed { index, mainDTO ->
                        arrIds.set(index,mainDTO.id?:0)
                    }
                    mainViewModel.mainDAO.deleteByIds(arrIds)
                    mainViewModel.items.removeAll(items)
                    bindAdapter()
                }, MaterialDialog.SingleButtonCallback { dialog, which ->
                    dialog.dismiss()
        },this).show()

    }

    fun bindAdapter(){
        searchActivityBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)

            this@SearchActivity.adapter = SearchAdapter(this@SearchActivity, getListExpandGroup())
            adapter = this@SearchActivity.adapter
            var divider = DividerItemDecoration(context, RecyclerView.VERTICAL)
            divider.setDrawable(resources.getDrawable(R.drawable.list_divider_transparent))
            addItemDecoration(divider)

        }

    }

    override fun onItemClick(mainDTO: MainDTO) {
        startActivity<VehicleActivity>("object" to mainDTO)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

}

interface SearchListener {
    fun deleteItem(mainDTO: MainDTO)
    fun deleteGroup(items: List<MainDTO>)
    fun onItemClick(mainDTO: MainDTO)
}
