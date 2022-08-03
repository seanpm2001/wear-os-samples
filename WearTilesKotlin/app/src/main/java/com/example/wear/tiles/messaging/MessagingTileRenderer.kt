/*
 * Copyright 2022 The Android Open Source Project
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

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.TitleChip
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.example.wear.tiles.R
import com.example.wear.tiles.golden.GoldenTilesColors
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElement {
        return PrimaryLayout.Builder(deviceParameters)
            .setContent(
                TitleChip.Builder(context, "Start", emptyClickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.Black)
                        )
                    )
                    .build()
            ).setPrimaryChipContent(
                CompactChip.Builder(context, "New", emptyClickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkYellow),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White)
                        )
                    )
                    .build()
            ).build()

//        return messagingTileLayout(context, deviceParameters, state)
    }

    override fun Resources.Builder.produceRequestedResources(
        resourceResults: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        if (resourceIds.isEmpty() || resourceIds.contains(ID_IC_SEARCH)) {
            addIdToImageMapping(
                ID_IC_SEARCH, imageResourceFrom(R.drawable.ic_search)
            )
        }

        // Add the scaled & cropped avatar images
        resourceResults.forEach { (contact, bitmap) ->
            val imageResource = bitmapToImageResource(bitmap)
            // Add each created image resource to the list
            addIdToImageMapping(
                "$ID_CONTACT_PREFIX${contact.id}", imageResource
            )
        }
    }

    private fun imageResourceFrom(@DrawableRes resourceId: Int) = ImageResource.Builder()
        .setAndroidResourceByResId(
            ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(resourceId)
                .build()
        )
        .build()

    companion object {
        // Dimensions
        internal val SPACING_TITLE_SUBTITLE = DimensionBuilders.dp(4f)
        internal val SPACING_SUBTITLE_CONTACTS = DimensionBuilders.dp(12f)
        internal val SPACING_CONTACTS_HORIZONTAL = DimensionBuilders.dp(8f)
        internal val SPACING_CONTACTS_VERTICAL = DimensionBuilders.dp(4f)

        // Resource identifiers for images
        internal const val ID_IC_SEARCH = "ic_search"
        internal const val ID_CONTACT_PREFIX = "contact:"
    }
}
