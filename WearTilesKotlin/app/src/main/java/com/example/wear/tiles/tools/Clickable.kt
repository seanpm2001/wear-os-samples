package com.example.wear.tiles.tools

import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ModifiersBuilders

val emptyClickable = ModifiersBuilders.Clickable.Builder()
    .setOnClick(ActionBuilders.LoadAction.Builder().build())
    .setId("")
    .build()
