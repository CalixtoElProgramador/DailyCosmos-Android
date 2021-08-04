package com.listocalixto.dailycosmos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class APOD(
    val copyright: String = "",
    val date: String = "",
    val explanation: String = "",
    val hdurl: String = "",
    val media_type: String = "",
    val title: String = "",
    val url: String = ""
)

@Entity
data class APODEntity(
    @PrimaryKey
    val date: String = "",

    @ColumnInfo(name = "copyright")
    val copyright: String = "",

    @ColumnInfo(name = "explanation")
    val explanation: String = "",

    @ColumnInfo(name = "hdurl")
    val hdurl: String = "",

    @ColumnInfo(name = "media_type")
    val media_type: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "url")
    val url: String = ""
)

fun List<APODEntity>.toAPODList(): List<APOD> {
    val resultList = mutableListOf<APOD>()
    this.forEach{apodEntity ->
        resultList.add(apodEntity.toAPOD())
    }
    return resultList
}

fun APODEntity.toAPOD(): APOD = APOD(
    this.copyright,
    this.date,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.title,
    this.url
)

fun APOD.toAPODEntity(): APODEntity = APODEntity(
    this.date,
    this.copyright,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.title,
    this.url
)