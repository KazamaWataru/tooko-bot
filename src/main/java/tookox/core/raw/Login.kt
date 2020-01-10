@file:Suppress(
    "unused"
)

package tookox.core.raw

import kotlinx.coroutines.*
import td.TdApi.*
import tookox.core.client.*

/**
 * Returns information about a button of type inlineKeyboardButtonTypeLoginUrl
 * The method needs to be called when the user presses the button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 */
suspend fun TdAbsHandler.getLoginUrlInfo(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0
) = sync<LoginUrlInfo>(
    GetLoginUrlInfo(
        chatId,
        messageId,
        buttonId
    )
)

/**
 * Returns information about a button of type inlineKeyboardButtonTypeLoginUrl
 * The method needs to be called when the user presses the button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 */
suspend fun TdAbsHandler.getLoginUrlInfoOrNull(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0
) = syncOrNull<LoginUrlInfo>(
    GetLoginUrlInfo(
        chatId,
        messageId,
        buttonId
    )
)

/**
 * Returns information about a button of type inlineKeyboardButtonTypeLoginUrl
 * The method needs to be called when the user presses the button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 */
fun TdAbsHandler.getLoginUrlInfo(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0,
    block: (suspend CoroutineScope.(LoginUrlInfo) -> Unit)
) = send(
    GetLoginUrlInfo(
        chatId,
        messageId,
        buttonId
    ),block = block
)

/**
 * Returns an HTTP URL which can be used to automatically authorize the user on a website after clicking an inline button of type inlineKeyboardButtonTypeLoginUrl
 * Use the method getLoginUrlInfo to find whether a prior user confirmation is needed
 * If an error is returned, then the button must be handled as an ordinary URL button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 * @allowWriteAccess - True, if the user allowed the bot to send them messages
 */
suspend fun TdAbsHandler.getLoginUrl(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0,
    allowWriteAccess: Boolean = false
) = sync<HttpUrl>(
    GetLoginUrl(
        chatId,
        messageId,
        buttonId,
        allowWriteAccess
    )
)

/**
 * Returns an HTTP URL which can be used to automatically authorize the user on a website after clicking an inline button of type inlineKeyboardButtonTypeLoginUrl
 * Use the method getLoginUrlInfo to find whether a prior user confirmation is needed
 * If an error is returned, then the button must be handled as an ordinary URL button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 * @allowWriteAccess - True, if the user allowed the bot to send them messages
 */
suspend fun TdAbsHandler.getLoginUrlOrNull(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0,
    allowWriteAccess: Boolean = false
) = syncOrNull<HttpUrl>(
    GetLoginUrl(
        chatId,
        messageId,
        buttonId,
        allowWriteAccess
    )
)

/**
 * Returns an HTTP URL which can be used to automatically authorize the user on a website after clicking an inline button of type inlineKeyboardButtonTypeLoginUrl
 * Use the method getLoginUrlInfo to find whether a prior user confirmation is needed
 * If an error is returned, then the button must be handled as an ordinary URL button
 *
 * @chatId - Chat identifier of the message with the button
 * @messageId - Message identifier of the message with the button
 * @buttonId - Button identifier
 * @allowWriteAccess - True, if the user allowed the bot to send them messages
 */
fun TdAbsHandler.getLoginUrl(
    chatId: Long = 0L,
    messageId: Long = 0L,
    buttonId: Int = 0,
    allowWriteAccess: Boolean = false,
    block: (suspend CoroutineScope.(HttpUrl) -> Unit)
) = send(
    GetLoginUrl(
        chatId,
        messageId,
        buttonId,
        allowWriteAccess
    ),block = block
)
