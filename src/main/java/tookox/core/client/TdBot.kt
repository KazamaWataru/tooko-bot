package tookox.core.client

import cn.hutool.core.util.RuntimeUtil
import cn.hutool.core.util.StrUtil
import com.google.gson.Gson
import tooko.main.Env
import tooko.td.TdApi
import tooko.td.client.TdException
import java.io.File

open class TdBot(val botToken: String) : TdClient(initDataDir(botToken)), TdBotAbsHandler {

    override fun onAuthorizationState(authorizationState: TdApi.AuthorizationState) {

        super<TdClient>.onAuthorizationState(authorizationState)

        if (authorizationState is TdApi.AuthorizationStateWaitPhoneNumber) {

            log.trace("SEND BOT TOKEN")

            send(TdApi.CheckAuthenticationBotToken(botToken)) {

                isOk, _: TdApi.Object?, error ->

                log.trace("BOT LOGIN ${if (isOk) "OK" else "Failed"}")

                if (!isOk) {

                    onLoginFailed(TdException(error!!))

                }

            }

        }

    }

    override fun onEvent(event: TdApi.Object) {

        super<TdClient>.onEvent(event)

        log.debug("${event.javaClass.simpleName} : ${Gson().toJson(event)}")

    }

    companion object {

        private fun initDataDir(botToken: String): TdOptions {

            val dataDir = Env.getFile("data/" + StrUtil.subBefore(botToken, ":", false))

            dataDir.mkdirs()

            mkLink(dataDir, "stickers")
            mkLink(dataDir, "profile_photos")
            mkLink(dataDir, "thumbnails")
            mkLink(dataDir, "wallpapers")


            return TdOptions().databaseDirectory(dataDir.path)
        }

        private fun mkLink(dataDir: File, target: String) {

            val sourceDir = File(dataDir, target)

            val targetDir = Env.getFile("cache/files/$target")

            if (!sourceDir.isDirectory) {

                targetDir.mkdirs()

                RuntimeUtil.execForStr("ln -s " + targetDir.path + " " + sourceDir.path)

            }

        }

    }

}