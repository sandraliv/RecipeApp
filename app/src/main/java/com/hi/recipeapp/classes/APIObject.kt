package com.hi.recipeapp.classes

import com.google.gson.annotations.SerializedName

data class APIObject(
    val id: String,
    val name: String,
    val data: LaptopData
)

data class LaptopData(
    val year: Int,
    val price: Double,
    @SerializedName("CPU model") val cpuModel: String,
    @SerializedName("Hard disk size") val hardDiskSize: String
)
