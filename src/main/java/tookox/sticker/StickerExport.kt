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

package tookox.sticker

import cn.hutool.core.img.ImgUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.RuntimeUtil
import cn.hutool.core.util.ZipUtil
import kotlinx.coroutines.*
import td.TdApi.*
import tookox.core.*
import tookox.core.client.*
import tookox.core.env.*
import tookox.core.utils.*
import java.awt.Color
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class StickerExport : TdBotHandler() {

    override fun onLoad() = initData(DATA_1)

    override suspend fun onNewMessage(userId: Int, chatId: Long, message: Message) {

        if (!message.fromPrivate || message.content !is MessageSticker) return

        val sticker = (message.content as MessageSticker).sticker

        runCatching {

            sudo make UploadingPhoto syncTo chatId

            var stickerFile = sticker.sticker

            if (!stickerFile.local.isDownloadingCompleted) {

                stickerFile = sync(DownloadFile(stickerFile.id, 1, 0, 0, true))

            }

            val L = Lang.get(userId)

            sudo make {

                if (!sticker.isAnimated) {

                    inputPhoto = stickerFile.local.path!!

                } else {

                    val cache = Env.getFile("cache/tgs_gif_cache/${stickerFile.remote.uniqueId}.gif")

                    if (!cache.isFile) {

                        try {

                            RuntimeUtil.exec("tgsconvert.py ${stickerFile.local.path!!} $cache").waitFor()

                        } catch (ex: Exception) {

                            sudo make "${ex.message}" sendTo chatId

                            return

                        }

                    }

                    inputVideo = cache.path

                }

                captionHtml = L.STICKER_CAPTION.input(stickerFile.remote.id!!, sticker.emoji, sticker.setId)

                replyMarkup = inlineButton {

                    dataLine(L.STICKER_EXPORT, DATA_1, 0, sticker.setId.asByteArray)

                }

            } sendTo chatId

        }.onFailure {

            sudo make it sendTo chatId

        }

    }

    override suspend fun onNewCallbackQuery(userId: Int, chatId: Long, messageId: Long, queryId: Long, subId: Int, data: Array<ByteArray>) = coroutineScope {

        val L = Lang.get(userId)

        if (subId == 0) {

            launch(Dispatchers.Unconfined) {

                runCatching {

                    val set = sync<StickerSet>(GetStickerSet(data[0].asLong))

                    sudo makeInlineButton null at chatId editTo messageId

                    val stat = sudo make {

                        inputHtml = L.STICKER_EXPORT_DL.input(set.title, 0, set.stickers.size)

                    } syncTo chatId

                    val cachePath = File(Env.getPath("cache/stickers_pack_export/" + set.id))

                    val zip = File(cachePath, set.title + ".zip")

                    if (!zip.isFile) {

                        val count = AtomicInteger(0)

                        val deferreds = LinkedList<Deferred<*>>()

                        for (index in set.stickers.indices) {

                            val sticker: Sticker = set.stickers[index]

                            val stickerFile = sticker.sticker

                            if (stickerFile.local.isDownloadingCompleted) continue

                            async {

                                sticker.sticker = sync(DownloadFile(stickerFile.id, 1, 0, 0, true))

                                val c = count.incrementAndGet()

                                sudo make {

                                    inputHtml = L.STICKER_EXPORT_DL.input(set.title, c, set.stickers.size)

                                } syncEditTo stat

                            }.also {

                                deferreds.add(it)

                            }

                        }

                        deferreds.awaitAll()

                        sudo make L.STICKER_EXPORT_PACK syncEditTo stat

                        val cacheDir = File(cachePath, set.title)

                        File(cacheDir, "src").mkdirs()

                        for (index in set.stickers.indices) {

                            val stickerFile = set.stickers[index].sticker

                            val localFile = FileUtil.file(stickerFile.local.path)

                            if (!localFile.isFile) continue

                            val src = File(cacheDir, "src/$index.webp")

                            FileUtil.copy(localFile, src, true)

                            runCatching {

                                val image = ImgUtil.read(src)

                                val img = Img(image.width, image.height, Color.WHITE)

                                img.drawImage(0, 0, image, image.width, image.height)

                                img.toFile(File(cacheDir, "$index.jpg"), "jpg")

                            }.onFailure {

                                File(cacheDir, "README.txt").appendText("sticker $index export field : $it\n\n")

                            }

                        }

                        ZipUtil.zip(cacheDir.path, zip.path, true)

                        FileUtil.del(cacheDir)

                    }

                    sudo make L.STICKER_EXPORT_SEND syncEditTo stat

                    sudo make UploadingPhoto syncTo chatId

                    sudo make {

                        inputFile = zip.absolutePath

                        captionText = "https://t.me/addstickers/${set.name}"

                    } syncTo chatId

                    sudo delete stat

                }.onFailure {

                    sudo makeAlert L.STICKER_DL_FAILED.input(it) answerTo queryId

                }

            }

        }

    }

}