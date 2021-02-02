package com.origin.model

import com.google.gson.annotations.SerializedName

class PcAppearance(
    @SerializedName("n")
    val visibleName: String,

    @SerializedName("vt")
    val visibleTitle: String,

    @SerializedName("s")
    val sex: Byte, // Female (1)
) {
}