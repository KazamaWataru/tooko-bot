package tooko.twitter.follow

import cn.hutool.core.date.DateUtil
import cn.hutool.log.LogFactory
import tooko.Launcher
import tooko.main.Fn
import tooko.main.Lang
import tooko.twitter.TwitterAccount
import tooko.twitter.actions.TrackTask
import tooko.twitter.archives.UserA
import tooko.twitter.spam.PredictProcess
import tooko.twitter.spam.UserR
import twitter4j.Paging
import twitter4j.Twitter
import twitter4j.TwitterException
import java.util.*

class FollowTask : TimerTask() {

    val log = LogFactory.get("Follow")

    override fun run() {

        TwitterAccount.DATA.getAllByField("follow", true).forEach {

            fetchInfo(it, it.mkApi())

        }

    }

    companion object {

        private var timer: Timer? = null

        fun start() {

            stop()

            timer = Timer("Twitter FC Task").apply {

                scheduleAtFixedRate(TrackTask(), DateUtil.tomorrow(), 24 * 60 * 60 * 1000L)

            }

        }

        fun stop() {

            timer?.cancel()

            timer = null

        }

    }

    fun fetchInfo(account: TwitterAccount, api: Twitter) {

        try {

            val queue: HashSet<Long> = LinkedHashSet()

            api.getHomeTimeline(Paging().count(200)).forEach {

                queue.add(it.user.id)

                if (it.inReplyToUserId > 0) {

                    queue.add(it.inReplyToStatusId)

                }

            }

            val iter = queue.iterator()

            while (iter.hasNext()) {

                val accountId = iter.next()

                if (TrackTask.friends.arrayIsIn(account.accountId, "array", accountId) || TrackTask.followers.arrayIsIn(account.accountId, "array", accountId) || AutoData.DATA.arrayIsIn(account.accountId, "autoFollowedIDs", accountId)) {

                    iter.remove()

                }

            }

            Fn.fetchUsers(api, queue).forEach {

                val archive = UserA.save(it)

                if (PredictProcess.predict(api, UserR.predictUser(archive))) {

                    return@forEach

                }

                if (archive.friends > 500 && archive.followers < 20) return@forEach

                try {

                    api.createFriendship(archive.accountId)

                    Launcher.twitter.postHtml(account.owner.toLong(), "Followed {}", archive.parseInfo(Lang.get(account.owner)))

                    return

                } catch (ex: TwitterException) {

                    return@forEach

                }


            }

        } catch (e: TwitterException) {

            log.warn(e)

        }
    }

}