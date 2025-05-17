package com.example.hackaton

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

@Serializable
data class PlanEntry(val time: String, val activity: String, val type: String, val tasktype: String)

fun parsePlan(json: String): List<PlanEntry> {
    val tree = Json.parseToJsonElement(json).jsonObject
    val arr = tree["SOBOTA"]?.toString() ?: "[]"
    return Json.decodeFromString(arr)
}

@Composable
fun DayPlanTimeline(entries: List<PlanEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color = Color(0xFF6200EE), shape = CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = entry.time, fontSize = 14.sp, color = Color.Gray)

                    if (entry.type=="misc") {
                        Text(
                            text = entry.activity, fontSize = 16.sp,
                            modifier = Modifier.background(
                                color = Color.LightGray, shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }else if (entry.type=="user-defined") {
                        Text(
                            text = entry.activity, fontSize = 16.sp,
                            modifier = Modifier.background(
                                color = Color.Magenta, shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }else {
                        Text(text = entry.tasktype, fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = entry.activity, fontSize = 16.sp,
                            modifier = Modifier.background(
                                color = Color.Green, shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun CreatePlan(json: String) {
    val plan = remember(json) { parsePlan(json) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DayPlanTimeline(entries = plan)
        }
    }
}