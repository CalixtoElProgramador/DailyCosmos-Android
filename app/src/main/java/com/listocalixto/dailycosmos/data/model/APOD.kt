package com.listocalixto.dailycosmos.data.model

import android.view.View
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

data class APOD(
    val copyright: String = "",
    val date: String = "",
    val explanation: String = "",
    val hdurl: String = "",
    val media_type: String = "",
    val thumbnail_url: String = "",
    val title: String = "",
    val url: String = "",
    var is_favorite: Int = -1,
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

    @ColumnInfo(name = "thumbnail_url")
    val thumbnail_url: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "url")
    val url: String = "",

    @ColumnInfo(name = "is_favorite")
    val is_favorite: Int = -1
)

@Entity
data class FavoriteEntity(
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

    @ColumnInfo(name = "thumbnail_url")
    val thumbnail_url: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "url")
    val url: String = "",

    @ColumnInfo(name = "uid")
    val uid: String? = ""
)

fun List<APODEntity>.toAPODList(): List<APOD> {
    val resultList = mutableListOf<APOD>()
    this.forEach { apodEntity ->
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
    this.thumbnail_url,
    this.title,
    this.url,
    this.is_favorite
)

fun APODEntity.toFavorite(uid: String?): FavoriteEntity = FavoriteEntity(
    this.date,
    this.copyright,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.thumbnail_url,
    this.title,
    this.url,
    uid = uid
)

fun APOD.toAPODEntity(is_favorite: Int): APODEntity = APODEntity(
    this.date,
    this.copyright,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.thumbnail_url,
    this.title,
    this.url,
    is_favorite = is_favorite
)

fun APOD.toFavorite(uid: String?): FavoriteEntity = FavoriteEntity(
    this.date,
    this.copyright,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.thumbnail_url,
    this.title,
    this.url,
    uid = uid
)

fun FavoriteEntity.toAPODEntity(is_favorite: Int): APODEntity = APODEntity(
    this.date,
    this.copyright,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.thumbnail_url,
    this.title,
    this.url,
    is_favorite = is_favorite
)

fun FavoriteEntity.toAPOD(is_favorite: Int): APOD = APOD(
    this.copyright,
    this.date,
    this.explanation,
    this.hdurl,
    this.media_type,
    this.thumbnail_url,
    this.title,
    this.url,
    is_favorite = is_favorite
)