package jp.tsukakei

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import io.ktor.jackson.jackson
import io.ktor.websocket.WebSockets
import jp.tsukakei.controller.tweet
import jp.tsukakei.service.DatabaseFactory
import jp.tsukakei.service.TweetService
import kotlinx.html.*
import kotlinx.css.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }
    DatabaseFactory.init()
    val tweetService = TweetService()
    install(Routing) {
        tweet(tweetService)
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
    }
}
