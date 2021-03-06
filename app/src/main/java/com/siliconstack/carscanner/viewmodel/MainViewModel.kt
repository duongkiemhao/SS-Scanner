package com.siliconstack.carscanner.viewmodel

import android.arch.lifecycle.AndroidViewModel
import android.arch.persistence.db.SimpleSQLiteQuery
import com.orhanobut.logger.Logger
import com.siliconstack.carscanner.dao.MainDAO
import com.siliconstack.carscanner.AppApplication
import com.siliconstack.carscanner.config.Config
import com.siliconstack.carscanner.model.MainModel
import com.siliconstack.carscanner.room.AppDatabase
import javax.inject.Inject
import com.siliconstack.carscanner.dao.FloorDAO
import com.siliconstack.carscanner.dao.LocationDAO
import com.siliconstack.carscanner.dao.NameDAO
import com.siliconstack.carscanner.model.MainDTO


class MainViewModel @Inject constructor (application: AppApplication): AndroidViewModel(application) {

    var mainDAO: MainDAO = AppDatabase.getDatabase(getApplication()).mainDAO()
    var locationDAO: LocationDAO = AppDatabase.getDatabase(getApplication()).locationDAO()
    var floorDAO: FloorDAO = AppDatabase.getDatabase(getApplication()).floorDAO()
    var nameDAO: NameDAO = AppDatabase.getDatabase(getApplication()).nameDAO()

    var items = ArrayList<MainDTO>()
    var keyword=""

    init {


    }

    fun initItems(){
        items = mainDAO.query(SimpleSQLiteQuery("select a.*, b.name as locationName,c.name as floorName,d.name as operatorName ,0 as isSelected   from MainModel a left join LocationModel b on a.locationID=b.id left join FloorModel c on a.floorID=c.id left join OperatorModel d on a.locationID=d.id  order by a.timestamp desc limit "+Config.LIMIT+" offset 0")) as ArrayList<MainDTO>
    }

    fun addMainModel(mainModel: MainModel){
        mainDAO.addRow(mainModel)
    }

    fun filterListSearch(isDesc:Boolean,offset:Int,orderBy:String):List<MainDTO> {
        val query="select a.*, b.name as locationName,c.name as floorName,d.name as operatorName ,0 as isSelected   from MainModel a left join LocationModel b on a.locationID=b.id left join FloorModel c on a.floorID=c.id left join OperatorModel d on a.locationID=d.id where a.scanText like '%"+keyword+"%' or b.name like '%"+keyword+"%' or c.name like '%"+keyword+"%' or d.name like '%"+keyword+"%' or a.bayNumber like '%"+keyword+"%' order by "+orderBy+" "+(if(isDesc) "desc" else "asc")+" limit "+Config.LIMIT+" offset "+offset
        Logger.d(query)
        return mainDAO.query(SimpleSQLiteQuery(query)) as ArrayList<MainDTO>
    }



    fun isScanTextExist(scanText:String):MainDTO?{
        val items=mainDAO.query(SimpleSQLiteQuery("select a.*, b.name as locationName,c.name as floorName,d.name as operatorName ,0 as isSelected   from MainModel a left join LocationModel b on a.locationID=b.id left join FloorModel c on a.floorID=c.id left join OperatorModel d on a.locationID=d.id where a.scanText = '"+scanText+"'")) as ArrayList<MainDTO>
        if(items.count()>0)
            return items[0]
        return null
    }


}