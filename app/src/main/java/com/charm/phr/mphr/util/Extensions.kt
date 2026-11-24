package com.charm.phr.mphr.util

import android.util.Log
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.ReadableInstant


fun  RecyclerView.ViewHolder.setVerticalItemMargin(cardView: CardView, index: Int, size: Int){

    val layoutParam = cardView.layoutParams as ViewGroup.MarginLayoutParams
    when (index) {
        0 -> {
            layoutParam.topMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_padding_half)
            layoutParam.bottomMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_margin_vertical)
        }
        size - 1 -> {
            layoutParam.bottomMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_padding_half)
            layoutParam.topMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_margin_vertical)
        }
        else -> {
            layoutParam.topMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_margin_vertical)
            layoutParam.bottomMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_margin_vertical)
        }
    }

    cardView.layoutParams = layoutParam

}


fun JsonObject.getString(key: String): String? {

    val reqEle = get(key)

    if (reqEle != null && reqEle.isNotJsonNull()) {
        return reqEle.asString
    }

    return null
}

fun JsonObject.getInt(key: String): Int? {
    try {
        val reqEle = get(key)
        if (reqEle != null && reqEle.isNotJsonNull()) {
            return try {
                reqEle.asInt
            } catch (e: Exception) {
                val intStr = reqEle.asString ?: ""
                intStr.toIntOrNull()
            }
        }
    } catch (e: Exception) {
        Log.d("getInt", e.toString())
    }
    return null
}

fun JsonObject.getBoolean(key: String): Boolean? {
    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return try {
                reqEle.asBoolean
            } catch (e: Exception) {
                val intStr = reqEle.asString ?: ""
                intStr.toBooleanStrictOrNull()
            }
        }
    } catch (e: Exception) {
        Log.d("getBoolean", e.toString())
    }
    return null
}

fun JsonObject.getJsonArray(key: String): JsonArray? {

    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return reqEle.asJsonArray
        }
    } catch (e: Exception) {
        return null
    }

    return null
}

fun JsonObject.getJsonObject(key: String): JsonObject? {

    try {
        val reqEle = get(key)

        if (reqEle != null && reqEle.isNotJsonNull()) {
            return reqEle.asJsonObject
        }
    } catch (e: Exception) {
        return null
    }

    return null
}

fun DateTime.isBeforeOrEqual(readableInstant: ReadableInstant): Boolean{
    return isBefore(readableInstant) || isEqual(readableInstant)
}
fun DateTime.isAfterOrEqual(readableInstant: ReadableInstant): Boolean{
    return isAfter(readableInstant) || isEqual(readableInstant)
}




