package com.lootspy.util.popup

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun LootSpyPopup(
  popupState: PopupState,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  properties: PopupProperties = PopupProperties(focusable = true),
  content: @Composable ColumnScope.() -> Unit
) {
  // Create a transition state to track whether the popup is expanded.
  val expandedStates = remember { MutableTransitionState(false) }
  expandedStates.targetState = popupState.isVisible

  // Only show the popup if it's visible.
  if (expandedStates.currentState || expandedStates.targetState) {
    val density = LocalDensity.current

    // Instantiate a CustomPopupPositionProvider with the given offset.
    val popupPositionProvider = LootSpyPopupPositionProvider(
      contentOffset = offset,
      density = density
    ) { alignment, isTop ->
      // Update the PopupState's alignment and direction.
      popupState.horizontalAlignment = alignment
      popupState.isTop = !isTop
    }

    // Display the popup using the Popup composable.
    Popup(
      onDismissRequest = onDismissRequest,
      popupPositionProvider = popupPositionProvider,
      properties = properties
    ) {
      // Display the popup's content using the CustomPopupContent composable.
      LootSpyPopupContent(
        expandedStates = expandedStates,
        transformOrigin = TransformOrigin(
          pivotFractionX = when (popupState.horizontalAlignment) {
            Alignment.Start -> 0f
            Alignment.CenterHorizontally -> 0.5f
            else -> 1f
          },
          pivotFractionY = if (popupState.isTop) 1f else 0f
        ),
        modifier = modifier,
        content = content
      )
    }
  }
}

@Composable
private fun LootSpyPopupContent(
  expandedStates: MutableTransitionState<Boolean>,
  transformOrigin: TransformOrigin,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  // Menu open/close animation.
  val transition = updateTransition(expandedStates, "Popup")

  // Scale animation.
  val scale by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        // Dismissed to expanded.
        tween(durationMillis = 200)
      } else {
        // Expanded to dismissed.
        tween(durationMillis = 200)
      }
    },
    label = "Popup Scale"
  ) {
    if (it) {
      // Popup is expanded.
      1f
    } else {
      // Popup is dismissed.
      0f
    }
  }

  // Alpha animation.
  val alpha by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        // Dismissed to expanded.
        tween(durationMillis = 200)
      } else {
        // Expanded to dismissed.
        tween(durationMillis = 200)
      }
    },
    label = "Popup Alpha"
  ) {
    if (it) {
      // Popup is expanded.
      1f
    } else {
      // Popup is dismissed.
      0f
    }
  }

  // Helper function for applying animations to graphics layer.
  fun GraphicsLayerScope.graphicsLayerAnim() {
    scaleX = scale
    scaleY = scale
    this.alpha = alpha
    this.transformOrigin = transformOrigin
  }

  Surface(
    modifier = Modifier
      .graphicsLayer {
        graphicsLayerAnim()
      }
  ) {
    Column(
      modifier = modifier
        .width(IntrinsicSize.Max)
        .verticalScroll(rememberScrollState()),
      content = content
    )
  }
}