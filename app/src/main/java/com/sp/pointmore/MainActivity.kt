package com.sp.pointmore

import android.app.AppOpsManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sp.pointmore.ui.theme.PointmoreTheme
import android.app.usage.UsageStatsManager.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import java.util.Calendar
import androidx.compose.runtime.*
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

val debug: Int = 1 //turn off if ur not gona need debuging
var errornum: Long = -2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission(this)
        }
        enableEdgeToEdge()
        setContent {
            PointmoreTheme {
                var screenTime by remember { mutableStateOf<Long?>(-2) }

                LaunchedEffect(Unit) {
                    if (hasUsageStatsPermission(this@MainActivity)) {
                        screenTime = getTotalScreentime(this@MainActivity)
                    }
                }
                var truescreenTime by remember { mutableStateOf<Long?>(-2) }
                Column {
                    if (screenTime != errornum && debug == 1) {
                        Text("\nDebug Stats:")
                        Text("\ntotalScreenUsage: ${screenTime!! / (1000 * 60 * 60)} hours")
                        truescreenTime = screenTime!! / (1000 * 60 * 60)
                        Text("\ntotaltrueScreenUsage: ${truescreenTime} hours")
                    } else if (!hasUsageStatsPermission(this@MainActivity)) {
                        Text("\n\nUsage Access permission not granted.")
                    }


                }
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        buildAnnotatedString {

                            append("Points: ")

                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
                                val points = 24 - truescreenTime!!
                                append("${points}")
                            }
                            append(" / 24")

                            append("\nWhich is ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
                                append("${truescreenTime}")
                            }
                            append(" hours of screentime today.")

                            if (truescreenTime!! < 4) {
                                append("\nGood job in not going over 4 hours of screentime today!")

                            }

                        }
                    )
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
    }.timeInMillis

    return usageStatsManager
        .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
        ?.sumOf { it.totalTimeInForeground } ?: 0L
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun guiMain() {



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
