package com.example.robertsfacialemotionrecognizer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robertsfacialemotionrecognizer.ui.theme.RobertsFacialEmotionRecognizerTheme
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RobertsFacialEmotionRecognizerTheme {
                EmotionApp()
            }
        }
    }
}

fun loadModelFile(context: android.content.Context, modelName: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

fun classifyImage(bitmap: Bitmap, interpreter: Interpreter): FloatArray {
    val resized = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
    val input = Array(1) { Array(48) { Array(48) { FloatArray(1) } } }
    for (y in 0 until 48) {
        for (x in 0 until 48) {
            val pixel = resized.getPixel(x, y)
            val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114) / 255.0f
            input[0][y][x][0] = gray.toFloat()
        }
    }
    val output = Array(1) { FloatArray(7) }
    interpreter.run(input, output)
    return output[0]
}

@Composable
fun EmotionApp() {
    val context = LocalContext.current
    val models = listOf(
        "MobileNet — FER-2013-augmented" to "mobilenet_gpu_fer-2013-augmented.tflite",
        "V-CNN — CK+" to "vcnn_gpu_ck.tflite",
        "V-CNN — FER-2013-original" to "vcnn_gpu_fer-2013-original.tflite",
        "VRES-CNN — RAF-DB" to "vrescnn_gpu_raf_db.tflite"
    )

    val emotionMap = mapOf(
        "mobilenet_gpu_fer-2013-augmented.tflite" to listOf("Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise"),
        "vcnn_gpu_ck.tflite" to listOf("Angry", "Contempt", "Disgust", "Fear", "Happy", "Sadness", "Surprise"),
        "vcnn_gpu_fer-2013-original.tflite" to listOf("Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise"),
        "vrescnn_gpu_raf_db.tflite" to listOf("Surprise", "Fear", "Disgust", "Happy", "Sad", "Angry", "Neutral")
    )

    var selectedModelIndex by remember { mutableIntStateOf(0) }
    val emotions = emotionMap[models[selectedModelIndex].second]
        ?: listOf("Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise")
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scores by remember { mutableStateOf(FloatArray(7)) }
    var latency by remember { mutableStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            imageBitmap = bitmap
            val modelFile = models[selectedModelIndex].second
            val interpreter = Interpreter(loadModelFile(context, modelFile))
            val t1 = System.currentTimeMillis()
            scores = classifyImage(bitmap, interpreter)
            val t2 = System.currentTimeMillis()
            latency = (t2 - t1).toFloat()
            interpreter.close()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF1a1a2e))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("Facial Emotion Recognizer", color = androidx.compose.ui.graphics.Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Bare emotii
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0f3460))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Emoții detectate", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                emotions.forEachIndexed { i, emotion ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(emotion, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, modifier = Modifier.width(64.dp))
                        LinearProgressIndicator(
                            progress = { scores[i] },
                            modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(4.dp)),
                            color = androidx.compose.ui.graphics.Color(0xFF4ade80),
                            trackColor = androidx.compose.ui.graphics.Color(0xFF1a1a2e)
                        )
                        Text("${(scores[i] * 100).toInt()}%", color = androidx.compose.ui.graphics.Color(0xFF4ade80), fontSize = 12.sp, modifier = Modifier.width(40.dp).padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Imagine
        Card(modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0f3460))) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imageBitmap != null) {
                    Image(bitmap = imageBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Text("Nicio imagine selectată", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Latenta si emotie principala
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Latență: ${latency.toInt()} ms", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
            if (scores.max() > 0f) {
                val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
                Text("${emotions[maxIdx]}", color = androidx.compose.ui.graphics.Color(0xFF4ade80), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Buton galerie
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFe94560)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Alege imagine din galerie", color = androidx.compose.ui.graphics.Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selector model
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0f3460))) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Model activ", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1a1a2e)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(models[selectedModelIndex].first, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        models.forEachIndexed { index, (name, _) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                selectedModelIndex = index
                                expanded = false
                                imageBitmap = null
                                scores = FloatArray(7)
                                latency = 0f
                            })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) } }

