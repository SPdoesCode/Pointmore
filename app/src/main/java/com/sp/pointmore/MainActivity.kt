@file:Suppress("DEPRECATION")

package com.sp.pointmore

import android.app.AppOpsManager
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.unit.dp

const val debug: Boolean = false

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
                var screenTime by remember { mutableLongStateOf(2222222222222222222)}

                LaunchedEffect(Unit) {
                    if (hasUsageStatsPermission(context)) {
                        screenTime = getTotalScreenTime(context)
                    }
                }
                val truescreenTime = (screenTime) / (1000 * 60 * 60)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSecondary)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (debug) {
                        Text("\nDebug Stats:")
                        Text("\nVal of screenTime: $screenTime}")
                        Text("\nval of truescreentime: $truescreenTime")
                    } else if (!hasUsageStatsPermission(context)) {
                        Text("\n\nUsage Access permission not granted.")
                    }

                    GuiMain(truescreenTime)

                }

            }
        }
    }
}

fun getTotalScreenTime(context: Context): Long {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val now = System.currentTimeMillis()
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startOfDay,
        now
    )

    var totalTime = 0L
    for (usageStat in stats) {
        totalTime += usageStat.totalTimeInForeground
    }

    return totalTime
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode", showSystemUi = true)
@Composable
fun GuiMain(sts: Long = 0) {
    val maxPoints = 24
    val points = maxPoints - sts
    PointmoreTheme(
        darkTheme = true
    ) {
        Box (
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSecondary)
                .fillMaxSize(),
            contentAlignment = Alignment.Center

        ) {
            Card(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onSecondary)
                    .padding(34.dp)
                    .size(width = 240.dp, height = 102.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = buildAnnotatedString {

                        append("Points: ")

                        if (sts >= 4) {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            ) {
                                append("$points")
                            }
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            ) {
                                append("$points")
                            }
                        }
                        append(" / $maxPoints")

                        append("\nWhich is ")
                        if (sts >= 4) {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            ) {
                                append("$sts")
                            }
                        } else {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            ) {
                                append("$sts")
                            }
                        }
                        append(" hours of screentime today.\n(Only counts time out of the ap!)")

                        if (sts < 4) {
                            append("\nGood job in not going over ${sts + 1} hours of screentime today!")

                        }

                    }
                )
            }
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
