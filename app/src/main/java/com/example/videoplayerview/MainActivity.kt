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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_ONE
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
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val channelIndex = mutableIntStateOf(1)
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
    arrayOf("A001", "udp://@233.166.172.6:1234"),
    arrayOf("A002", "udp://@233.166.172.8:1234"),
    arrayOf("A003", "udp://@233.166.172.25:1234"),
    arrayOf("A004", "udp://@233.166.172.41:1234"),
    arrayOf("A005", "udp://@233.166.172.24:1234"),
            arrayOf("A003", "udp://@233.166.172.42:1234"),
arrayOf("A004", "udp://@233.166.172.43:1234"),
arrayOf("A005", "udp://@233.166.172.46:1234")
)

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
    Box (modifier = Modifier
                 .fillMaxSize()
      //           .border(4.dp, Color.Red)
                       ){
        ExoPlayerView(channelIndex = channelIndex,
            splitFraction = 1f)
        Column (modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom){
            NavigationButtons(
                onButtonForwardClick = onButtonForwardClick,
                onButtonBackwardClick = onButtonBackwardClick
            )
            Spacer(Modifier.width(30.dp))
            InformationBox(channelIndex = channelIndex)
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CounterButton(value = channelIndex.value.toString(),
                    onButtonForwardClick = onButtonForwardClick,
                    onButtonBackwardClick = onButtonBackwardClick)
            }
            //DraggableTextLowLevel()
        }
    }
}


@Composable
fun NavigationButtons(
 //   channelIndex: State<Int>,
    onButtonForwardClick: () -> Unit,
    onButtonBackwardClick: () -> Unit
) {
//    val channelIndexValue = channelIndex.value

    val CONTAINER_BACKGROUND_ALPHA_INITIAL = 0.56f
    Row(modifier = Modifier
      //  .padding(top = 20.dp)
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopCenter)
        .clip(RoundedCornerShape(4.dp))
        .background(Color.Black.copy(alpha = CONTAINER_BACKGROUND_ALPHA_INITIAL))
        //.border(4.dp, Color.Black, shape = RoundedCornerShape(10.dp))
        .padding(10.dp),


        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Button(
            onClick = onButtonBackwardClick,
            Modifier.padding(end = 20.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Назад", color = Color.White)
        }
        Button(
            onClick = onButtonForwardClick,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Вперед", color = Color.White)
        }



    }
}

@Composable
fun InformationBox(
    channelIndex: State<Int>
){
    val channelIndexValue = channelIndex.value

        Column(modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()

            .wrapContentSize(Alignment.TopCenter)
            .border(4.dp, Color.Black, shape = RoundedCornerShape(10.dp))
            .padding(15.dp)
            .background(Color.Red),
            horizontalAlignment = Alignment.CenterHorizontally,
            ){
            Text(channels[channelIndexValue][0])
            Spacer(Modifier.width(30.dp))
            Text(channels[channelIndexValue][1])
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
    Log.d("Boom", "$context")
//    val channelIndex = remember { mutableIntStateOf(0) }
    val channelIndexValue = channelIndex.value
    Log.d("Boom", "ClickCounter ${channels[channelIndexValue][1]}")
    // Initialize ExoPlayer
//    val exoPlayer = remember (channelIndex){ExoPlayer.Builder(context).build()}
    val exoPlayer = remember { ExoPlayer.Builder(context).build()}
 //   LaunchedEffect(key1 = channelIndexValue) {
    val defaultHttpDataSourceFactory =
        DataSource.Factory { UdpDataSource(3000, 20000) }
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
    LaunchedEffect(channelIndexValue) {
        Log.d("Boom", "${mediaSource.hashCode()}")
    // Из MediaItem без MediaSource
    //    exoPlayer.setMediaItem(MediaItem.fromUri(channels[channelIndex.intValue][1]))
    // С MediaSource больше customization
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
//        exoPlayer.playWhenReady
        Log.d("Boom", "${channels.count()}" + "${channelIndexValue}")
/*        if (exoPlayer.isPlaying.not()) {
            exoPlayer.play()
        }*/

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
            .fillMaxSize()
    //        .padding(top = 10.dp)
    //        .border(4.dp, Color.Red)
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
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                            controllerAutoShow = false
                            setKeepContentOnPlayerReset(true)
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            // Set resize mode to fill the available space
                            //resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
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

/*@Composable
private fun DraggableTextLowLevel() {
    Box(modifier = Modifier.fillMaxSize()) {
        val offsetX = remember { mutableFloatStateOf(0f) }
        val offsetY = remember { mutableFloatStateOf(0f) }

        Box(
            Modifier
                .offset { IntOffset(offsetX.floatValue.roundToInt(), offsetY.floatValue.roundToInt()) }
                .background(Color.Blue)
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX.floatValue += dragAmount.x
                        offsetY.floatValue += dragAmount.y
                    }
                }
        )
    }
}*/


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

