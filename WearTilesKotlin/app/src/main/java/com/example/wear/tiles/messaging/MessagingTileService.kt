/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.wear.tiles.messaging

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import coil.Coil
import coil.ImageLoader
import com.google.android.horologist.tiles.CoroutinesTileService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Creates a Messaging tile, showing up to 4 contacts, an icon button and compact chip.
 *
 * It extends [CoroutinesTileService], a Coroutine-friendly wrapper around
 * [androidx.wear.tiles.TileService], and implements [tileRequest] and [resourcesRequest].
 *
 * The main function, [tileRequest], is triggered when the system calls for a tile and implements
 * ListenableFuture which allows the Tile to be returned asynchronously. // TODO:
 *
 * Resources are provided with the [resourcesRequest] method, which is triggered when the tile
 * uses an Image.
 */
class MessagingTileService : CoroutinesTileService() {

    private lateinit var renderer: MessagingTileRenderer
    private lateinit var repo: MessagingRepository
    private lateinit var imageLoader: ImageLoader
    private lateinit var tileStateFlow: StateFlow<MessagingTileState?>

    override fun onCreate() {
        super.onCreate()
        renderer = MessagingTileRenderer(application)
        repo = MessagingRepository(application)
        imageLoader = Coil.imageLoader(application)
        tileStateFlow = createTileStateFlow()
        Log.d("!!!", "messaging tile service = onCreate")
    }

    /**
     * TODO: why stateFlow
     * Creates a [StateFlow] object that can be used to store the latest tile state. We're using
     * this because
     */
    private fun createTileStateFlow() = repo.getFavoriteContacts()
        .map { contacts -> MessagingTileState(contacts) }
        .stateIn(
            lifecycleScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        Log.d("!!!", "messaging tile service = tileRequest")
        val tileState = getLatestTileState()
        Log.d("!!!", "messaging tile service = contacts: ${tileState.contacts}")
        return renderer.renderTimeline(tileState, requestParams)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        Log.d("!!!", "messaging tile service = resourceRequest")
        val tileState = getLatestTileState()
        val images = fetchRequestedResources(tileState, requestParams)
        return renderer.produceRequestedResources(images, requestParams)
    }

    private suspend fun getLatestTileState(): MessagingTileState {
        var tileState = tileStateFlow.filterNotNull().last()

        if (tileState.contacts.isEmpty()) {
            updateContacts()
            tileState = tileStateFlow.filterNotNull().first()
        }

        return tileState
    }

    /**
     * If our data source (the repository) is empty/has stale data, this is where we could perform
     * an update.
     *
     * For this sample, we're updating the repository with fake data
     * ([MessagingRepository.knownContacts]).
     */
    private suspend fun updateContacts() {
        repo.updateContacts(MessagingRepository.knownContacts)
    }

    private suspend fun fetchRequestedResources(
        tileState: MessagingTileState,
        requestParams: ResourcesRequest,
    ): Map<Contact, Bitmap> {
        val requestedAvatars = if (requestParams.resourceIds.isEmpty()) {
            tileState.contacts
        } else {
            tileState.contacts.filter {
                requestParams.resourceIds.contains(MessagingTileRenderer.ID_CONTACT_PREFIX + it.id)
            }
        }

        val images = coroutineScope {
            requestedAvatars.map { contact ->
                async {
                    val image = imageLoader.loadAvatar(this@MessagingTileService, contact)

                    image?.let { contact to it }
                }
            }
        }.awaitAll().filterNotNull().toMap()

        return images
    }
}
