import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.FloatingActionMenuItem
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDTextView

@Composable
fun MdFloatingActionMenu(
    modifier: Modifier = Modifier,
    items: List<FloatingActionMenuItem>,
    isMenuExpanded: MutableState<Boolean?>,
    onMenuExpandedChange: (Boolean) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        if (isMenuExpanded.value == true) -135f else 0f, label = "", animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val animationProgress by animateFloatAsState(
        targetValue = if (isMenuExpanded.value == true) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isMenuExpanded.value == true) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { /* Intercept touch events */ }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        if (isMenuExpanded.value == true) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.9f)),
                    start = Offset(size.width, size.height),
                    end = Offset(
                        size.width * (1 - animationProgress),
                        size.height * (1 - animationProgress)
                    )
                )
                drawRect(brush = brush)
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(
                    end = dimensionResource(R.dimen.dashboard_fab_margin),
                    bottom = dimensionResource(R.dimen.dashboard_fab_margin)
                )
                .align(Alignment.BottomEnd)
        ) {
            if (isMenuExpanded.value == true) {
                items.forEach { item ->
                    MdFloatingMenuButton(
                        isMenuExpanded = isMenuExpanded.value == true,
                        label = item.label,
                        icon = item.icon,
                        color = item.backgroundColor,
                        contentColor = item.contentColor,
                        onItemClicked = {
                            item.onClick()
                            onMenuExpandedChange(false)
                        },
                        modifier = Modifier
                    )

                }
            }
            FloatingActionButton(
                onClick = {
                    onMenuExpandedChange(isMenuExpanded.value != true)
                },
                containerColor = colorResource(R.color.colorPrimary),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_primary),
                    contentDescription = "Toggle Menu",
                    tint = colorResource(R.color.tabSelectedTxtColor),
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
fun MdFloatingMenuButton(
    isMenuExpanded: Boolean,
    label: String,
    icon: Int,
    color: Color,
    onItemClicked: () -> Unit,
    modifier: Modifier,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, onClick = { onItemClicked() })
            .padding(end = 8.dp)
    ) {

        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideIn(
                animationSpec = tween(durationMillis = 300),
                initialOffset = { IntOffset(50, 0) }
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)) + slideOut(
                animationSpec = tween(durationMillis = 200),
                targetOffset = { IntOffset(50, 0) }
            )
        ) {
            MDTextView(
                value = label,
                style = MdTheme.typography.semiBold14TextStyle,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 100)) + scaleIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)) + scaleOut()
        ) {
            FloatingActionButton(
                modifier = Modifier.size(40.dp),
                onClick = { onItemClicked() },
                containerColor = color,
                shape = CircleShape
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = contentColor
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMdFloatingMenuButton() {
    MPHRTheme {

        var isMenuExpanded by remember { mutableStateOf(null) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            MdFloatingMenuButton(
                label = "Edit",
                icon = R.drawable.ic_message_white,
                color = colorResource(R.color.dashboardComposeMessageFabColor),
                contentColor = colorResource(R.color.white),
                onItemClicked = { /* Handle item click */ },
                modifier = Modifier,
                isMenuExpanded = false
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMdFloatingActionMenu() {


    val items = listOf(
        FloatingActionMenuItem(
            label = "Edit asdfasdf asdfasdf ",
            icon = R.drawable.ic_edit,
            backgroundColor = colorResource(R.color.colorAccent),
            onClick = { /* Handle edit click */ }
        ),
        FloatingActionMenuItem(
            label = "Delete asdfasdf asdfasdf",
            icon = R.drawable.ic_message_white,
            backgroundColor = colorResource(R.color.colorPrimary),
            onClick = { /* Handle delete click */ }
        )
    )

    MPHRTheme {
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            MdFloatingActionMenu(
                items = items,
                modifier = TODO(),
                isMenuExpanded = TODO(),
                onMenuExpandedChange = TODO()
            )
        }
    }
}