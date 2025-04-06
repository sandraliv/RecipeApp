package com.hi.recipeapp

import android.app.Application
import com.google.gson.GsonBuilder
import com.hi.recipeapp.classes.SessionManager
import com.hi.recipeapp.services.RecipeService
import com.hi.recipeapp.ui.networking.NetworkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton
import androidx.room.Room
import com.hi.recipeapp.data.local.RecipeDatabase
import com.hi.recipeapp.data.local.RecipeDao


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
    fun provideRecipeService(
        networkService: NetworkService,
        sessionManager: SessionManager // Add sessionManager as a parameter here
    ): RecipeService {
        return RecipeService(networkService, sessionManager) // Pass sessionManager to the RecipeService constructor
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()  // Optional, in case your API has lenient JSON
            .create()  // Create a Gson instance with default settings (you can add customizations here)

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson)) // Add the customized Gson instance here
            .build()
    }

    // Add a provider for SessionManager
    @Provides
    @Singleton
    fun provideSessionManager(application: Application, recipeDao: RecipeDao): SessionManager {
        return SessionManager(application,recipeDao) // Provide SessionManager instance
    }


    @Provides
    @Singleton
    fun provideDatabase(application: Application): RecipeDatabase {
        return Room.databaseBuilder(
            application,
            RecipeDatabase::class.java,
            "recipe_database"
        ).fallbackToDestructiveMigration() // optional: clears DB on schema change
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(database: RecipeDatabase): RecipeDao {
        return database.recipeDao()
    }


}