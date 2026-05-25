package com.learnlab.content.chapter

import kotlinx.serialization.Serializable

@Serializable
data class CastFile(
    val characters: List<Character>,
)

@Serializable
data class Character(
    val id: String,
    val name: String,
    val role: String,
    val displayName: String,
    val region: String? = null,
    val shortBio: String? = null,
    val avatar: String? = null,
    val accentColor: String? = null,
)
