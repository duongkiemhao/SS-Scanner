package com.siliconstack.carscanner.view.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.orhanobut.logger.Logger
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.PreferenceSetting
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.config.Config
import com.siliconstack.carscanner.config.Config.Companion.MAP_ZOOM
import com.siliconstack.carscanner.config.Constant
import com.siliconstack.carscanner.databinding.ScanResultFragmentBinding
import com.siliconstack.carscanner.model.*
import com.siliconstack.carscanner.view.adapter.FilterListAdapter
import com.siliconstack.carscanner.view.eventbus.MainEventBus
import com.siliconstack.carscanner.view.helper.DialogHelper
import com.siliconstack.carscanner.view.ui.base.BaseActivity
import com.siliconstack.carscanner.view.utility.DateUtility
import com.siliconstack.carscanner.view.utility.Utility
import com.siliconstack.carscanner.viewmodel.MainViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import com.theartofdev.edmodo.cropper.CropImage
import dagger.android.AndroidInjection
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.uiThread
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class ScanResultActivity : BaseActivity(){

    lateinit var scanResultFragmentBinding: ScanResultFragmentBinding

    //lateinit var progressDialog: MaterialDialog
    val REQUEST_QRCODE=101
    val REQUEST_BARCODE=100
    val REQUEST_CHOOSE_FROM_GALLERY=102
    lateinit var rxPermissions: RxPermissions
    var result:String?=null
    var scanEnum:Int = 0
    //spinner
     var listLocation:ArrayList<FilterDialogModel> = arrayListOf()
    var listFloor:ArrayList<FilterDialogModel> = arrayListOf()
    var listName:ArrayList<FilterDialogModel> = arrayListOf()
     var locationModel:FilterDialogModel?=null
    var floorModel:FilterDialogModel?=null
    var nameModel:FilterDialogModel?=null

    //google map
    lateinit var googleMap: GoogleMap

    lateinit var marker:MarkerOptions


    //scan
    enum class SCAN_ENUM{
        VIN, REGO,BARCODE,QRCODE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        initViewBinding()
        setTranslucentToolbar()

        EventBus.getDefault().register(this)
        scanEnum=intent.getIntExtra("scanEnum",0)
        setListener()
        initInfo()

        //showPickChooseIntent()

    }

    private fun initViewBinding() {
        scanResultFragmentBinding = DataBindingUtil.setContentView(this, R.layout.scan_result_fragment)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
    }


    @SuppressLint("CheckResult")
    fun openCameraActivity(){
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe { it: Boolean? ->
                    if (it!!) {
                        when(scanEnum){
                            SCAN_ENUM.VIN.ordinal ->
                                startActivity<CameraActivity>()
                            SCAN_ENUM.REGO.ordinal ->
                                startActivity<CameraActivity>()
                            SCAN_ENUM.BARCODE.ordinal -> {
                                val intent = Intent(this, ZXingScannerActivity::class.java)
                                startActivityForResult(intent, REQUEST_BARCODE)
                            }
                            else -> {
                                val intent = Intent(this, ZXingScannerActivity::class.java)
                                startActivityForResult(intent, REQUEST_QRCODE)
                            }

                        }
                    }
                }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_QRCODE ->{
                when(resultCode){
                    Activity.RESULT_OK ->{
                        val result=data!!.getStringExtra("result")
                        scanResultFragmentBinding.ediScanResult.setText(result)
                    }
                }
            }
            REQUEST_BARCODE->{
                when(resultCode){
                    Activity.RESULT_OK ->{
                        val result=data!!.getStringExtra("result")
                        scanResultFragmentBinding.ediScanResult.setText(result)
                    }
                }
            }
            REQUEST_CHOOSE_FROM_GALLERY ->{
                when(resultCode){
                    Activity.RESULT_OK->{
                        val uri=data?.data
                        CropImage.activity(uri)
                                .start(this);
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        val resultUri = result.uri
                        val resizedBitmap = Utility.scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri),840)
                        scanResultFragmentBinding.imageView.setImageBitmap(resizedBitmap)
                        loadCloudScan(resizedBitmap)

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                    }
            }
        }
    }

    fun setListener() {
        scanResultFragmentBinding.btnCancel.setOnClickListener {
            finish()
        }
        scanResultFragmentBinding.btnConfirm.setOnClickListener {
            if(scanResultFragmentBinding.ediScanResult.text.isNullOrBlank())
                return@setOnClickListener
            val scanItem=mainViewModel.isScanTextExist(scanResultFragmentBinding.ediScanResult.text.toString().trim())
            if(scanItem!=null){
                val view=LayoutInflater.from(this).inflate(R.layout.view_vehicle_found,null)
                view.findViewById<TextView>(R.id.txt_value).text=scanItem.scanText
                view.findViewById<TextView>(R.id.txt_location).text=scanItem.locationName
                view.findViewById<TextView>(R.id.txt_floor).text=scanItem.floorName
                view.findViewById<TextView>(R.id.txt_bay).text=scanItem.bayNumber
                view.findViewById<TextView>(R.id.txt_operator).text=scanItem.operatorName
                view.findViewById<TextView>(R.id.txt_timestamp).text=DateUtility.parseDateToDateTimeStr(Constant.COMBINE_DATE_TIME_FORMAT,Date(scanItem.timestamp?:0))
                DialogHelper.materialCustomViewDialog("Matching Vehicle Found!",view,"Ok","Cancel", MaterialDialog.SingleButtonCallback { dialog, which ->
                    insertToDB()
                    dialog.dismiss()
                }, MaterialDialog.SingleButtonCallback { dialog, which ->
                    dialog.dismiss()
                },this@ScanResultActivity).show()

            }
            else {
                insertToDB()
            }
        }
        scanResultFragmentBinding.btnCamera.setOnClickListener {
            openCameraActivity()
        }
        scanResultFragmentBinding.btnGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, REQUEST_CHOOSE_FROM_GALLERY)
        }

        scanResultFragmentBinding.txtLocation.setOnClickListener {
            DialogHelper.materialProgressDialog("", this, FilterListAdapter(listLocation),
                    MaterialDialog.SingleButtonCallback { dialog, which ->
                        listLocation= (dialog.recyclerView.adapter as FilterListAdapter).items
                        listLocation.forEach {
                            if (it.isSelect) {
                                locationModel=it
                                scanResultFragmentBinding.txtLocation.text=locationModel?.name
                                PreferenceSetting.locationSetting=it
                                return@SingleButtonCallback
                            }
                        }
                        dialog.dismiss()
                    }).show()
        }
        scanResultFragmentBinding.txtFloor.setOnClickListener {
            DialogHelper.materialProgressDialog("", this, FilterListAdapter(listFloor),
                    MaterialDialog.SingleButtonCallback { dialog, which ->
                        listFloor= (dialog.recyclerView.adapter as FilterListAdapter).items
                        listFloor.forEach {
                            if (it.isSelect) {
                                floorModel=it
                                scanResultFragmentBinding.txtFloor.text=floorModel?.name
                                PreferenceSetting.floorSetting=it
                                return@SingleButtonCallback
                            }
                        }
                        dialog.dismiss()
                    }).show()
        }
        scanResultFragmentBinding.txtOperator.setOnClickListener {
            DialogHelper.materialProgressDialog("", this, FilterListAdapter(listName),
                    MaterialDialog.SingleButtonCallback { dialog, which ->
                        listName= (dialog.recyclerView.adapter as FilterListAdapter).items
                        listName.forEach {
                            if (it.isSelect) {
                                nameModel=it
                                scanResultFragmentBinding.txtOperator.text=nameModel?.name
                                PreferenceSetting.nameSetting=it
                                return@SingleButtonCallback
                            }
                        }
                        dialog.dismiss()
                    }).show()
        }

    }

    fun insertToDB(){
        val locationId = locationModel?.code?.toIntOrNull()?:0
        val floorId =  floorModel?.code?.toIntOrNull()?:0
        val nameId =  nameModel?.code?.toIntOrNull()?:0
        val date = Date()
        val mainModel = MainModel(0, scanResultFragmentBinding.ediScanResult.text.toString(), date.time, getType(),
                if (locationId == 0) null else locationId, if (floorId == 0) null else floorId
                , if (nameId == 0) null else nameId, scanResultFragmentBinding.ediBayNumber.text.toString(),googleMap.cameraPosition.target.latitude,
                googleMap.cameraPosition.target.longitude,null)
        mainModel.dateString = DateUtility.parseDateToDateTimeStr(Config.DATE_TIME_PATTERN, date)
        //get screenshot
        googleMap.addMarker(MarkerOptions().position(com.google.android.gms.maps.model.LatLng(googleMap.cameraPosition.target.latitude,
                googleMap.cameraPosition.target.longitude)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker)))
        scanResultFragmentBinding.btnMarker.visibility=View.GONE
        googleMap.snapshot {
            mainModel.image=Utility.convertBitmapToBase64(Utility.scaleBitmapDown(it,640))
            mainViewModel.addMainModel(mainModel)
            finish()
        }

    }

    fun getType():Int{
        when(scanEnum){
            SCAN_ENUM.VIN.ordinal ->
                return 0
            SCAN_ENUM.REGO.ordinal ->
                return 1
            SCAN_ENUM.BARCODE.ordinal ->
                return 2
            else -> return 3

        }
    }

    fun getToolbarTitle():String{
        when(scanEnum){
            SCAN_ENUM.VIN.ordinal ->
                return "SCAN VIN"
            SCAN_ENUM.REGO.ordinal ->
                return "SCAN REGO"
            SCAN_ENUM.BARCODE.ordinal ->
                return "SCAN BARCODE"
            else -> return "SCAN QRCODE"

        }
    }
    fun initInfo() {
        rxPermissions = RxPermissions(this)
        scanResultFragmentBinding.txtTitle.text=getToolbarTitle()

//        //spinner
//        listLocation= mainViewModel.locationDAO.getAll() as ArrayList<LocationModel>
//        listLocation.add(0,LocationModel("---none---",0))
//        val adapterLocation = ArrayAdapter<LocationModel>(this,  android.R.layout.simple_spinner_dropdown_item, listLocation);
//        adapterLocation.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
//        scanResultFragmentBinding.spnLocation.setAdapter(adapterLocation)
//        scanResultFragmentBinding.spnLocation.onItemSelectedListener=object: AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                (parent!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
//                PreferenceSetting.locationPosition=position
//            }
//
//        }
//
//        listFloor= mainViewModel.floorDAO.getAll() as ArrayList<FloorModel>
//        listFloor.add(0,FloorModel("---none---",0))
//        val adapterFloor = ArrayAdapter<FloorModel>(this,  android.R.layout.simple_spinner_dropdown_item, listFloor);
//        adapterFloor.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
//        scanResultFragmentBinding.spnFloor.setAdapter(adapterFloor)
//        scanResultFragmentBinding.spnFloor.onItemSelectedListener=object: AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                (parent!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
//                PreferenceSetting.floorPosition=position
//            }
//
//        }
//
//        listName=mainViewModel.nameDAO.getAll() as ArrayList
//        listName.add(0,OperatorModel("---none---",0))
//        val adapterName= ArrayAdapter<OperatorModel>(this,  android.R.layout.simple_spinner_dropdown_item, listName);
//        adapterName.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
//        scanResultFragmentBinding.spnName.setAdapter(adapterName)
//        scanResultFragmentBinding.spnName.onItemSelectedListener=object: AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                (parent!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
//                PreferenceSetting.namePosition=position
//            }
//
//        }
//        scanResultFragmentBinding.spnLocation.setSelection(PreferenceSetting.locationPosition)
//        scanResultFragmentBinding.spnFloor.setSelection(PreferenceSetting.floorPosition)
//        scanResultFragmentBinding.spnName.setSelection(PreferenceSetting.namePosition)

        scanResultFragmentBinding.txtLocation.text=PreferenceSetting.locationSetting?.name?:""
        scanResultFragmentBinding.txtFloor.text=PreferenceSetting.floorSetting?.name?:""
        scanResultFragmentBinding.txtOperator.text=PreferenceSetting.nameSetting?.name?:""
        //google map
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            it?.let {
                googleMap = it
                googleMap.mapType = intent.getIntExtra("mapType",0)
                rxPermissions
                        .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .subscribe { it: Boolean? ->
                            if (it!!) {
                                val latLng=LatLng(intent.getDoubleExtra("lat", 0.0), intent.getDoubleExtra("lng",0.0))
                                val update = CameraUpdateFactory.newLatLngZoom(latLng,
                                        MAP_ZOOM);
                                googleMap.animateCamera(update)

                            }
                        }

            }
        }

        Handler().postDelayed({
            val layoutHeight=scanResultFragmentBinding.layoutImage.height
            val imageHeight=scanResultFragmentBinding.btnMarker.height
            (scanResultFragmentBinding.btnMarker.layoutParams as ConstraintLayout.LayoutParams).setMargins(0,(layoutHeight/2)-(imageHeight),0,0)
            scanResultFragmentBinding.btnMarker.requestLayout()
        },500)

        if(scanEnum==SCAN_ENUM.VIN.ordinal || scanEnum==SCAN_ENUM.REGO.ordinal)
        {
            scanResultFragmentBinding.btnGallery.visibility=View.VISIBLE
            scanResultFragmentBinding.btnCamera.visibility=View.VISIBLE
        }
        else{
            scanResultFragmentBinding.btnGallery.visibility=View.GONE
            scanResultFragmentBinding.btnCamera.visibility=View.VISIBLE
        }

        this.listLocation.add(FilterDialogModel("--none--","0","0"==PreferenceSetting.locationSetting?.code?:0))
        mainViewModel.locationDAO.getAll().forEach {
            this.listLocation.add(FilterDialogModel(it.name,it.id.toString(),it.id.toString()==PreferenceSetting.locationSetting?.code?:"0"))
        }
        this.listFloor.add(FilterDialogModel("--none--","0","0"==PreferenceSetting.floorSetting?.code?:"0"))
        mainViewModel.floorDAO.getAll().forEach {
            this.listFloor.add(FilterDialogModel(it.name,it.id.toString(),it.id.toString()==PreferenceSetting.floorSetting?.code?:"0"))
        }
        this.listName.add(FilterDialogModel("--none--","0","0"==PreferenceSetting.nameSetting?.code?:"0"))
        mainViewModel.nameDAO.getAll().forEach {
            this.listName.add(FilterDialogModel(it.name,it.id.toString(),it.id.toString()==PreferenceSetting.nameSetting?.code?:"0"))
        }
    }

    fun loadInfo() {
        val PATTERN_VIN ="(?s).*([0-9ABCDEFGHJKLNPRSTUVWXYZ]{17}).*";
        when(scanEnum){
            SCAN_ENUM.VIN.ordinal ->{
                val matcher = Pattern.compile(PATTERN_VIN).matcher(result)
                if (matcher.matches()) {
                    scanResultFragmentBinding.ediScanResult.setText(matcher.group(1).replace("[^0-9A-Z]".toRegex(), ""))
                }
            }
            SCAN_ENUM.REGO.ordinal ->{
                scanResultFragmentBinding.ediScanResult.setText(result?.replace("[^0-9A-Z]".toRegex(), ""))
            }
        }
    }

    fun loadCloudScan(bitmap: Bitmap){

        scanResultFragmentBinding.animationView.visibility=View.VISIBLE
        val visionBuilder = Vision.Builder(
                NetHttpTransport(),
                AndroidJsonFactory(),
                null)

        visionBuilder.setVisionRequestInitializer(
                VisionRequestInitializer(Config.CLOUD_VISION_API_KEY))
        val vision = visionBuilder.build()
        val desiredFeature = Feature()
        desiredFeature.setType(Config.CLOUD_VISION_DETECT_TYPE)
        val request = AnnotateImageRequest()
        val inputImage = Image()
        inputImage.setContent(Utility.convertBitmapToBase64(bitmap))
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));

        val batchRequest = BatchAnnotateImagesRequest()
        batchRequest.setRequests(Arrays.asList(request))
        //bitmap.recycle()

        doAsync {
            var batchResponse: BatchAnnotateImagesResponse
            try{
                batchResponse = vision.images().annotate(batchRequest).execute()
                uiThread {
                    val imagesResponse=batchResponse.getResponses().get(0)

                    scanResultFragmentBinding.animationView.visibility=View.GONE
                    if(scanEnum== SCAN_ENUM.VIN.ordinal) {
                        val content = imagesResponse.getFullTextAnnotation();
                        if (content != null)
                        {
                            result=content.text
                            loadInfo()
                        }
                        else content?.let { it1 -> Toasty.error(this@ScanResultActivity, "Scan error, no text found").show() }
                    }
                    else if(scanEnum== SCAN_ENUM.REGO.ordinal){
                        val list = imagesResponse.textAnnotations;
                        var result :StringBuffer= StringBuffer()
                        if(list!=null) {
                            list.forEachIndexed { index, entityAnnotation ->
                                if(index>=1){
                                    if(entityAnnotation.boundingPoly.vertices.count()>=4)
                                        if(Math.abs(entityAnnotation.boundingPoly.vertices[1].y-entityAnnotation.boundingPoly.vertices[3].y)>=100)
                                            result.append(entityAnnotation.description)
                                }
                            }
                            this@ScanResultActivity.result=result.toString()
                            loadInfo()
                        }
                        else list?.let { it1 -> Toasty.error(this@ScanResultActivity,"Scan error, no text found").show() }
                    }
                }
            }
            catch (exp:Exception) {

                scanResultFragmentBinding.animationView.visibility=View.GONE
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(mainEventBus: MainEventBus) {
        mainEventBus.bitmapURL?.let {
            val bitmap=Utility.getBitmapFromURL(it)
            scanResultFragmentBinding.imageView.setImageBitmap(bitmap)
            loadCloudScan(bitmap)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
