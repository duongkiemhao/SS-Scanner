package com.siliconstack.carscanner.di

import android.content.Context
import com.google.gson.Gson
import com.siliconstack.carscanner.AppApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [NetModule::class, DatabaseModule::class, RepositoryModule::class, ViewModelModule::class])
class AppModule() {

    @Singleton
    @Provides
    fun provideGSon(): Gson {
        return Gson()
    }


    @Singleton
    @Provides
    fun provideContext(): Context{
        return AppApplication()
    }


}