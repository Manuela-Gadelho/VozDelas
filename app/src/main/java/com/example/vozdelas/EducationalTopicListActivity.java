package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EducationalTopicListActivity extends AppCompatActivity {

    private static final String TAG = "EducationalTopicListAct";
    private RecyclerView rvEducationalTopicItems;
    private EducationalTopicAdapter adapter;
    private List<EducationalContent> contentList;
    private FirebaseFirestore db;
    private TextView tvEducationalTopicListTitle;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational_topic_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tópicos Educacionais"); // Título padrão
        }

        tvEducationalTopicListTitle = findViewById(R.id.tv_educational_topic_list_title);
        rvEducationalTopicItems = findViewById(R.id.rv_educational_topic_items);
        rvEducationalTopicItems.setLayoutManager(new LinearLayoutManager(this));

        contentList = new ArrayList<>();
        adapter = new EducationalTopicAdapter(this, contentList);
        rvEducationalTopicItems.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("categoryName")) {
            selectedCategory = getIntent().getStringExtra("categoryName");
            tvEducationalTopicListTitle.setText(selectedCategory); // Define o título da tela
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(selectedCategory); // Define o título da ActionBar
            }
            Log.d(TAG, "Categoria clicada: " + selectedCategory); // Loga a categoria clicada
            loadContentByCategory(selectedCategory);
        } else {
            Toast.makeText(this, "Categoria não especificada.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadContentByCategory(String category) {
        Log.d(TAG, "Executando query para categoria: '" + category + "'");
        db.collection("educationalContent")
                .whereEqualTo("category", category)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .orderBy("__name__", Query.Direction.DESCENDING) // *** LINHA CORRIGIDA: Adicionado o __name__ ***
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            contentList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EducationalContent content = document.toObject(EducationalContent.class);
                                content.setId(document.getId()); // Garante que o ID do documento é setado no objeto
                                contentList.add(content);
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Conteúdo carregado para a categoria " + category + ". Total: " + contentList.size());
                            if (contentList.isEmpty()) {
                                Toast.makeText(EducationalTopicListActivity.this, "Nenhum conteúdo encontrado para esta categoria.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Erro ao carregar conteúdo: ", task.getException());
                            Toast.makeText(EducationalTopicListActivity.this, "Erro ao carregar conteúdo.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}