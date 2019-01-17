package jp.tsukakei.model

import org.jetbrains.exposed.sql.Table

object Tweets: Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val tweet = text("tweet")
    val updatedAt = long("updatedAt")
}

data class Tweet(
    val id: Int,
    val tweet: String,
    val updatedAt: Long
)

data class NewTweet(
    val id: Int,
    val tweet: String,
    val updatedAt: Long
)