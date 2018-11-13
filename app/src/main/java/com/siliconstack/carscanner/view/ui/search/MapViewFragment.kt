package com.siliconstack.carscanner.view.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.bol.instantapp.exception.NoNetworkException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.config.Config
import com.siliconstack.carscanner.config.Constant
import com.siliconstack.carscanner.databinding.ListviewFragmentBinding
import com.siliconstack.carscanner.databinding.MapviewFragmentBinding
import com.siliconstack.carscanner.di.Injectable
import com.siliconstack.carscanner.model.MainDTO
import com.siliconstack.carscanner.model.SearchDTO
import com.siliconstack.carscanner.view.control.CSVWriter
import com.siliconstack.carscanner.view.helper.DialogHelper
import com.siliconstack.carscanner.view.utility.DateUtility
import com.siliconstack.carscanner.view.utility.Utility
import com.siliconstack.carscanner.viewmodel.MainViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
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


class MapViewFragment : Fragment(), Injectable,MapViewFragmentListener {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var mapviewFragmentBinding: MapviewFragmentBinding
    lateinit var mainViewModel: MainViewModel
    lateinit var googleMap: GoogleMap

    val rxPermissions by lazy {
        RxPermissions(this)
    }

    companion object {
        fun newInstance(): MapViewFragment {
            val fragment = MapViewFragment()
            val args = Bundle()

            fragment.setArguments(args)
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapviewFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.mapview_fragment, null, false);
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        AppApplication.appComponent.injectViewModel(mainViewModel)
        return mapviewFragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        init()

    }


    @SuppressLint("MissingPermission")
    private fun init() {
        mainViewModel.initItems()

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            it?.let {
                googleMap = it
                googleMap.mapType = Config.MAP_DEFAULT_TYPE
                googleMap.uiSettings.isZoomControlsEnabled = true
                rxPermissions
                        .request(Manifest.permission.ACCESS_FINE_LOCATION)
                        .subscribe { it: Boolean? ->
                            if (it!!) {

                                googleMap.isMyLocationEnabled = true
                                googleMap.uiSettings.isMyLocationButtonEnabled = true

                                focusAllMarkers()
                            }
                        }


            }
        }
    }



    @SuppressLint("MissingPermission")
    fun focusAllMarkers() {
        val builder = LatLngBounds.Builder()
          mainViewModel.items.forEach {
              if(it.lat==null || it.lat==0.0)
                  return@forEach
              val latLng=LatLng(it.lat?:0.0,it.lng?:0.0)
              val markerOption=MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker))

              var marker=googleMap.addMarker(markerOption)
              marker.tag=it
              marker.showInfoWindow()
              builder.include(latLng)
          }
        val  bounds = builder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        googleMap.animateCamera(cameraUpdate)

        val customInfoWindow = CustomInfoWindowGoogleMap(context!!)
        googleMap.setInfoWindowAdapter(customInfoWindow)


    }

    private fun setListener() {

    }



}

interface MapViewFragmentListener {
}
