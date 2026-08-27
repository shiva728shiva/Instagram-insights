package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ReelInsightsData
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary

@Composable
fun EditDemographicsDialog(
    data: ReelInsightsData,
    initialTab: String = "Country",
    onDismiss: () -> Unit,
    onSave: (
        followersPct: Float,
        nonFollowersPct: Float,
        reelsTabPct: Float,
        explorePct: Float,
        profilePct: Float,
        countryDemographics: Map<String, Float>,
        genderDemographics: Map<String, Float>
    ) -> Unit
) {
    var followersPct by remember { mutableStateOf(data.followersAudiencePct.toString()) }
    var nonFollowersPct by remember { mutableStateOf(data.nonFollowersAudiencePct.toString()) }

    var reelsTabPct by remember { mutableStateOf(data.reelsTabPct.toString()) }
    var explorePct by remember { mutableStateOf(data.explorePct.toString()) }
    var profilePct by remember { mutableStateOf(data.profilePct.toString()) }

    // Country fields
    var country1Name by remember { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(0) ?: "India") }
    var country1Pct by remember { mutableStateOf((data.countryDemographics.values.elementAtOrNull(0) ?: 44.0f).toString()) }

    var country2Name by remember { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(1) ?: "United States") }
    var country2Pct by remember { mutableStateOf((data.countryDemographics.values.elementAtOrNull(1) ?: 4.0f).toString()) }

    var country3Name by remember { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(2) ?: "Brazil") }
    var country3Pct by remember { mutableStateOf((data.countryDemographics.values.elementAtOrNull(2) ?: 4.0f).toString()) }

    var country4Name by remember { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(3) ?: "Indonesia") }
    var country4Pct by remember { mutableStateOf((data.countryDemographics.values.elementAtOrNull(3) ?: 3.7f).toString()) }

    var country5Name by remember { mutableStateOf(data.countryDemographics.keys.elementAtOrNull(4) ?: "Uzbekistan") }
    var country5Pct by remember { mutableStateOf((data.countryDemographics.values.elementAtOrNull(4) ?: 3.4f).toString()) }

    // Gender
    var menPct by remember { mutableStateOf((data.genderDemographics["Men"] ?: 67.6f).toString()) }
    var womenPct by remember { mutableStateOf((data.genderDemographics["Women"] ?: 32.4f).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF19191E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Country & Audience Ratio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IgTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IgTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Who viewed your reel (Follower / Non-follower Ratio)
                Text(
                    text = "Who Viewed Your Reel (Ratio %)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgMagenta
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Followers %",
                        value = followersPct,
                        onValueChange = { followersPct = it },
                        modifier = Modifier.weight(1f)
                    )
                    DemoEditField(
                        label = "Non-followers %",
                        value = nonFollowersPct,
                        onValueChange = { nonFollowersPct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Top Countries
                Text(
                    text = "Top Country Demographics",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgMagenta
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Country 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Country 1",
                        value = country1Name,
                        onValueChange = { country1Name = it },
                        isNumber = false,
                        modifier = Modifier.weight(1.5f)
                    )
                    DemoEditField(
                        label = "Percent %",
                        value = country1Pct,
                        onValueChange = { country1Pct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Country 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Country 2",
                        value = country2Name,
                        onValueChange = { country2Name = it },
                        isNumber = false,
                        modifier = Modifier.weight(1.5f)
                    )
                    DemoEditField(
                        label = "Percent %",
                        value = country2Pct,
                        onValueChange = { country2Pct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Country 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Country 3",
                        value = country3Name,
                        onValueChange = { country3Name = it },
                        isNumber = false,
                        modifier = Modifier.weight(1.5f)
                    )
                    DemoEditField(
                        label = "Percent %",
                        value = country3Pct,
                        onValueChange = { country3Pct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Country 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Country 4",
                        value = country4Name,
                        onValueChange = { country4Name = it },
                        isNumber = false,
                        modifier = Modifier.weight(1.5f)
                    )
                    DemoEditField(
                        label = "Percent %",
                        value = country4Pct,
                        onValueChange = { country4Pct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Country 5
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Country 5",
                        value = country5Name,
                        onValueChange = { country5Name = it },
                        isNumber = false,
                        modifier = Modifier.weight(1.5f)
                    )
                    DemoEditField(
                        label = "Percent %",
                        value = country5Pct,
                        onValueChange = { country5Pct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Top Sources of Views
                Text(
                    text = "Top Sources of Views (%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgMagenta
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Reels Tab %",
                        value = reelsTabPct,
                        onValueChange = { reelsTabPct = it },
                        modifier = Modifier.weight(1f)
                    )
                    DemoEditField(
                        label = "Explore %",
                        value = explorePct,
                        onValueChange = { explorePct = it },
                        modifier = Modifier.weight(1f)
                    )
                    DemoEditField(
                        label = "Profile %",
                        value = profilePct,
                        onValueChange = { profilePct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Gender Demographics
                Text(
                    text = "Gender Ratio (%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgMagenta
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoEditField(
                        label = "Men %",
                        value = menPct,
                        onValueChange = { menPct = it },
                        modifier = Modifier.weight(1f)
                    )
                    DemoEditField(
                        label = "Women %",
                        value = womenPct,
                        onValueChange = { womenPct = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val countriesMap = linkedMapOf<String, Float>()
                        if (country1Name.isNotBlank()) countriesMap[country1Name.trim()] = country1Pct.toFloatOrNull() ?: 44f
                        if (country2Name.isNotBlank()) countriesMap[country2Name.trim()] = country2Pct.toFloatOrNull() ?: 4f
                        if (country3Name.isNotBlank()) countriesMap[country3Name.trim()] = country3Pct.toFloatOrNull() ?: 4f
                        if (country4Name.isNotBlank()) countriesMap[country4Name.trim()] = country4Pct.toFloatOrNull() ?: 3.7f
                        if (country5Name.isNotBlank()) countriesMap[country5Name.trim()] = country5Pct.toFloatOrNull() ?: 3.4f

                        val genderMap = mapOf(
                            "Men" to (menPct.toFloatOrNull() ?: 67.6f),
                            "Women" to (womenPct.toFloatOrNull() ?: 32.4f)
                        )

                        onSave(
                            followersPct.toFloatOrNull() ?: data.followersAudiencePct,
                            nonFollowersPct.toFloatOrNull() ?: data.nonFollowersAudiencePct,
                            reelsTabPct.toFloatOrNull() ?: data.reelsTabPct,
                            explorePct.toFloatOrNull() ?: data.explorePct,
                            profilePct.toFloatOrNull() ?: data.profilePct,
                            countriesMap,
                            genderMap
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IgMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_demographics_btn")
                ) {
                    Text(
                        text = "Save Demographics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DemoEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isNumber: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IgCardBg,
            unfocusedContainerColor = IgCardBg,
            focusedBorderColor = IgMagenta,
            unfocusedBorderColor = IgBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = IgMagenta,
            unfocusedLabelColor = IgTextSecondary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
