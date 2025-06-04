package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SupportNetworkActivity extends AppCompatActivity {

    private static final String TAG = "SupportNetworkActivity";
    private RecyclerView rvOrganizations;
    private SupportOrganizationAdapter adapter;
    private List<SupportOrganization> organizationList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_network);

        // Configurar a ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Exibe o botão de voltar
            getSupportActionBar().setTitle("Rede de Apoio"); // Defina o título
        }

        db = FirebaseFirestore.getInstance();

        rvOrganizations = findViewById(R.id.rv_support_organizations);
        rvOrganizations.setLayoutManager(new LinearLayoutManager(this));
        organizationList = new ArrayList<>();
        adapter = new SupportOrganizationAdapter(organizationList);
        rvOrganizations.setAdapter(adapter);

        loadSupportOrganizations();
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

    private void loadSupportOrganizations() {
        db.collection("supportOrganizations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            organizationList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SupportOrganization organization = document.toObject(SupportOrganization.class);
                                organizationList.add(organization);
                            }
                            adapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram
                            Log.d(TAG, "Organizações de apoio carregadas. Total: " + organizationList.size());
                            if (organizationList.isEmpty()) {
                                Toast.makeText(SupportNetworkActivity.this, "Nenhuma organização de apoio encontrada.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Erro ao carregar organizações de apoio: ", task.getException());
                            Toast.makeText(SupportNetworkActivity.this, "Erro ao carregar organizações de apoio.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}