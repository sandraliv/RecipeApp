package com.hi.recipeapp

import com.google.gson.GsonBuilder
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.ui.networking.NetworkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkService(retrofit: Retrofit): NetworkService {
        return retrofit.create(NetworkService::class.java)
    }

    @Provides
    @Singleton
    fun provideRecipeService(networkService: NetworkService): RecipeService {
        return RecipeService(networkService)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()  // Optional, in case your API has lenient JSON
            .create()  // Create a Gson instance with default settings (you can add customizations here)

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson)) // Add the customized Gson instance here
            .build()
    }


}