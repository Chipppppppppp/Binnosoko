package space.aioilight.tsubonofuta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import space.aioilight.tsubonofuta.ui.theme.DynamicColor
import space.aioilight.tsubonofuta.ui.theme.Typography

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }

    DynamicColor {
        // A surface container using the 'background' color from the theme
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            content = {
                SettingsContent()
            },
            topBar = {
                SettingsTopBar(scrollBehavior)
            }
        )
    }
}

@Composable
fun SettingsContent() {
    val scrollState = rememberScrollState()
    val config = AppConfig.newInstanceForModule(LocalContext.current)
    Column(
        Modifier.verticalScroll(scrollState)
    ) {
        SettingsSwitch(
            title = stringResource(id = R.string.settings_thread_title),
            description = stringResource(id = R.string.settings_thread_desc),
            value = config.hideThreadAd
        ) {
            config.hideThreadAd = it
        }
        SettingsSwitch(
            title = stringResource(id = R.string.settings_inline_title),
            description = stringResource(id = R.string.settings_inline_desc),
            value = config.hideInlineAd
        ) {
            config.hideInlineAd = it
        }
        SettingsSwitch(
            title = stringResource(id = R.string.settings_past_log_title),
            description = stringResource(id = R.string.settings_past_log_desc),
            value = config.hidePastLogAd
        ) {
            config.hidePastLogAd = it
        }
        Spacer(modifier = Modifier.height(32.dp))
        GitHub()
        Status()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(topAppBarScrollBehavior: TopAppBarScrollBehavior) {
    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        scrollBehavior = topAppBarScrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit
) {
    val checkedState = remember { mutableStateOf(value) }
    Row(
        Modifier
            .clickable {
                checkedState.value = !checkedState.value
                onChanged(checkedState.value)
            }
            .fillMaxWidth()
            .padding(24.dp),
        Arrangement.SpaceBetween,
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = Typography.subtitle1
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = Typography.caption
                )
            }
        }
        Checkbox(checked = checkedState.value, onCheckedChange = null)
    }
}

@Composable
fun Status() {
    val title = stringResource(
        id = R.string.settings_status_title,
        stringResource(id = R.string.app_name),
        BuildConfig.VERSION_NAME
    )
    val desc = stringResource(id = R.string.settings_status_desc)
    val context = LocalContext.current
    Row(
        Modifier
            .clickable {
                val intent = Intent(context, OssLicensesMenuActivity::class.java)
                context.startActivity(intent)
            }
            .fillMaxWidth()
            .padding(24.dp),
        Arrangement.SpaceBetween,
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = Typography.subtitle1
            )
            Text(
                text = desc,
                style = Typography.caption
            )
        }
    }
}

@Composable
fun GitHub() {
    val uri = stringResource(id = R.string.repository_url)
    val context = LocalContext.current
    Row(
        Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            }
            .fillMaxWidth()
            .padding(24.dp),
        Arrangement.SpaceBetween,
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.settings_github_title),
                style = Typography.subtitle1
            )
            Text(
                text = stringResource(id = R.string.settings_github_desc),
                style = Typography.caption
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SettingScreen()
}