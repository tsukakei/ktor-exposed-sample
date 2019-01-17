package jp.tsukakei.service

import jp.tsukakei.model.*
import jp.tsukakei.service.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class TweetService {
    private val listeners = mutableMapOf<Int, suspend (Notification<Tweet?>) -> Unit>()

    fun addChangeListener(id: Int, listener: suspend (Notification<Tweet?>) -> Unit) {
        listeners[id] = listener
    }

    fun removeChangeListener(id: Int) = listeners.remove(id)

    private suspend fun onChange(type: ChangeType, id: Int, entity: Tweet?=null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    suspend fun getAllTweets(): List<Tweet> = dbQuery {
        Tweets.selectAll().map { toTweet(it) }
    }

    suspend fun getTweet(id: Int): Tweet? = dbQuery {
        Tweets.select {
            (Tweets.id eq id)
        }.mapNotNull { toTweet(it) }
            .singleOrNull()
    }

    suspend fun updateTweet(newTweet: NewTweet): Tweet? {
        val id = newTweet.id
        return if (id == null) {
            addTweet(newTweet)
        } else {
            dbQuery {
                Tweets.update({Tweets.id eq id}) {
                    it[tweet] = newTweet.tweet
                    it[updatedAt] = newTweet.updatedAt
                }
            }
            getTweet(id).also {
                onChange(ChangeType.UPDATE, id, it)
            }
        }
    }

    suspend fun addTweet(newTweet: NewTweet): Tweet {
        var key = 0
        dbQuery {
            key = (
                    Tweets.insert {
                        it[tweet] = newTweet.tweet
                        it[updatedAt] = System.currentTimeMillis()
                    }
                    get Tweets.id
                    )!!
        }
        return getTweet(key)!!.also {
            onChange(ChangeType.CREATE, key, it)
        }
    }

    suspend fun deleteTweet(id: Int): Boolean = dbQuery {
        Tweets.deleteWhere { Tweets.id eq id } > 0
    }.also {
        if (it) onChange(ChangeType.DELETE, id)
    }



    private fun toTweet(row: ResultRow): Tweet =
            Tweet(
                id = row[Tweets.id],
                tweet = row[Tweets.tweet],
                updatedAt = row[Tweets.updatedAt]
            )
}