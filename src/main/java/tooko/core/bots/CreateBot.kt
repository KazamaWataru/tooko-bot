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

package tooko.core.bots

import td.TdApi
import tooko.core.PERSIST_1
import tooko.core.client.TdBotHandler
import tooko.core.env.Env
import tooko.core.env.Lang
import tooko.core.input
import tooko.core.langFor
import tooko.core.text
import tooko.core.utils.*
import com.pengrad.telegrambot.request.GetMe as HttpGetMe

class CreateBot : TdBotHandler() {

    private val createCache = hashMapOf<Int, BotData>()

    override fun onLoad() {

        initFunction("new_bot")

        initPersist(PERSIST_1)

    }

    override suspend fun onFunction(userId: Int, chatId: Long, message: TdApi.Message, function: String, param: String, params: Array<String>, originParams: Array<String>) {

        if (message.fromPrivateOrdelete) {

            val L = userId.langFor

            if (!Env.isAdmin(userId)) {

                if (!Env.PUBLIC_BOT_CREATE) {

                    sudo make L.ERR_PERSIONAL sendTo chatId

                    return

                }

                val count = BotData.DATA.countByField("owner", userId)

                if (Env.BOT_CREATE_MAX != -1 && count >= Env.BOT_CREATE_MAX) {

                    sudo make L.ERR_LIMIT sendTo chatId

                    return

                }

            }

            writePersist(userId, PERSIST_1)

            sudo makeHtml L.BOT_INPUT_TOKEN sendTo chatId

        }

    }

    override suspend fun onPersistMessage(userId: Int, chatId: Long, message: TdApi.Message, subId: Int) {

        val L = Lang.get(userId)

        removePersist(userId)

        if (subId == 0) {

            val botToken = message.text

            if (botToken == null) {

                onSendCanceledMessage(userId)

                return

            }

            httpSend(botToken, HttpGetMe()) {

                val botMe = it.user()

                val bot = BotData()

                bot.botId = botMe.id()

                if (BotImage.images.containsKey(bot.botId)) {

                    sudo make L.BOT_EXISTS sendTo chatId

                    return@httpSend

                }

                bot.userName = botMe.username()
                bot.botToken = botToken
                bot.owner = userId

                createCache[userId] = bot

                writePersist(userId, PERSIST_1, 1)

                sudo make {

                    inputText = L.BOT_N_TYPE

                    replyMarkup = keyboadButton {

                        textLine(L.BOT_TYPE_PM)

                    }

                } sendTo chatId

            } onError {

                sudo make L.BOT_TOKEN_INVALID.input(it.message) sendTo chatId

            }


        } else if (subId == 1) {

            val type = message.text

            val data = createCache.remove(userId)!!

            if (L.BOT_TYPE_PM == type) {

                data.type = 1

            } else {

                onSendCanceledMessage(userId)

                return

            }

            BotData.DATA.setById(data.botId, data)

            BotImage.start(data)

            sudo make {

                inputHtml = L.BOT_CREATED.input(data.userName)

                replyMarkup = removeKeyboard()

            } sendTo chatId

        }

    }

}