package jp.tsukakei.controller

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.websocket.webSocket
import jp.tsukakei.model.NewTweet
import jp.tsukakei.service.TweetService

fun Route.tweet(tweetService: TweetService) {
    route("/tweet") {

        get("/") {
            call.respond(tweetService.getAllTweets())
        }

        get("/{id}") {
            val tweet = tweetService.getTweet(call.parameters["id"]?.toInt()!!)
        }

        post("/") {
            val tweet = call.receive<NewTweet>()
            call.respond(HttpStatusCode.Created, tweetService.addTweet(tweet))
        }

        delete("/{id}") {
            val removed = tweetService.deleteTweet(call.parameters["id"]?.toInt()!!)
            if (removed) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }
    }

    val mapper = jacksonObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    webSocket("/updates") {
        try {
            tweetService.addChangeListener(this.hashCode()) {
                outgoing.send(Frame.Text(mapper.writeValueAsString(it)))
            }
            while (true) {
                incoming.receiveOrNull() ?: break
            }
        } finally {
            tweetService.removeChangeListener(this.hashCode())
        }
    }
}