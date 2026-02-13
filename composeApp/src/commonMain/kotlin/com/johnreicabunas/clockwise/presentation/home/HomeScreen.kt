package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.presentation.components.AdBanner
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val deviceZone = TimeZone.currentSystemDefault()

    var now by remember { mutableStateOf(Clock.System.now()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            now = Clock.System.now()
            delay(1000)
        }
    }

    val filteredZones = remember(state.zones, searchQuery) {
        if (searchQuery.isBlank()) {
            state.zones
        } else {
            state.zones.filter {
                it.name.contains(searchQuery, true) ||
                        it.country.contains(searchQuery, true) ||
                        it.zoneId.contains(searchQuery, true)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "World Clock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Compare timezones worldwide",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F6FA)
                )
            )
        },
        bottomBar = {
            AdBanner()
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                placeholder = {
                    Text("Search city or country...", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEDEFF5),
                    unfocusedContainerColor = Color(0xFFEDEFF5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF5B5FC7)
                )
            )

            HeaderSection(deviceZone, now)

            Spacer(Modifier.height(16.dp))

            Text(
                "${filteredZones.size} TIMEZONES",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredZones) { zone ->
                    WorldClockItem(
                        timeZone = zone,
                        now = now,
                        deviceZone = deviceZone,
                        isExpanded = state.expandedZone == zone.zoneId,
                        onClick = { viewModel.onZoneClicked(zone.zoneId) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    deviceZone: TimeZone,
    now: Instant
) {
    val deviceTime = now.toLocalDateTime(deviceZone)
    val deviceDate = deviceTime.formatDate()

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1E2A47),
                            Color(0xFF3E3C8F)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("YOUR TIMEZONE",
                            fontSize = 11.sp,
                            color = Color.White.copy(.7f)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        deviceZone.id.substringAfterLast("/").replace("_", " "),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        deviceDate,
                        fontSize = 13.sp,
                        color = Color.White.copy(.85f)
                    )
                }

                Text(
                    deviceTime.formatTime(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WorldClockItem(
    timeZone: TimeZoneModel,
    now: Instant,
    deviceZone: TimeZone,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val cityZone = TimeZone.of(timeZone.zoneId)
    val deviceTime = now.toLocalDateTime(deviceZone)
    val cityTime = now.toLocalDateTime(cityZone)
    val cityDate = cityTime.formatDate()

    val diffHours =
        (cityZone.offsetAt(now).totalSeconds -
                deviceZone.offsetAt(now).totalSeconds) / 3600

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                Color(0xFFF8F9FF) else Color.White
        ),
        border = if (isExpanded) {
            BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF5B5FC7).copy(alpha = 0.3f),
                        Color(0xFF8A8ED9).copy(alpha = 0.3f)
                    )
                )
            )
        } else null
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isExpanded)
                                    Color(0xFF5B5FC7).copy(alpha = 0.12f)
                                else Color(0xFFEDEFF5),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            timeZone.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isExpanded) Color(0xFF1E2A47) else Color.Black
                        )
                        Text(
                            timeZone.country,
                            fontSize = 12.sp,
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            cityTime.formatTime(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Black
                        )
                        Text(
                            "UTC${formatUtcOffset(cityZone, now)}",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(
                                    if (isExpanded)
                                        Color(0xFF5B5FC7).copy(alpha = 0.1f)
                                    else Color(0xFFEDEFF5),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF5B5FC7).copy(alpha = 0.2f),
                                    Color(0xFF8A8ED9).copy(alpha = 0.2f)
                                )
                            )
                        )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Schedule),
                            contentDescription = null,
                            tint = Color(0xFF5B5FC7),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                "Your Time",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                deviceTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color(0xFF1E2A47) else Color.Black
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (diffHours >= 0)
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Icon(
                                painter = rememberVectorPainter(
                                    if (diffHours >= 0)
                                        Icons.Default.ArrowUpward
                                    else Icons.Default.ArrowDownward
                                ),
                                contentDescription = null,
                                tint = if (diffHours >= 0)
                                    Color(0xFF4CAF50)
                                else Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "${if (diffHours >= 0) "+" else ""}${diffHours}h",
                                fontWeight = FontWeight.Bold,
                                color = if (diffHours >= 0)
                                    Color(0xFF4CAF50)
                                else Color(0xFFF44336)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                timeZone.name,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                cityTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color(0xFF5B5FC7) else Color.Black
                            )
                        }
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.LocationOn),
                            contentDescription = null,
                            tint = Color(0xFF5B5FC7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        cityDate,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

fun LocalDateTime.formatTime(): String {
    val hour12 = ((hour + 11) % 12) + 1
    val amPm = if (hour < 12) "AM" else "PM"

    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

fun LocalDateTime.formatDate(): String {

    val monthName = when (month) {
        Month.JANUARY -> "January"
        Month.FEBRUARY -> "February"
        Month.MARCH -> "March"
        Month.APRIL -> "April"
        Month.MAY -> "May"
        Month.JUNE -> "June"
        Month.JULY -> "July"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "October"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
    }

    val dayName = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }

    return "$dayName, $monthName $day, $year"
}

fun formatUtcOffset(zone: TimeZone, now: Instant): String {
    val totalSeconds = zone.offsetAt(now).totalSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return "%+03d:%02d".replace("%+03d", if (hours >= 0) "+$hours" else "$hours")
        .replace("%02d", minutes.toString().padStart(2, '0'))
}
