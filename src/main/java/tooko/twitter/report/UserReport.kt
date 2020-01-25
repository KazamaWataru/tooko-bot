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

package tooko.twitter.report

import cn.hutool.core.util.NumberUtil
import kotlinx.coroutines.*
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import td.TdApi
import tooko.core.client.TdBotHandler
import tooko.core.env.Fn
import tooko.core.input
import tooko.core.utils.make
import tooko.lottie.mkDriver
import tooko.lottie.waitForId
import tooko.twitter.AuthToken
import twitter4j.Paging
import twitter4j.Status
import java.util.*

class UserReport : TdBotHandler() {

    val reportStatus = "https://twitter.com/i/safety/report_story?next_view=report_story_start&source=reporttweet&reported_tweet_id={}&reported_user_id={}&is_media={}&is_promoted=false&client_location=home_latest%3A%3A&client_app_id=258901&lang=zh-CN"

    override fun onLoad() {

        initFunction("report", "report_hateful")

    }

    fun newTwitterDriver(authToken: String): ChromeDriver {

        val driver = mkDriver(true)

        driver.get("https://mobile.twitter.com/")

        driver.manage().addCookie(Cookie("auth_token", authToken, ".twitter.com", "/",
                Date(System.currentTimeMillis() + 5 * 365 * 24 * 60 * 60 * 1000L)
                , true, true))

        return driver

    }

    fun reportStatus(driver: WebDriver, status: Status, hateful: Boolean = false) {

        driver.runCatching {

            val media = status.mediaEntities.isNotEmpty()

            get(reportStatus.input(status.id, status.user.id, media))

            waitForId("abuse-btn").click()

            if (hateful) {

                waitForId("hateful-btn").click()

                waitForId("group-victim-btn").click()

            } else {

                waitForId("harassing-btn").click()

                waitForId("someone-else-btn").click()

            }

            waitForId("attach_tweets").click()

        }

    }

    override suspend fun onFunction(userId: Int, chatId: Long, message: TdApi.Message, function: String, param: String, params: Array<String>, originParams: Array<String>) {

        if (params.isEmpty()) {

            sudo make "/$function <userId/screenName>..." sendTo chatId

            return

        }

        val hateful = function.endsWith("hateful")

        params.forEach {

            val tokens = AuthToken.getByOwner(userId)
            val apis = tokens.map { it.mkApi() }

            val drivers = tokens.map {

                GlobalScope.async {

                    newTwitterDriver(it.authToken)

                }

            }.awaitAll()

            val target = runCatching {

                if (NumberUtil.isLong(it)) {

                    apis[0].showUser(it.toLong())

                } else {

                    apis[0].showUser(Fn.parseScreenName(it))

                }

            }.onFailure {

                sudo make it sendTo chatId

            }.getOrNull() ?: return

            val timeline = apis[0].getUserTimeline(it, Paging().count(200))

            val status = sudo make "reporting 0 / ${timeline.size}..." syncTo chatId

            timeline.forEachIndexed { index, s ->

                val deferreds = LinkedList<Deferred<Unit>>()

                drivers.forEach { driver ->

                    deferreds.add(GlobalScope.async {

                        reportStatus(driver, s, hateful)

                    })

                }

                deferreds.awaitAll()

                sudo make "reporting ${index + 1} / ${timeline.size}..." editTo status

                delay(3 * 1000L)

            }

            sudo make "reported ${timeline.size} status" editTo status

        }

    }

}