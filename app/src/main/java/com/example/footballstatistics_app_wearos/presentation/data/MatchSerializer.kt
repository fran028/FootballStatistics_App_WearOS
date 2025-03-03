package com.example.footballstatistics_app_wearos.presentation.data

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.write
import kotlin.text.decodeToString
import kotlin.text.encodeToByteArray
/*
object MatchSerializer : androidx.datastore.core.Serializer<Match> {
    override val defaultValue: Match
        get() = Match(
            date = "",
            total_time = "",
            away_corner_location = "",
            home_corner_location = "",
            kickoff_location = "",
            start_location = "",
            activityData = "",
            iniTime = "",
            endTime = "",
            matchStatus = "Not Started",
            end_location = ""
        )

    override suspend fun readFrom(input: InputStream): Match {
        return try {
            Json.decodeFromString(
                deserializer = Match.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: Match, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = Match.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}*/