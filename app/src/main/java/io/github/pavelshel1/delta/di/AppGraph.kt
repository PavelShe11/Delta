package io.github.pavelshel1.delta.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.pavelshel1.delta.R
import io.github.pavelshel1.delta.about.AppInfo
import io.github.pavelshel1.delta.db.DeltaDatabase
import io.github.pavelshel1.delta.root.DefaultRootComponent

@DependencyGraph(AppScope::class)
interface AppGraph {

    val rootComponentFactory: DefaultRootComponent.Factory

    @Provides
    fun provideDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(DeltaDatabase.Schema, context, "delta.db")

    @Provides
    fun provideAppInfo(context: Context): AppInfo =
        AppInfo.fromBuildConfig(context.getString(R.string.app_name))

    @Provides
    fun provideDatabase(driver: SqlDriver): DeltaDatabase = DeltaDatabase(driver)

    @Provides
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
    
}
