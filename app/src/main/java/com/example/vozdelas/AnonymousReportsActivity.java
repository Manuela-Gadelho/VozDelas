package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnonymousReportsActivity extends AppCompatActivity {

    private static final String TAG = "AnonymousReportsAct";

    private EditText etReportContent;
    private Button btnSubmitReport;
    private RecyclerView rvReports;

    private FirebaseFirestore db;
    private CollectionReference reportsRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private List<AnonymousReport> reportList;
    private AnonymousReportAdapter adapter;
    private ListenerRegistration reportsListenerRegistration; // Para gerenciar o listener em tempo real

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_reports);

        // Configurar a ActionBar (botão de voltar e título)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Denúncias Anônimas");
        }

        // Inicializar Views
        etReportContent = findViewById(R.id.et_report_content);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        rvReports = findViewById(R.id.rv_reports);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        reportsRef = db.collection("anonymousReports");
        auth = FirebaseAuth.getInstance();

        // Configurar RecyclerView
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        adapter = new AnonymousReportAdapter(this, reportList);
        rvReports.setAdapter(adapter);

        // Lógica de autenticação anônima: Garante que sempre há um usuário autenticado
        auth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInAnonymously:success");
                        currentUser = auth.getCurrentUser();
                        setupListeners(); // Configura os listeners após a autenticação
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(AnonymousReportsActivity.this, "Erro ao autenticar anonimamente. Tente novamente.", Toast.LENGTH_SHORT).show();
                        // Considere desabilitar a funcionalidade de envio se a autenticação falhar
                        btnSubmitReport.setEnabled(false);
                    }
                });
    }

    private void setupListeners() {
        // Listener para o botão de enviar denúncia
        btnSubmitReport.setOnClickListener(v -> submitReport());

        // Listener em tempo real para as denúncias
        reportsListenerRegistration = reportsRef
                .orderBy("timestamp", Query.Direction.DESCENDING) // Ordena da mais nova para a mais antiga
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            Toast.makeText(AnonymousReportsActivity.this, "Erro ao carregar denúncias.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            reportList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                AnonymousReport report = doc.toObject(AnonymousReport.class);
                                reportList.add(report);
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Denúncias carregadas. Total: " + reportList.size());
                        }
                    }
                });
    }

    private void submitReport() {
        String reportContent = etReportContent.getText().toString().trim();

        if (reportContent.isEmpty()) {
            Toast.makeText(this, "Por favor, digite sua denúncia.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Aguardando autenticação. Tente novamente em breve.", Toast.LENGTH_SHORT).show();
            // Tente autenticar novamente ou desabilite o botão até que esteja autenticado
            auth.signInAnonymously().addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    currentUser = auth.getCurrentUser();
                    submitReport(); // Tenta enviar novamente após autenticação
                }
            });
            return;
        }

        AnonymousReport newReport = new AnonymousReport(
                reportContent,
                Timestamp.now(), // Timestamp atual
                currentUser.getUid() // UID do usuário que postou anonimamente
        );

        reportsRef.add(newReport)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Denúncia enviada com sucesso!", Toast.LENGTH_SHORT).show();
                    etReportContent.setText(""); // Limpa o campo de texto
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao enviar denúncia: ", e);
                    Toast.makeText(this, "Erro ao enviar denúncia. Tente novamente.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Importante: Remover o listener para evitar vazamentos de memória e comportamentos indesejados
        if (reportsListenerRegistration != null) {
            reportsListenerRegistration.remove();
        }
    }

    // Configurar o botão de voltar na ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}