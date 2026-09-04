package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReelItem
import com.example.data.model.UserProfile
import com.example.ui.theme.IgBackground
import com.example.ui.theme.IgCardBg
import com.example.ui.theme.IgDivider
import com.example.ui.theme.IgGreen
import com.example.ui.theme.IgPinkAccent
import com.example.ui.theme.IgPurple
import com.example.ui.theme.IgTextMuted
import com.example.ui.theme.IgTextPrimary

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSelectReel: (ReelItem) -> Unit,
    onChangeUsernameClick: () -> Unit,
    onSelectVideoClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = onChangeUsernameClick,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Posts, 1: Reels (Default matching Screenshot 2), 2: Tagged

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IgBackground)
            .statusBarsPadding()
    ) {
        // 1. Top Bar: [+] [username ˅] [@ Threads] [☰ Menu]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSelectVideoClick() },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("create_reel_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reel / Select Video",
                    tint = IgTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Clickable Username with Dropdown
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditProfileClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = IgTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Switch Account",
                    tint = IgTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Threads Icon (@ symbol styled)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onEditProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "@",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = IgTextPrimary
                    )
                }

                IconButton(
                    onClick = { onEditProfileClick() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = IgTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Lazy Grid containing Profile Header and 3-column Reels
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalArrangement = Arrangement.spacedBy(1.5.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("profile_reels_grid")
        ) {
            // Full-width Header Section
            item(span = { GridItemSpan(3) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row: Avatar on Left + Stats on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Avatar with Story ring and bubble note (Clickable to edit profile)
                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier.clickable { onEditProfileClick() }
                        ) {
                            // "Make this space yours..." note bubble
                            Box(
                                modifier = Modifier
                                    .offset(y = (-14).dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF262626))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Make this space\nyours...",
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    color = Color(0xFFC7C7C7),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Story Avatar Circle
                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFFF9CE34),
                                                IgPinkAccent,
                                                IgPurple,
                                                Color(0xFFF9CE34)
                                            )
                                        )
                                    )
                                    .padding(2.5.dp)
                                    .clip(CircleShape)
                                    .background(IgBackground)
                                    .padding(2.dp)
                            ) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = "Profile Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )

                                // Orange + badge on bottom right
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9500))
                                        .border(2.dp, IgBackground, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add story",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Posts, Followers, Following Counters (Clickable to edit stats)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditProfileClick() }
                                .padding(start = 24.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(count = profile.postsCount.toString(), label = "posts")
                            ProfileStatItem(count = profile.followersCount.toString(), label = "followers")
                            ProfileStatItem(count = profile.followingCount.toString(), label = "following")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Name, Category, Bio (Clickable to edit)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditProfileClick() }
                    ) {
                        Text(
                            text = profile.fullName,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = IgTextPrimary
                        )
                        Text(
                            text = profile.category,
                            fontSize = 13.sp,
                            color = IgTextMuted,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        profile.bio.lines().forEach { line ->
                            Text(
                                text = line,
                                fontSize = 13.sp,
                                color = IgTextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // + Add banners button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF262626))
                            .clickable { onEditProfileClick() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = IgTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add banners",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IgTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Professional Dashboard Card (Matching Screenshot 2)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditProfileClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252D))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                text = "Professional dashboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IgTextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = IgGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.monthlyViews,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF9EABB8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Edit Profile, Share Profile, and Select Video buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onEditProfileClick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF262626),
                                contentColor = IgTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(
                                text = "Edit profile",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { onSelectVideoClick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0095F6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(36.dp)
                                .testTag("select_video_profile_btn")
                        ) {
                            Text(
                                text = "Import Reel",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF262626),
                                contentColor = IgTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(
                                text = "Share",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid Tabs: [Posts Grid] [Reels] [Tagged]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Posts Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Posts",
                                tint = if (selectedTab == 0) IgTextPrimary else IgTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.5.dp)
                                    .background(if (selectedTab == 0) IgTextPrimary else Color.Transparent)
                            )
                        }

                        // Reels Tab (Active in screenshot)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.VideoLibrary,
                                    contentDescription = "Reels",
                                    tint = if (selectedTab == 1) IgTextPrimary else IgTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) IgTextPrimary else IgTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.5.dp)
                                    .background(if (selectedTab == 1) IgTextPrimary else Color.Transparent)
                            )
                        }

                        // Tagged Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 2 },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonPin,
                                contentDescription = "Tagged",
                                tint = if (selectedTab == 2) IgTextPrimary else IgTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.5.dp)
                                    .background(if (selectedTab == 2) IgTextPrimary else Color.Transparent)
                            )
                        }
                    }
                }
            }

            // 3-Column Reels Grid Items
            items(profile.reels, key = { it.id }) { reel ->
                ReelGridItem(
                    reel = reel,
                    onClick = { onSelectReel(reel) }
                )
            }
        }

        // Bottom Navigation Bar (Instagram standard)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IgBackground)
                .border(0.5.dp, IgDivider)
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Home",
                tint = IgTextPrimary,
                modifier = Modifier.size(26.dp)
            )
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = IgTextPrimary,
                modifier = Modifier.size(26.dp)
            )
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = "Reels",
                tint = IgTextPrimary,
                modifier = Modifier.size(26.dp)
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = IgTextPrimary,
                modifier = Modifier.size(26.dp)
            )
            // Profile Tab circle
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, IgTextPrimary, CircleShape)
                    .padding(1.5.dp)
            ) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = IgTextPrimary
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = IgTextPrimary
        )
    }
}

@Composable
fun ReelGridItem(
    reel: ReelItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.75f) // Instagram 3:4 or 9:16 grid cell aspect ratio
            .background(Color(0xFF1E1E1E))
            .clickable { onClick() }
            .testTag("reel_item_${reel.id}")
    ) {
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = "Reel thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // View Count Badge at Bottom Left (e.g. 👁 1,379 or 👁 336)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = "Views",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = formatViews(reel.viewsCount),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

private fun formatViews(views: Int): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
        views >= 10_000 -> String.format("%.1fK", views / 1000.0)
        views >= 1000 -> String.format("%,d", views)
        else -> views.toString()
    }
}
