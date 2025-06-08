package com.example.deepcook

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private val ingredients = listOf(
        "egg", "milk", "flour", "rice", "meat", "fish", "onion", "garlic"
    )
    private val recipes = mapOf(
        "한식" to listOf(
            "비빔밥", "김치찌개", "된장찌개", "불고기", "갈비찜",
            "김밥", "제육볶음", "잡채", "갈비탕", "삼계탕"
        ),
        "양식" to listOf(
            "스파게티", "피자", "스테이크", "리조또", "샐러드",
            "라자냐", "파니니", "버거", "수프", "치킨 파스타"
        ),
        "일식" to listOf(
            "초밥", "라멘", "규동", "돈까스", "우동",
            "덴푸라", "오코노미야키", "가라아게", "야키소바", "미소시루"
        ),
        "중식" to listOf(
            "짜장면", "짬뽕", "탕수육", "깐풍기", "마파두부",
            "양장피", "볶음밥", "군만두", "훠궈", "라조기"
        ),
        "야식" to listOf(
            "라면", "떡볶이", "순대", "치킨", "야식 피자",
            "분식 세트", "김치전", "부침개", "닭발", "계란말이"
        )
    )

    private val pickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let { processImage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        findViewById<Button>(R.id.pickButton).setOnClickListener { pickImage() }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickerLauncher.launch(intent)
    }

    private fun processImage(uri: Uri) {
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        imageView.setImageBitmap(bitmap)
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                resultText.text = text
                val found = ingredients.filter { text.contains(it, ignoreCase = true) }
                val recommendations = recipes.mapValues { entry ->
                    entry.value.filter { recipe ->
                        found.any { ingredient -> recipe.contains(ingredient, ignoreCase = true) }
                    }.ifEmpty { entry.value }
                }
                val sb = StringBuilder()
                for ((category, list) in recommendations) {
                    sb.append("$category:\n")
                    list.take(10).forEach { sb.append("- $it\n") }
                }
                resultText.text = sb.toString()
            }
            .addOnFailureListener { e ->
                resultText.text = e.localizedMessage
            }
    }
}

