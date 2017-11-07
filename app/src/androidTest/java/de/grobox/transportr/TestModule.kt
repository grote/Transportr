package de.grobox.transportr

import dagger.Module
import de.grobox.transportr.data.TestDbModule

@Module(includes = arrayOf(TestDbModule::class, ViewModelModule::class))
internal class TestModule(application: TestApplication) : AppModule(application)
