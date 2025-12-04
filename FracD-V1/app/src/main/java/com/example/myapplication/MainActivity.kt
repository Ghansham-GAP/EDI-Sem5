package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var selectButton: Button
    private lateinit var predictButton: Button
    private lateinit var historyButton: Button
    private lateinit var progressBar: ProgressBar
    private var selectedBitmap: Bitmap? = null
    private var selectedImageUri: Uri? = null

    companion object {
        val historyList = ArrayList<HistoryItem>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        selectButton = findViewById(R.id.selectButton)
        predictButton = findViewById(R.id.predictButton)
        historyButton = findViewById(R.id.historyButton)
        progressBar = findViewById(R.id.progressBar)

        // Load TFLite model
        interpreter = Interpreter(loadModelFile())

        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(selectedBitmap)
            }
        }

        selectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        predictButton.setOnClickListener {
            selectedBitmap?.let {
                progressBar.visibility = View.VISIBLE
                resultText.visibility = View.GONE

                Thread {
                    val prediction = runModel(it)
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val currentDate = sdfDate.format(Date())
                    val currentTime = sdfTime.format(Date())
                    historyList.add(HistoryItem(selectedImageUri.toString(), prediction, currentDate, currentTime))

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        resultText.visibility = View.VISIBLE
                        resultText.text = prediction
                        if (prediction.contains("Fractured")) {
                            resultText.setTextColor(ContextCompat.getColor(this, R.color.colorError))
                        } else {
                            resultText.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
                        }
                    }
                }.start()
            }
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("efficientnet_gray_from_scratch.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun runModel(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(1) } } }

        for (x in 0 until 224) {
            for (y in 0 until 224) {
                val pixel = resized.getPixel(x, y)
                val gray = ((pixel shr 16 and 0xFF) * 0.3f +
                        (pixel shr 8 and 0xFF) * 0.59f +
                        (pixel and 0xFF) * 0.11f) / 255f
                input[0][x][y][0] = gray
            }
        }

        val output = Array(1) { FloatArray(2) }
        interpreter.run(input, output)

        return if (output[0][0] > 0.5f) "🩻 Fractured" else "✅ Normal"
    }
}
