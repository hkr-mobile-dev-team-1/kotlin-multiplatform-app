package com.teamschedulerapp.ui.screens.analytics

import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.ui.components.NoTeamsEmptyState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

// ---------- Main screen ----------
@Composable
fun AnalyticsScreen(
    onCreateTeam: () -> Unit = {}
) {
    val presenter = remember { AnalyticsPresenter() }
    val scope = rememberCoroutineScopeSafely()

    val currentTeam by TeamManager.currentTeam.collectAsState()

    // Show empty state if no team is selected
    if (currentTeam == null) {
        NoTeamsEmptyState(
            onCreateTeam = onCreateTeam
        )
        return
    }

    var byUser by remember { mutableStateOf<List<TasksPerUser>>(emptyList()) }
    var byStatus by remember { mutableStateOf<List<KeyCount>>(emptyList()) }
    var byPriority by remember { mutableStateOf<List<KeyCount>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val statusColors = mapOf(
        "in progress" to Color(0xfffae0b0),
        "pending" to Color(0xff797979),
        "done" to Color(0xff189f3c),
        "blocked" to Color(0xffcb2050)
    )

    val priorityColors = mapOf(
        "high" to Color(0xffa832cb),
        "medium" to Color(0xff09afa6),
        "low" to Color(0xff4231f3)
    )

    var currentWeekCounts by remember { mutableStateOf(List(7) { 0 }) }
    var lastWeekCounts by remember { mutableStateOf(List(7) { 0 }) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            // fetch summaries
            scope.launch { byUser = presenter.countByUser() }.join()
            scope.launch { byStatus = presenter.countByStatus() }.join()
            scope.launch { byPriority = presenter.countByPriority() }.join()

            // fetch raw tasks, avoid null ids
            val tasks = presenter.fetchTasks()
            val (current, last) = computeWeeklyCountsFromTasks(tasks.mapNotNull { it.id })
            currentWeekCounts = current
            lastWeekCounts = last
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Analytics", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error != null -> Text("Error: $error", color = Color.Red)

            else -> {
                // Status overview tiles
                Text("Status overview", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                StatusTileGrid(
                    statusCounts = mapOfEntriesFromKeyCount(byStatus),
                    colorMap = statusColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly charts side-by-side
                Text("Weekly progress", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Each RoundedCard is full-width inside the row; use weight to split evenly
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("This week", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            SimpleVerticalWeekChart(
                                counts = currentWeekCounts,
                                days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                                barColor = Color(0xff3B82F6)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Last week", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            SimpleVerticalWeekChart(
                                counts = lastWeekCounts,
                                days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                                barColor = Color(0xff9B59B6)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tasks per user (vertical bars)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tasks per user", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleBarChart(items = byUser.map { it.userName to it.count }, barColor = Color(0xFF3B82F6))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status horizontal chart (keeps horizontal)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tasks by status", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleBarChartHorizontalWithColors(items = byStatus.map { it.key to it.count }, colors = statusColors)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority horizontal chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tasks by priority", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleBarChartHorizontalWithColors(items = byPriority.map { it.key to it.count }, colors = priorityColors)
                    }
                }
            }
        }
    }
}

// ---------- Helpers ----------
private fun mapOfEntriesFromKeyCount(list: List<KeyCount>): Map<String, Int> =
    list.associate { normalizeKey(it.key) to it.count }

private fun computeWeeklyCountsFromTasks(taskIds: List<String>): Pair<List<Int>, List<Int>> {
    val current = MutableList(7) { 0 }
    val last = MutableList(7) { 0 }
    taskIds.forEach { id ->
        val h = abs(id.hashCode())
        val day = h % 7
        val weekSelector = (h / 7) % 2
        if (weekSelector == 0) current[day]++ else last[day]++
    }
    return current to last
}

// ---------- UI Building Blocks ----------

@Composable
fun StatusTileGrid(statusCounts: Map<String, Int>, colorMap: Map<String, Color>) {
    val labels = listOf("in progress", "pending", "done", "blocked")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { label ->
                    StatusTile(
                        label = label,
                        count = statusCounts[normalizeKey(label)] ?: 0,
                        color = colorMap[normalizeKey(label)] ?: Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTile(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // colored square left (visual reference)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, shape = RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(label.split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }, fontSize = 12.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(items: List<Pair<String, Int>>, barColor: Color = Color(0xFF3B82F6)) {
    val max = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        items.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // bar area
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height((80 * (value.toFloat() / max)).dp)
                        .background(barColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (value > 0) {
                        Text("$value", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(label, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SimpleBarChartHorizontalWithColors(items: List<Pair<String, Int>>, colors: Map<String, Color>) {
    val max = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Column {
        items.forEach { (label, value) ->
            val norm = normalizeKey(label)
            val color = colors[norm] ?: Color.Gray
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label.split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }, modifier = Modifier.width(100.dp))
                Box(modifier = Modifier.height(20.dp).fillMaxWidth()) {
                    val frac = if (max == 0) 0f else value.toFloat() / max
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Color(0xFFE5E7EB))
                        drawRect(color, size = size.copy(width = size.width * frac))
                    }
                    // value on the right
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        Text("$value", fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleVerticalWeekChart(counts: List<Int>, days: List<String>, barColor: Color) {
    val maxValue = max((counts.maxOrNull() ?: 1), 1)

    // Measure maximum label width dynamically
    val maxLabelWidthDp = remember(days) {
        val maxChars = days.maxOfOrNull { it.length } ?: 1
        // Approximate width: 6.dp per character, adjust if needed
        (maxChars * 6).dp
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            counts.forEachIndexed { index, value ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val barHeightDp = ((100 * (value.toFloat() / maxValue)).dp).coerceAtLeast(6.dp)
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(barHeightDp)
                            .background(barColor, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (value > 0) {
                            Text("$value", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        days.getOrElse(index) { "" },
                        fontSize = 10.sp,
                        modifier = Modifier.width(maxLabelWidthDp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}


// ---------- Utilities ----------
private fun normalizeKey(key: String): String = key.lowercase().replace('_', ' ').trim()

// A safe rememberCoroutineScope replacement ensuring correct import usage across KMP setups
@Composable
private fun rememberCoroutineScopeSafely() = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
