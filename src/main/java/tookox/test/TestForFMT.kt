package tookox.test

import tooko.td.TdApi
import tookox.core.client.TdBotHandler
import tookox.core.shift
import tookox.core.utils.asHtml
import tookox.core.utils.asMarkdown
import tookox.core.utils.make

class TestForFMT : TdBotHandler() {

    override fun onLoad() {

        initFunction("test_1")

    }

    override suspend fun onFunction(userId: Int, chatId: Long, message: TdApi.Message, function: String, param: String, params: Array<String>, originParams: Array<String>) {

        if ("html" == params[0]) {

            val html = originParams.shift().joinToString(" ")

            runCatching {

                sudo make html.asHtml syncTo chatId

            }.onFailure {

                sudo make it sendTo chatId

            }

        } else if ("md" == params[0]) {

            val md = originParams.shift().joinToString(" ")

            runCatching {

                sudo make md.asMarkdown syncTo chatId

            }.onFailure {

                sudo make it sendTo chatId

            }

        }

    }

}