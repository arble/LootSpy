package com.lootspy

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lootspy.data.source.DestinyProfile
import com.lootspy.util.BungiePathHelper
import com.lootspy.util.ScreenContent
import com.lootspy.util.ScreenContentWithEmptyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootSpyProfilePrompt(
  profiles: List<DestinyProfile>,
  profileSelectedAction: (DestinyProfile) -> Unit,
) {
  Scaffold(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(it),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (profiles.isEmpty()) {
        return@Column
      }
      Text(text = stringResource(id = R.string.profile_choose_header))
      ScreenContent(
        loading = false, items = profiles, itemContent = { _, profile ->
          ProfileCard(profile = profile, onClickProfile = profileSelectedAction)
        }, emptyComposable = {
          Text(text = stringResource(id = R.string.profile_no_profiles))
        }
      )
    }
  }
}

@Composable
private fun ProfileCard(
  profile: DestinyProfile,
  onClickProfile: (DestinyProfile) -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = dimensionResource(id = R.dimen.horizontal_margin),
          vertical = dimensionResource(id = R.dimen.loot_item_padding)
        )
        .clickable { onClickProfile(profile) }
    ) {
      Image(
        painter = rememberAsyncImagePainter(model = BungiePathHelper.getFullUrlForPath(profile.iconPath)),
        contentDescription = null
      )
      Text(text = profile.platformDisplayName, style = MaterialTheme.typography.headlineSmall)
    }
  }
}