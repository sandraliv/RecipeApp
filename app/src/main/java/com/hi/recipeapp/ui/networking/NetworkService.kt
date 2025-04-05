package com.hi.recipeapp.ui.networking

import com.hi.recipeapp.classes.Category
import com.hi.recipeapp.classes.FavoriteRecipesDTO
import com.hi.recipeapp.classes.FullRecipe
import com.hi.recipeapp.classes.LoginRequest
import com.hi.recipeapp.classes.RecipeCard
import com.hi.recipeapp.classes.RecipeTag
import com.hi.recipeapp.classes.User
import com.hi.recipeapp.classes.UserCreateDTO
import com.hi.recipeapp.classes.UserDTO
import com.hi.recipeapp.classes.UserFullRecipe
import com.hi.recipeapp.classes.UserRecipeCard
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Define API calls inside NetworkService (No ApiService.kt needed)
interface NetworkService {

    @PATCH("users/{id}/updatePassword")
    suspend fun patchUpdateUserPassword(
        @Path("id") id: Int,
        @Body updates: Map<String, String>
    ): Response<String>

    @GET("recipes")
    fun getRecipesByQueryAndTags(
        @Query("query") query: String?,
        @Query("tags") tags: Set<String>?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "RATING"
    ): Call<List<RecipeCard>>

    @GET("users")
    suspend fun getAllUsers(
    ): Response<List<User>>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): Response<Unit>

    @GET("recipes/all")
    fun getAllRecipes(
        @Query("page") page: Int,       // The page number to fetch
        @Query("size") size: Int,       // The number of items per page
        @Query("sort") sort: String     // The sorting criteria, either "rating" or "date"
    ): Call<List<RecipeCard>>

    @Multipart
    @POST("users/{id}/add_profile_pic")
    suspend fun postProfilePic(
        @Path("id") userId: Int,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @GET("/recipes/byCategory")
    fun getRecipesByCategory(
        @Query("categories") categories: Set<String>,   // Using String to represent enum names
        @Query("sort") sort: String,                     // The sorting criteria, either "rating" or "date"
        @Query("page") page: Int,                       // The page number to fetch
        @Query("size") size: Int                        // The number of items per page
    ): Call<List<RecipeCard>>  // Return Call for asynchronous processing

    @POST("recipes/{id}/addRating")
    suspend fun addRatingToRecipe(
        @Path("id") recipeId: Int,
        @Query("userId") userId: Int,
        @Query("score") score: Int
    ): Response<String>

    @GET("recipes/{id}")
    fun getRecipeById(@Path("id") id: Int): Call<FullRecipe>

    @GET("recipes/byDate")
    fun getRecipesSortedByDate(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<List<RecipeCard>>

    @GET("recipes/highestRated")
    fun getRecipesSortedByRating(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<List<RecipeCard>>

    @GET("users/{id}/getUserFav")
    suspend fun getUserFavorites(@Path("id") userId: Int): Response<List<RecipeCard>>

    @GET("user-recipes/{userId}/getUserRecipes")
    suspend fun getUserRecipes(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<UserRecipeCard>>

    @POST("recipes/{id}/addAsFav")
    suspend fun addRecipeToFavorites(
        @Path("id") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<String>

    @DELETE("recipes/removeFavorite/{recipeId}")
    suspend fun removeRecipeFromFavorites(
        @Path("recipeId") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<String>

    @GET("user-recipes/{id}")
    suspend fun getUserRecipeById(
        @Path("id") recipeId: Int,
        @Query("userId") userId: Int
    ): Response<UserFullRecipe>


    @GET("users/1")
    fun getRoot(): Call<UserDTO>

    @POST("users/Login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserDTO>

    @POST("users/Register")
    fun signup(@Body signupRequest: UserCreateDTO): Call<String> // ✅ Call<String> í stað Call<UserDTO>

    @POST("user-recipes/{userId}/upload")
    suspend fun uploadRecipe(
        @Path("userId") userId: Int,
        @Body recipe: UserFullRecipe
    ): Response<UserFullRecipe>


    interface ImageUploadService {
        @Multipart
        @POST("recipes/{recipeId}/upload")  // API Endpoint úr bakenda
        suspend fun uploadImageToRecipe(
            @Path("recipeId") recipeId: Int,
            @Part file: MultipartBody.Part
        ): Response<String>
    }




}

