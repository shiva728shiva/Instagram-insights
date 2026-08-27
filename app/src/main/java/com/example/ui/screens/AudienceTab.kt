package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.ReelInsightsData
import com.example.ui.components.MetricProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgPurple

@Composable
fun AudienceTab(
    data: ReelInsightsData,
    loading: Boolean,
    subTab: String,
    onSubTabSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 40.dp)
    ) {
        // 1. Who viewed your reel
        SectionHeader(title = "Who viewed your reel")
        Spacer(modifier = Modifier.height(10.dp))

        MetricProgressBar(
            label = "Followers",
            percent = data.followersAudiencePct,
            barColor = IgPurple,
            loading = loading
        )
        MetricProgressBar(
            label = "Non-followers",
            percent = data.nonFollowersAudiencePct,
            barColor = IgPurple,
            loading = loading
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Audience details
        SectionHeader(title = "Audience details")
        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills: Age, Country, Gender
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Age", "Country", "Gender").forEach { tab ->
                FilterPill(
                    label = tab,
                    isSelected = subTab == tab,
                    onClick = { onSubTabSelect(tab) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Demographics progress bars
        when (subTab) {
            "Age" -> {
                data.ageDemographics.forEach { (ageRange, pct) ->
                    MetricProgressBar(
                        label = ageRange,
                        percent = pct,
                        barColor = IgMagenta,
                        loading = loading
                    )
                }
            }
            "Country" -> {
                data.countryDemographics.forEach { (country, pct) ->
                    MetricProgressBar(
                        label = country,
                        percent = pct,
                        barColor = IgMagenta,
                        loading = loading
                    )
                }
            }
            "Gender" -> {
                data.genderDemographics.forEach { (gender, pct) ->
                    MetricProgressBar(
                        label = gender,
                        percent = pct,
                        barColor = if (gender.contains("Women", ignoreCase = true)) IgPurple else IgMagenta,
                        loading = loading
                    )
                }
            }
        }
    }
}
