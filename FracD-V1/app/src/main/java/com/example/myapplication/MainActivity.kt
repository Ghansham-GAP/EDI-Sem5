package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        val selectButton = findViewById<Button>(R.id.selectButton)

        // ✅ Load TFLite model
        interpreter = Interpreter(loadModelFile())

        // ✅ Image Picker
        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                val prediction = runModel(bitmap)
                resultText.text = prediction
            }
        }

        selectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
    }

    // ✅ Load .tflite model from assets
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("efficientnet_gray_from_scratch.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // ✅ Run inference
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

        val output = Array(1) { FloatArray(2) }  // ✅ match model output [1,2]
        interpreter.run(input, output)


        return if (output[0][0] > 0.5f) "🩻 Fractured" else "✅ Normal"
    }
}
