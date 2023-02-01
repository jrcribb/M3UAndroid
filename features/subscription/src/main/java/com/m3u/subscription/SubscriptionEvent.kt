package com.m3u.subscription

sealed interface SubscriptionEvent {
    data class GetDetails(val url: String) : SubscriptionEvent
    object Sync : SubscriptionEvent

    data class AddToFavourite(val id: Int) : SubscriptionEvent
}
