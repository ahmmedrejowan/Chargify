package com.rejowan.battify.di

import com.rejowan.battify.repo.HomeRepository
import com.rejowan.battify.repoImpl.HomeRepositoryImpl
import com.rejowan.battify.vm.HomeViewModel
import org.koin.core.module.dsl.viewModel

import org.koin.dsl.module

val homeModule = module {
    single<HomeRepository> { HomeRepositoryImpl(get()) }
    viewModel { HomeViewModel(get()) }
}

