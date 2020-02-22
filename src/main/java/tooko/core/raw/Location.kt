/*
 * Copyright (c) 2019 - 2020 KazamaWataru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "unused"
)

package tooko.core.raw

import kotlinx.coroutines.CoroutineScope
import td.TdApi.*
import tooko.core.client.TdAbsHandler

/**
 * Changes the location of the current user
 * Needs to be called if GetOption("is_location_visible") is true and location changes for more than 1 kilometer
 *
 * @location - The new location of the user
 */
suspend fun TdAbsHandler.setLocation(
    location: Location? = null
) = sync<Ok>(
    SetLocation(
        location
    )
)

suspend fun TdAbsHandler.setLocationOrNull(
    location: Location? = null
) = syncOrNull<Ok>(
    SetLocation(
        location
    )
)

fun TdAbsHandler.setLocation(
    location: Location? = null,
    block: (suspend CoroutineScope.(Ok) -> Unit)
) = send(
    SetLocation(
        location
    ),block = block
)
