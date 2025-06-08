package com.example.deepcook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView resultText;

    private final List<String> ingredients = Arrays.asList(
            "egg", "milk", "flour", "rice", "meat", "fish", "onion", "garlic"
    );

    private final Map<String, List<String>> recipes = new LinkedHashMap<>();

    private ActivityResultLauncher<Intent> pickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        findViewById(R.id.pickButton).setOnClickListener(v -> pickImage());

        populateRecipes();
        pickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            processImage(data.getData());
                        }
                    }
                });
    }

    private void populateRecipes() {
        recipes.put("한식", Arrays.asList(
                "비빔밥", "김치찌개", "된장찌개", "불고기", "갈비찜",
                "김밥", "제육볶음", "잡채", "갈비탕", "삼계탕"
        ));
        recipes.put("양식", Arrays.asList(
                "스파게티", "피자", "스테이크", "리조또", "샐러드",
                "라자냐", "파니니", "버거", "수프", "치킨 파스타"
        ));
        recipes.put("일식", Arrays.asList(
                "초밥", "라멘", "규동", "돈까스", "우동",
                "덴푸라", "오코노미야키", "가라아게", "야키소바", "미소시루"
        ));
        recipes.put("중식", Arrays.asList(
                "짜장면", "짬뽕", "탕수육", "깐풍기", "마파두부",
                "양장피", "볶음밥", "군만두", "훠궈", "라조기"
        ));
        recipes.put("야식", Arrays.asList(
                "라면", "떡볶이", "순대", "치킨", "야식 피자",
                "분식 세트", "김치전", "부침개", "닭발", "계란말이"
        ));
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickerLauncher.launch(intent);
    }

    private void processImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();
                        List<String> found = new ArrayList<>();
                        for (String ingredient : ingredients) {
                            if (text.toLowerCase().contains(ingredient.toLowerCase())) {
                                found.add(ingredient);
                            }
                        }

                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, List<String>> entry : recipes.entrySet()) {
                            sb.append(entry.getKey()).append(":\n");
                            List<String> filtered = new ArrayList<>();
                            for (String recipe : entry.getValue()) {
                                for (String ing : found) {
                                    if (recipe.toLowerCase().contains(ing.toLowerCase())) {
                                        filtered.add(recipe);
                                        break;
                                    }
                                }
                            }
                            if (filtered.isEmpty()) {
                                filtered = entry.getValue();
                            }
                            int count = Math.min(10, filtered.size());
                            for (int i = 0; i < count; i++) {
                                sb.append("- ").append(filtered.get(i)).append("\n");
                            }
                        }
                        resultText.setText(sb.toString());
                    })
                    .addOnFailureListener(e -> resultText.setText(e.getLocalizedMessage()));
        } catch (IOException e) {
            resultText.setText(e.getLocalizedMessage());
        }
    }
}
