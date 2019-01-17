package jp.tsukakei.service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jp.tsukakei.model.Tweets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(hikari())
        transaction {
            create(Tweets)
            Tweets.insert {
                it[tweet] = "first tweet"
                it[updatedAt] = System.currentTimeMillis()
            }
            Tweets.insert {
                it[tweet] = "second tweet"
                it[updatedAt] = System.currentTimeMillis()
            }
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction { block() }
            }
}