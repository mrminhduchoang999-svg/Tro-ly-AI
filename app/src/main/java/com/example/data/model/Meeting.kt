package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // Format: YYYY-MM-DD
    val startTime: String, // Format: HH:mm
    val endTime: String = "", // Format: HH:mm
    val location: String = "Phòng họp UBND xã",
    val chairperson: String = "Lãnh đạo UBND xã", // Người chủ trì
    val attendees: String = "Thành phần theo Mời", // Thành phần tham dự
    val preparation: String = "", // Nội dung chuẩn bị
    val documents: String = "", // Tài liệu liên quan
    val priority: Int = 2, // 1: Cao (Đỏ), 2: Thường (Xanh), 3: Chuẩn bị (Vàng)
    val reminderMinutes: Int = 15, // 5, 15, 30, 60, 1440
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
