package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.IgBorder
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgMagenta
import com.example.ui.theme.IgTextPrimary
import com.example.ui.theme.IgTextSecondary

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (
        username: String,
        fullName: String,
        avatarUrl: String,
        category: String,
        bio: String,
        postsCount: Int,
        followersCount: Int,
        followingCount: Int
    ) -> Unit
) {
    var username by remember { mutableStateOf(profile.username) }
    var fullName by remember { mutableStateOf(profile.fullName) }
    var avatarUrl by remember { mutableStateOf(profile.avatarUrl) }
    var category by remember { mutableStateOf(profile.category) }
    var bio by remember { mutableStateOf(profile.bio) }
    var postsCount by remember { mutableStateOf(profile.postsCount.toString()) }
    var followersCount by remember { mutableStateOf(profile.followersCount.toString()) }
    var followingCount by remember { mutableStateOf(profile.followingCount.toString()) }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80"
    )

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
                        text = "Edit Profile",
                        fontSize = 18.sp,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Preview and selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choose Avatar Preset:",
                        fontSize = 11.sp,
                        color = IgTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetAvatars.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { avatarUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProfileEditField(
                    label = "Avatar Image URL",
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it }
                )

                ProfileEditField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it }
                )

                ProfileEditField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it }
                )

                ProfileEditField(
                    label = "Category",
                    value = category,
                    onValueChange = { category = it }
                )

                ProfileEditField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { bio = it },
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileEditField(
                        label = "Posts",
                        value = postsCount,
                        onValueChange = { postsCount = it },
                        isNumber = true,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileEditField(
                        label = "Followers",
                        value = followersCount,
                        onValueChange = { followersCount = it },
                        isNumber = true,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileEditField(
                        label = "Following",
                        value = followingCount,
                        onValueChange = { followingCount = it },
                        isNumber = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSave(
                            username.trim().removePrefix("@").ifBlank { "user" },
                            fullName.trim().ifBlank { "User" },
                            avatarUrl.trim().ifBlank { profile.avatarUrl },
                            category.trim().ifBlank { "Creator" },
                            bio,
                            postsCount.toIntOrNull() ?: profile.postsCount,
                            followersCount.toIntOrNull() ?: profile.followersCount,
                            followingCount.toIntOrNull() ?: profile.followingCount
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IgMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button")
                ) {
                    Text(
                        text = "Save Profile",
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
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isNumber: Boolean = false,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
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
