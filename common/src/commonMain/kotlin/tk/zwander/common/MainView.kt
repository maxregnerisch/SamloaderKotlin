package tk.zwander.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.*
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class)
@ExperimentalTime
@Composable
fun MainView() {
    val page = remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }

    CustomMaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TabView(page)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(
                            ScrollState(0)
                        )
                ) {
                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Crossfade(
                            targetState = page.value,
                            animationSpec = tween(300)
                        ) {
                            when (it) {
                                Page.DOWNLOADER -> DownloadView(downloadModel)
                                Page.DECRYPTER -> DecryptView(decryptModel)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    FooterView()
                }
            }
        }
    }
}