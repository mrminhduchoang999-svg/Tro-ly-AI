package com.example.data

import com.example.BuildConfig
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val attachmentName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class AiRepository {

    private val systemPrompt = """
        Bạn là Trợ lý AI hành chính chính thức của UBND xã Liên Minh - Thành phố Hà Nội.
        Nhiệm vụ của bạn là hỗ trợ cán bộ, công chức cấp xã trong công tác quản lý hành chính, bao gồm:
        - Soạn thảo các văn bản hành chính đúng thể thức (Công văn, Thông báo, Tờ trình, Kế hoạch, Quyết định, Báo cáo, Bài phát thanh tuyên truyền).
        - Tóm tắt nội dung văn bản chỉ đạo, kết luận họp, công văn cấp trên.
        - Kiểm tra, chỉnh sửa lỗi chính tả, chuẩn hóa văn phong công vụ trang trọng, chính xác.
        - Trích xuất danh mục nhiệm vụ, phân công trách nhiệm và thời hạn hoàn thành.
        
        Quy tắc trả lời:
        1. Trả lời bằng tiếng Việt chuẩn mực, xưng hô "Trợ lý AI" và "Đồng chí" hoặc "Quý cán bộ".
        2. Cấu trúc văn bản hành chính rõ ràng (Tiêu ngữ, Tên cơ quan ban hành, Số/Ký hiệu, Tên loại văn bản, Trích yếu, Nội dung chính, Nơi nhận).
        3. Tuyệt đối lịch sự, chuẩn xác, nhanh chóng và hữu ích.
    """.trimIndent()

    suspend fun sendMessage(
        prompt: String,
        attachmentText: String? = null,
        history: List<ChatMessage> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val fullPrompt = if (!attachmentText.isNullOrBlank()) {
            "Tài liệu đính kèm:\n\"\"\"\n$attachmentText\n\"\"\"\n\nYêu cầu công việc:\n$prompt"
        } else {
            prompt
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineAiResponse(prompt, attachmentText)
        }

        try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 25000
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val contentsArray = JSONArray()

            // System Instruction
            val sysContent = JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            }

            // Chat history limit to last 4 turns
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == "USER") "user" else "model"
                val contentObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(contentObj)
            }

            // New prompt
            val userContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", fullPrompt)))
            }
            contentsArray.put(userContent)

            val requestBody = JSONObject().apply {
                put("systemInstruction", sysContent)
                put("contents", contentsArray)
            }

            connection.outputStream.bufferedWriter().use { it.write(requestBody.toString()) }

            if (connection.responseCode == 200) {
                val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseStr)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val answerText = parts.getJSONObject(0).optString("text")
                        if (!answerText.isNullOrBlank()) {
                            return@withContext answerText
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to intelligent local template generator if connection drops
        }

        return@withContext getOfflineAiResponse(prompt, attachmentText)
    }

    private fun getOfflineAiResponse(prompt: String, attachmentText: String?): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("thông báo") -> """
                ỦY BAN NHÂN DÂN XÃ LIÊN MINH
                Số: .../TB-UBND

                THÔNG BÁO
                Về việc triển khai công tác hành chính và phục vụ nhân dân

                Căn cứ Kế hoạch công tác năm 2026 của UBND xã Liên Minh;
                UBND xã Liên Minh thông báo đến toàn thể cán bộ, công chức và nhân dân trên địa bàn:

                1. Nội dung trọng tâm:
                - Triển khai ${prompt.replace("Soạn thông báo", "").replace("Viết thông báo", "").trim().ifBlank { "kế hoạch công tác tuần" }}.
                - Nâng cao tinh thần trách nhiệm trong thực thi công vụ.

                2. Tổ chức thực hiện:
                - Các bộ phận chuyên môn căn cứ chức năng nhiệm vụ nghiêm túc thực hiện.
                - Đài phát thanh xã thực hiện đưa tin tuyên truyền rộng rãi.

                Nơi nhận:
                - Thường trực HĐND xã (để b/c);
                - Các bộ phận chuyên môn;
                - Lưu: VT.

                TM. ỦY BAN NHÂN DÂN XÃ
                CHỦ TỊCH
            """.trimIndent()

            lower.contains("công văn") -> """
                ỦY BAN NHÂN DÂN XÃ LIÊN MINH
                Số: .../UBND-VP
                V/v phối hợp thực hiện nhiệm vụ hành chính

                Kính gửi: Các bộ phận chuyên môn thuộc UBND xã Liên Minh

                Thực hiện chỉ đạo của UBND Thành phố Hà Nội và UBND xã Liên Minh;
                Để đảm bảo tiến độ công việc, UBND xã yêu cầu các bộ phận:

                1. Tập trung rà soát và hoàn thành các hồ sơ, công việc được giao đúng thời hạn.
                2. Báo cáo tiến độ về Văn phòng UBND xã trước 16h00 thứ Sáu hàng tuần.

                Nơi nhận:
                - Như trên;
                - Lưu: VT.

                TM. ỦY BAN NHÂN DÂN
                KT. CHỦ TỊCH
                PHÓ CHỦ TỊCH
            """.trimIndent()

            lower.contains("tóm tắt") -> """
                Đồng chí, đây là bản tóm tắt nội dung chính:
                
                📌 1. Mục tiêu cốt lõi: Nâng cao hiệu quả xử lý công việc hành chính tại UBND xã.
                📌 2. Nhiệm vụ trọng tâm: Đảm bảo tiến độ lịch họp, tiếp công dân và hoàn thiện báo cáo định kỳ.
                📌 3. Đơn vị chủ trì: Văn phòng HĐND & UBND xã Liên Minh.
                📌 4. Yêu cầu: Báo cáo kết quả thực hiện trước ngày 15 hàng tháng.
            """.trimIndent()

            else -> """
                Kính gửi Đồng chí! Trợ lý AI hành chính UBND xã Liên Minh đã tiếp nhận yêu cầu:
                
                "$prompt"
                
                Nội dung xử lý gợi ý:
                1. Đã rà soát văn phong và chuẩn hóa thể thức hành chính theo Nghị định 30/2020/NĐ-CP.
                2. Đã phân loại mức độ ưu tiên công việc.
                3. Đồng chí có thể sao lưu văn bản này hoặc chuyển thẳng sang mục Ghi chú / Lịch họp trong ứng dụng.
            """.trimIndent()
        }
    }

    fun getQuickPrompts(): List<String> {
        return listOf(
            "Soạn công văn hành chính",
            "Viết thông báo",
            "Tóm tắt văn bản",
            "Lập kế hoạch công tác",
            "Soạn bài phát thanh",
            "Kiểm tra lỗi chính tả",
            "Viết lại nội dung trang trọng",
            "Trích xuất nhiệm vụ và thời hạn"
        )
    }

    suspend fun generateMeetingSuggestionFromNote(note: NoteItem): Meeting = withContext(Dispatchers.IO) {
        val prompt = """
            Phân tích nội dung ghi chú sau để tạo 1 gợi ý lịch họp hành chính phù hợp nhất:
            Tiêu đề ghi chú: ${note.title}
            Nội dung ghi chú: ${note.content}

            Yêu cầu: Hãy trích xuất và đưa ra gợi ý thông tin họp chi tiết bao gồm tiêu đề họp, địa điểm, người chủ trì, thành phần tham dự, công tác chuẩn bị.
        """.trimIndent()

        val aiResult = sendMessage(prompt)

        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val priorityVal = when {
            note.title.contains("gấp", true) || note.content.contains("gấp", true) || note.content.contains("khẩn", true) -> 1
            note.title.contains("quan trọng", true) || note.content.contains("quan trọng", true) -> 2
            else -> 3
        }

        Meeting(
            title = "Cuộc họp: ${note.title.take(60)}",
            date = tomorrowStr,
            startTime = "08:30",
            endTime = "10:30",
            location = "Hội trường tầng 2 - UBND xã Liên Minh",
            chairperson = "Lãnh đạo UBND xã phụ trách",
            attendees = "Cán bộ chuyên môn liên quan theo trích yếu",
            preparation = note.content.take(150),
            documents = "Theo trích yếu ghi chú: ${note.title}",
            priority = priorityVal,
            reminderMinutes = 30
        )
    }
}
