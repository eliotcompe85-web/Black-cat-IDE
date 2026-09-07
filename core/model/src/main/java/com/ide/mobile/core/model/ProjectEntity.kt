package com.ide.mobile.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rootPath: String,
    val lastOpenedTimestamp: Long,
    val isRemote: Boolean,
    val remoteRepositoryUrl: String? = null
)
