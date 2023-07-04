package com.lootspy

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lootspy.ui.theme.LootSpyTheme

class DummyActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val text = intent.data.toString()
    setContent {
      LootSpyTheme() {
        LootSpyLoginPrompt {

        }
//        Scaffold() {
//          Column(
//            modifier = Modifier
//              .padding(it)
//              .fillMaxSize(),
//            verticalArrangement = Arrangement.SpaceEvenly
//          ) {
//            Text(text = text)
//            Text(text = "hello world", color = Color.Black)
//          }
//        }
      }
    }
  }
}