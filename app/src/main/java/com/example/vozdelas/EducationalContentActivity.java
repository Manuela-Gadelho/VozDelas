package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent; // Importar Intent para iniciar a próxima Activity
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log; // Adicionado para logs de depuração
import java.util.ArrayList;
import java.util.List;

public class EducationalContentActivity extends AppCompatActivity {

    private static final String TAG = "EducationalContentAct"; // Tag para logs

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private List<String> categoryNames;
    private List<String> categoryDescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational_content);

        // --- Configuração da ActionBar ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Informações e Conscientização");
        }
        // ---------------------------------

        rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        categoryNames = new ArrayList<>();
        categoryDescriptions = new ArrayList<>();

        // --- Definição das Categorias Estáticas (Verifique se são EXATAMENTE iguais ao Firestore) ---
        categoryNames.add("Lei Maria da Penha");
        categoryDescriptions.add("Entenda a principal lei de proteção contra a violência doméstica.");

        categoryNames.add("Direitos Trabalhistas da Mulher");
        categoryDescriptions.add("Informações sobre licença maternidade, assédio no trabalho e mais.");

        categoryNames.add("Mitos e Verdades sobre a Violência");
        categoryDescriptions.add("Desmistifique conceitos comuns e entenda o ciclo da violência.");

        categoryNames.add("Direitos Reprodutivos e Saúde");
        categoryDescriptions.add("Conheça seus direitos relacionados à saúde da mulher e direitos reprodutivos.");

        categoryNames.add("Notícias e Casos de Sucesso");
        categoryDescriptions.add("Artigos e informações relevantes sobre igualdade de gênero e superação.");
        // ------------------------------------------

        // Inicializa e seta o adapter
        adapter = new CategoryAdapter(this, categoryNames, categoryDescriptions);
        rvCategories.setAdapter(adapter);

        // --- ESTE É O BLOCO QUE ESTAVA FALTANDO! ---
        // Configuração do Listener de Cliques no Adapter
        adapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String categoryName) {
                Log.d(TAG, "Categoria clicada: " + categoryName);
                Intent intent = new Intent(EducationalContentActivity.this, EducationalTopicListActivity.class);
                intent.putExtra("categoryName", categoryName); // Passa o nome da categoria
                startActivity(intent); // Inicia a próxima Activity
            }
        });
        // --------------------------------------------
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}