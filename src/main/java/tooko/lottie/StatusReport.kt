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

package tooko.lottie

import kotlinx.coroutines.*
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import tooko.core.env.Fn
import tooko.core.input
import twitter4j.Paging
import twitter4j.Status
import java.util.*


object StatusReport {

    val profileExt = "//*[@id=\"react-root\"]/div/div/div/main/div/div/div/div/div/div[2]/div/div/div[1]/div/div[1]/div/div[1]/div"
    val profileReport = "//*[@id=\"react-root\"]/div/div/div[1]/div/div/div[2]/div[3]/div/div/div/a[5]"

    val statusAndReply = "//*[@id=\"react-root\"]/div/div/div/main/div/div/div/div/div/div[2]/div/div/nav/div[2]/div[2]/a"

    val reportStatus = "https://twitter.com/i/safety/report_story?next_view=report_story_start&source=reporttweet&reported_tweet_id={}&reported_user_id={}&is_media={}&is_promoted=false&client_location=home_latest%3A%3A&client_app_id=258901&lang=zh-CN"
    val reportProfile = "https://twitter.com/i/safety/report_story?next_view=report_story_start&source=reportprofile&reported_user_id={}&is_media=false&is_promoted=false&client_location=home%3A%3A&client_app_id=258901&lang=zh-CN"

    val firstStatus = "//*[@id=\"MultiTweetReport-tweetList\"]/li[1]/div/div/span/input"
    val submitStatus = "//*[@id=\"attach_tweets\"]"

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val apiKey = "z6FQzXfFNeiEr9XmYuJTxD9Qp"
        val apiSecKey = "ALzp3YJ2uPQHyxfrNxNGfQRQNKKtiQTYHvw30mwKe3NueO0DnY"
        val accessToken = "<hide>"
        val accessTokenSecret = "<hide>"

        val accountId = accessToken.substringBefore("-").toLong()

        val api = Fn.mkApi(apiKey, apiSecKey, accessToken, accessTokenSecret)

        val authTokens = listOf(

                "<hide>"

        )

        val drivers = authTokens.map { newTwitterDriver(it) }

        var skip = 0

        api.getFollowersIDs("CHNST_", -1).iDs.forEach {

            val user = api.showUser(it)

            if (user.isProtected ||
                    user.isVerified ||
                    user.followersCount > 10000
            ) return@forEach

            if (skip > 0) {

                skip--

                return@forEach

            }

            val timeline = api.getUserTimeline(it, Paging().count(50))

            timeline.forEach {

                val deferreds = LinkedList<Deferred<Unit>>()

                drivers.forEach { driver ->

                    deferreds.add(GlobalScope.async {

                        driver.reportStatuses(it)

                    })

                }

                deferreds.awaitAll()

                delay(5 * 1000L)

            }

        }

    }

    fun newTwitterDriver(authToken: String): ChromeDriver {

        val driver = mkDriver(true)

        driver.get("https://mobile.twitter.com/")

        driver.manage().addCookie(Cookie("auth_token", authToken, ".twitter.com", "/",
                Date(System.currentTimeMillis() + 5 * 365 * 24 * 60 * 60 * 1000L)
                , true, true))

        driver.get("https://mobile.twitter.com/")

        return driver

    }

    fun WebDriver.reportStatuses(status: Status) {

        runCatching {

            val media = status.mediaEntities.isNotEmpty()

            get(reportStatus.input(status.id, status.user.id, media))

            waitForId("abuse-btn").click()

            /*

        waitForId("hateful-btn").click()

        waitForId("group-victim-btn").click()

         */

            waitForId("harassing-btn").click()

            waitForId("someone-else-btn").click()

            waitForId("attach_tweets").click()

        }

    }

}