package com.sp.pointmore

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sp.pointmore.ui.theme.PointmoreTheme
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import java.util.Calendar
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

const val debug: Int = 1 //turn off if ur not gona need debuging
const val errornum: Long = -2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission(this)
        }
        enableEdgeToEdge()
        setContent {
            PointmoreTheme {
                val context = LocalContext.current
                var screenTime by remember { mutableStateOf<Long?>(-2) }

                LaunchedEffect(Unit) {
                    if (hasUsageStatsPermission(context)) {
                        screenTime = getTotalScreentime(context)
                    }
                }
                var truescreenTime by remember { mutableStateOf<Long?>(-2) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (screenTime != errornum && debug == 1) {
                        Text("\nDebug Stats:")
                        Text("\ntotalScreenUsage: ${screenTime!! / (1000 * 60 * 60)} hours")
                        truescreenTime = screenTime!! / (1000 * 60 * 60)
                        Text("\ntotaltrueScreenUsage: ${truescreenTime} hours")
                    } else if (!hasUsageStatsPermission(this@MainActivity)) {
                        Text("\n\nUsage Access permission not granted.")
                    }

                    guiMain(truescreenTime!!)

                }

            }
        }
    }
}

fun getTotalScreentime(context: Context): Long {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val now = System.currentTimeMillis()
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val events = usageStatsManager.queryEvents(startOfDay, now)
    var lastForegroundTimestamp: Long? = null
    var totalTime = 0L

    val event = UsageEvents.Event()
    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            lastForegroundTimestamp = event.timeStamp
        } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && lastForegroundTimestamp != null) {
            totalTime += event.timeStamp - lastForegroundTimestamp!!
            lastForegroundTimestamp = null
        }
    }

    return totalTime
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode", showSystemUi = true)
@Composable
fun guiMain(sts: Long = 0) {
    PointmoreTheme(
        darkTheme = true
    ) {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background) // if i dont got ts set here then well it doesnt work idky
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text( color = MaterialTheme.colorScheme.primary,
                text = buildAnnotatedString {

                    append("Points: ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
                        val points = 24 - sts
                        append("${points}")
                    }
                    append(" / 24")

                    append("\nWhich is ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
                        append("${sts}")
                    }
                    append(" hours of screentime today.")

                    if (sts < 4) {
                        append("\nGood job in not going over ${sts + 1} hours of screentime today!")

                    }

                }
            )
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}
