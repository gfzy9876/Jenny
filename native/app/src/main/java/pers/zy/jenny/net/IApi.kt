package pers.zy.jenny.net

import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * @author: zy
 * @date: 2024/4/24
 */
interface IApi {
  @POST("command_string")
  @FormUrlEncoded
  suspend fun commandString(@Field("content") content: String): Response

  @POST("command_send_file_adb_pull")
  @FormUrlEncoded
  suspend fun commandSendFileAdbPull(@Field("content") content: String): Response

  @POST("command_send_image_byte")
  @Multipart
  suspend fun commandSendImageByte(@Part file: MultipartBody.Part): Response

  @POST("command_send_video_byte")
  @Multipart
  suspend fun commandSendVideoByte(@Part file: MultipartBody.Part): Response

  @GET("send_file_to_phone")
  suspend fun sendFileToPhone(): SendFileToPhoneResponse
}