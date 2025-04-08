package com.example.videoplayerview

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.TimestampAdjuster
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.UdpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.extractor.ts.TsExtractor.FLAG_EMIT_RAW_SUBTITLE_DATA
import androidx.media3.extractor.ts.TsExtractor.MODE_SINGLE_PMT
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val channelIndex = mutableIntStateOf(0)
        setContent {
            HomeScreen(
                channelIndex = channelIndex,
                onButtonForwardClick = {
                    if (channelIndex.intValue++ == (channels.count()-1))
                        channelIndex.intValue = 0
                    else channelIndex.intValue
                },
                onButtonBackwardClick = {
                    if (--channelIndex.intValue == -1)
                        channelIndex.intValue = (channels.count()-1)
                    else channelIndex.intValue
                }
            )
//            ExoPlayerView()

            }
        }
}
/*val channels = mapOf(0 to "udp://@233.166.172.211:1234",
                     1 to "udp://@233.166.172.212:1234",
                     2 to "udp://@233.166.172.249:1234")*/

val channels: Array<Array<String>> = arrayOf(
    arrayOf("Раз ТВ", "udp://@233.166.172.64:1234"),
    arrayOf("Наше Новое Кино HD", "udp://@233.166.172.65:1234"),
    arrayOf("A001", "udp://@233.166.172.211:1234"),
    arrayOf("A002", "udp://@233.166.172.212:1234"),
    arrayOf("A003", "udp://@233.166.172.249:1234"))

//const val EXAMPLE_VIDEO_URI = "udp://@233.166.172.211:1234"
/*@SuppressLint("AuthLeak")
const val EXAMPLE_VIDEO_URI = "rtsp://admin:VZwRkUu16M@192.168.2.197:554/stream1"*/

@Composable
fun HomeScreen(
    channelIndex: State<Int>,
    onButtonForwardClick: () -> Unit,
    onButtonBackwardClick: () -> Unit
) {
//    val channelIndexValue = channelIndex.value
    Column (modifier = Modifier.fillMaxSize()){
        Text("MOOOOOOOO!!!!")
        ExoPlayerView(channelIndex = channelIndex,
            splitFraction = 0.6f)
        NavigationButtons(channelIndex = channelIndex,onButtonForwardClick = onButtonForwardClick,
            onButtonBackwardClick = onButtonBackwardClick)
        Text("MOOOOOOOO!!!!")
    }
}

@Composable
fun NavigationButtons(
    channelIndex: State<Int>,
    onButtonForwardClick: () -> Unit,
    onButtonBackwardClick: () -> Unit
) {
    val channelIndexValue = channelIndex.value
    Row(modifier = Modifier.fillMaxSize().padding(end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End) {
        Button(
            onClick = onButtonBackwardClick,
            Modifier.padding(end = 20.dp)
        ) {
            Text("Назад", color = Color.White)
        }
        Button(
            onClick = onButtonForwardClick,
        ) {
            Text("Вперед", color = Color.White)
        }
        Spacer(Modifier.width(15.dp))
        Column {
            Text(channels[channelIndexValue][0])
            Text(channels[channelIndexValue][1])
        }

    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@androidx.annotation.OptIn(UnstableApi::class)
//@Preview(device = Devices.PIXEL_4)
@Composable
fun ExoPlayerView(channelIndex: State<Int>,
                  splitFraction: Float = 0.8f) {
    // Get the current context
    val context = LocalContext.current
//    val channelIndex = remember { mutableIntStateOf(0) }
    val channelIndexValue = channelIndex.value
    Log.d("Boom", "ClickCounter ${channels[channelIndexValue][1]}")
    // Initialize ExoPlayer
//    val exoPlayer = remember (channelIndex){ExoPlayer.Builder(context).build()}
    val exoPlayer = ExoPlayer.Builder(context).build()
    val defaultHttpDataSourceFactory =
        DataSource.Factory { UdpDataSource(2500, 100000) }
//    val defaultHttpDataSourceFactory: DataSource.Factory =  DefaultHttpDataSource.Factory()
    // Create a MediaSource
    val tsExtractorFactory = ExtractorsFactory {
        arrayOf(
            TsExtractor(
                MODE_SINGLE_PMT,
                TimestampAdjuster(0),
                DefaultTsPayloadReaderFactory()
            )
        )
    }
    val mediaSource: MediaSource =
        ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory, tsExtractorFactory)
            .createMediaSource(MediaItem.fromUri(channels[channelIndexValue][1]))
   // exoPlayer.addMediaItem(MediaItem.fromUri(channels[channelIndex.intValue+1][1]))


//    Log.d("Boom", "${mediaSource.mediaItem} ${mediaSource.hashCode()}")

    // Set MediaSource to ExoPlayer
    //LaunchedEffect(mediaSource) {
        Log.d("Boom", "${mediaSource.hashCode()}")
    // Из MediaItem без MediaSource
    //    exoPlayer.setMediaItem(MediaItem.fromUri(channels[channelIndex.intValue][1]))
    // С MediaSource больше customization
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
//        exoPlayer.playWhenReady
        Log.d("Boom", "${channels.count()}" + "${channelIndexValue}")
        if (exoPlayer.isPlaying.not()) {
            exoPlayer.play()
  //      }
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .padding(top = 10.dp)
    ) {

            // Use AndroidView to embed an Android View (PlayerView) into Compose
/*            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp * splitFraction)// Set your desired height
            )*/

                AndroidView (

                    factory = {
                        // AndroidView to embed a PlayerView into Compose
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            // Set resize mode to fill the available space
//                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                            // Hide unnecessary player controls
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                            setShowFastForwardButton(false)
                            setShowRewindButton(false)
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp * splitFraction)// Set your desired height
                    )

        }
    }




/*@Preview(widthDp = 300, heightDp = 600)
@Composable
fun ButtonSample() {
    val counter = remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Text(
            text = "Counter value: ${counter.value}",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = {
                counter.value++
            },
        ) {
            Text("Increment", color = Color.White)
        }
    }
}*/

