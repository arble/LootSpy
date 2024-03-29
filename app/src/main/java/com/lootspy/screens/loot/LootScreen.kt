package com.lootspy.screens.loot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.lootspy.R
import com.lootspy.api.workers.GetMembershipsWorker
import com.lootspy.api.workers.GetVendorsWorker
import com.lootspy.data.bungiePath
import com.lootspy.data.source.DestinyCharacter
import com.lootspy.elements.VendorItemDetails
import com.lootspy.elements.VendorItemElement
import com.lootspy.util.WorkBuilders
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootScreen(
  modifier: Modifier = Modifier,
  viewModel: LootViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val syncVendorWorkInfo =
    WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("sync_vendors")
      .observeAsState()
  val isSyncingVendors = syncVendorWorkInfo.value?.any { it.state == WorkInfo.State.RUNNING }
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(),
        actions = {
          IconButton(onClick = {
            WorkBuilders.dispatchUniqueWorkWithTokens(
              context,
              "sync_characters",
              mapOf("notify_channel" to "lootspyApi"),
              listOf(GetMembershipsWorker::class.java),
            )
//          viewModel.deleteAuthInfo()
          }) {
            Icon(Icons.Default.Check, null)
          }
          IconButton(onClick = {
//            WorkBuilders.dispatchUniqueWorkWithTokens(
//              context,
//              "sync_characters",
//              mapOf("notify_channel" to "lootspyApi"),
//              listOf(GetCharactersWorker::class.java),
//            )
//          viewModel.deleteAuthInfo()
            viewModel.clearLoot()
          }) {
            Icon(Icons.Default.List, null)
          }
          IconButton(
            enabled = isSyncingVendors != true,
            onClick = {
              WorkBuilders.dispatchUniqueWorkWithTokens(
                context,
                "sync_vendors",
                mapOf("notify_channel" to "lootspyApi"),
                listOf(GetVendorsWorker::class.java),
              )
//          viewModel.deleteAuthInfo()
            }) {
            Icon(Icons.Default.Refresh, null)
          }
        }
      )
    },
    modifier = modifier.fillMaxSize(),
    floatingActionButtonPosition = FabPosition.End
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showCharacterSheet by remember { mutableStateOf(false) }
    val characterSheetState =
      remember { SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Hidden) }
    val itemDetailSheetState =
      remember { SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Hidden) }
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (uiState.isLoading || isSyncingVendors == true) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = stringResource(id = R.string.loot_screen_loading))
            CircularProgressIndicator(modifier = modifier)
          }
        }
      } else if (uiState.items.isEmpty()) {
//        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = stringResource(id = R.string.loot_screen_empty),
          style = MaterialTheme.typography.headlineMedium
        )
//        }
      } else {
        for (entry in uiState.items) {
          VendorItemElement(item = entry.item) {
            viewModel.selectItem(it)
          }
        }
      }
      val activeCharacter = uiState.characters.find { it.characterId == uiState.activeCharacter }
        ?: uiState.characters.getOrNull(0)
      Row(modifier = Modifier.height(48.dp)) {
        if (activeCharacter != null) {
          CharacterSelectorItem(character = activeCharacter)
        } else {
          Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            text = "Select a character"
          )
        }
        IconButton(onClick = {
          scope.launch {
            showCharacterSheet = true
//            sheetState.show()
          }
        }) {
          Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
      }

      val selectedItem = uiState.selectedItem
      if (showCharacterSheet) {
        ModalBottomSheet(
          onDismissRequest = { showCharacterSheet = false },
          sheetState = characterSheetState,
          windowInsets = WindowInsets.safeDrawing,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .systemBarsPadding()
          ) {
            uiState.characters.forEach { character ->
              CharacterSelectorItem(character = character) { clickedCharacter ->
                viewModel.saveActiveCharacter(clickedCharacter.characterId)
                scope.launch { characterSheetState.hide() }.invokeOnCompletion {
                  if (!characterSheetState.isVisible) {
                    showCharacterSheet = false
                  }
                }
              }
            }
            Row(modifier = Modifier.height(48.dp)) {}
          }
        }
      } else if (selectedItem != null) {
        ModalBottomSheet(
          onDismissRequest = { viewModel.selectItem(null) },
          sheetState = itemDetailSheetState,
          windowInsets = WindowInsets.safeDrawing,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .systemBarsPadding()
          ) {
            VendorItemDetails(item = selectedItem)
            Row(modifier = Modifier.height(48.dp)) {}
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
private fun CharacterSelectorItem(
  character: DestinyCharacter,
  placeholder: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  error: Painter = painterResource(id = com.google.android.material.R.drawable.mtrl_ic_cancel),
  onClick: (DestinyCharacter) -> Unit = {},
) {
  Card(
    onClick = { onClick(character) },
    modifier = Modifier.fillMaxWidth(0.85f),
    colors = CardDefaults.cardColors(containerColor = Color(character.emblemColor))
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        model = character.emblemPath.bungiePath(),
        placeholder = placeholder,
        error = error,
        modifier = Modifier
          .width(48.dp)
          .height(48.dp),
        contentDescription = null
      )
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = "${character.race} ${character.guardianClass}",
        style = TextStyle.Default.copy(
          fontSize = 20.sp,
          drawStyle = Stroke(
            miter = 10f,
            width = 2f,
            join = StrokeJoin.Round
          )
        ),
        textAlign = TextAlign.Left,
        modifier = Modifier.weight(0.6f)
      )
      Text(
        text = "${character.power}",
        style = TextStyle.Default.copy(
          fontSize = 20.sp,
          drawStyle = Stroke(
            miter = 10f,
            width = 2f,
            join = StrokeJoin.Round
          )
        ),
        textAlign = TextAlign.Right,
        modifier = Modifier.weight(0.4f)
      )
      Spacer(modifier = Modifier.width(16.dp))
    }
  }
}