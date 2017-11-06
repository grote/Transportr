package de.grobox.transportr

import dagger.Module

@Module(includes = arrayOf(ViewModelModule::class))
internal class TestModule(application: TestApplication) : AppModule(application)
