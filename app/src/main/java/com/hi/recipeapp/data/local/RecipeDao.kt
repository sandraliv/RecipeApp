package com.hi.recipeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hi.recipeapp.classes.RecipeCard
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("Select * FROM user_recipes")
    fun getAll(): Flow<List<RecipeCard>>

    @Query("DELETE FROM user_recipes")
    suspend fun removeAll()

    @Query("Select * FROM user_recipes WHERE id = :id")
    fun findByPrimaryKey(id: Int): Recipe

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)
}