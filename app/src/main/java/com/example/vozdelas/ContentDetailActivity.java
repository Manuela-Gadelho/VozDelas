package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContentDetailActivity extends AppCompatActivity {

    private static final String TAG = "ContentDetailActivity";
    private TextView tvTitle, tvSubtitle, tvContent;
    private ImageView ivImage;
    private WebView wvVideo;
    private Button btnExternalLink;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalhes do Conteúdo");
        }

        tvTitle = findViewById(R.id.tv_detail_title);
        tvSubtitle = findViewById(R.id.tv_detail_subtitle);
        tvContent = findViewById(R.id.tv_detail_content);
        ivImage = findViewById(R.id.iv_detail_image);
        wvVideo = findViewById(R.id.wv_detail_video);
        btnExternalLink = findViewById(R.id.btn_external_link);

        db = FirebaseFirestore.getInstance();

        WebSettings webSettings = wvVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvVideo.setWebChromeClient(new WebChromeClient());

        String contentId = getIntent().getStringExtra("contentId");

        if (contentId != null) {
            loadContentDetails(contentId);
        } else {
            String title = getIntent().getStringExtra("contentTitle");
            String subtitle = getIntent().getStringExtra("contentSubtitle");
            String contentBody = getIntent().getStringExtra("contentBody");
            String imageUrl = getIntent().getStringExtra("imageUrl");
            String videoUrl = getIntent().getStringExtra("videoUrl");
            String externalLink = getIntent().getStringExtra("externalLink");

            displayContent(title, subtitle, contentBody, imageUrl, videoUrl, externalLink);
            Toast.makeText(this, "Aviso: Conteúdo carregado diretamente, sem ID do Firestore.", Toast.LENGTH_LONG).show();
        }

        btnExternalLink.setOnClickListener(v -> {
            String link = (String) v.getTag();
            if (link != null && !link.isEmpty()) {
                if (!link.startsWith("http://") && !link.startsWith("https://")) {
                    link = "http://" + link;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadContentDetails(String contentId) {
        db.collection("educationalContent").document(contentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                EducationalContent content = document.toObject(EducationalContent.class);
                                if (content != null) {
                                    displayContent(content.getTitle(), content.getSubtitle(), content.getContent(),
                                            content.getImageUrl(), content.getVideoUrl(), content.getExternalLink());
                                    if (getSupportActionBar() != null) {
                                        getSupportActionBar().setTitle(content.getTitle());
                                    }
                                }
                            } else {
                                Log.d(TAG, "Nenhum documento encontrado com o ID: " + contentId);
                                Toast.makeText(ContentDetailActivity.this, "Conteúdo não encontrado.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Log.e(TAG, "Erro ao carregar detalhes do conteúdo: ", task.getException());
                            Toast.makeText(ContentDetailActivity.this, "Erro ao carregar detalhes do conteúdo.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void displayContent(String title, String subtitle, String contentBody, String imageUrl, String videoUrl, String externalLink) {
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        tvContent.setText(contentBody);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(ivImage);
        } else {
            ivImage.setVisibility(View.GONE);
        }

        if (videoUrl != null && !videoUrl.isEmpty()) {
            wvVideo.setVisibility(View.VISIBLE);
            wvVideo.loadData(
                    "<html><body><iframe width=\"100%\" height=\"100%\" src=\"" + videoUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>",
                    "text/html", "utf-8"
            );
        } else {
            wvVideo.setVisibility(View.GONE);
        }

        if (externalLink != null && !externalLink.isEmpty()) {
            btnExternalLink.setVisibility(View.VISIBLE);
            btnExternalLink.setTag(externalLink);
        } else {
            btnExternalLink.setVisibility(View.GONE);
        }
    }
}