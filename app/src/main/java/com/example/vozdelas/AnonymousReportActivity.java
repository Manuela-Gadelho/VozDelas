package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Date; // Para registrar a data e hora da denúncia

public class AnonymousReportActivity extends AppCompatActivity {

    private static final String TAG = "AnonymousReportActivity";
    private EditText etReportDescription;
    private Button btnSubmitReport;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_report);

        // Configurar a ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Exibe o botão de voltar
            getSupportActionBar().setTitle("Denúncias Anônimas"); // Defina o título
        }

        db = FirebaseFirestore.getInstance(); // Inicializa o Firestore

        etReportDescription = findViewById(R.id.et_report_description);
        btnSubmitReport = findViewById(R.id.btn_submit_report);

        btnSubmitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnonymousReport();
            }
        });
    }

    // Método para lidar com o clique no botão de voltar da ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Retorna para a atividade anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitAnonymousReport() {
        String reportText = etReportDescription.getText().toString().trim();

        if (TextUtils.isEmpty(reportText)) {
            Toast.makeText(this, "Por favor, preencha a descrição da denúncia.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar um mapa de dados para a denúncia
        Map<String, Object> report = new HashMap<>();
        report.put("description", reportText);
        report.put("timestamp", new Date()); // Adiciona a data e hora do envio
        // NÃO ADICIONE NENHUM DADO QUE POSSA IDENTIFICAR O USUÁRIO (ex: UID) AQUI

        // Salvar a denúncia em uma coleção no Firestore
        db.collection("anonymousReports")
                .add(report)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Denúncia anônima enviada com sucesso! ID: " + documentReference.getId());
                        Toast.makeText(AnonymousReportActivity.this, "Denúncia enviada com sucesso. Agradecemos sua contribuição.", Toast.LENGTH_LONG).show();
                        etReportDescription.setText(""); // Limpa o campo de texto
                        // Opcional: Voltar para a tela anterior
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Erro ao enviar denúncia anônima: ", e);
                        Toast.makeText(AnonymousReportActivity.this, "Erro ao enviar denúncia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}