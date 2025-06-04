package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem; // Importante para a ActionBar
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContactsActivity extends AppCompatActivity implements EmergencyContactAdapter.OnItemClickListener {

    private static final String TAG = "EmergencyContactsActivity";

    private RecyclerView rvEmergencyContacts;
    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList;
    private FloatingActionButton fabAddContact;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 1003;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        // --- Habilitar e configurar a ActionBar padrão ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita a seta de voltar
            getSupportActionBar().setTitle("Contatos de Emergência"); // Define o título
        }
        // ------------------------------------------

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Você precisa estar logado para gerenciar contatos.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(EmergencyContactsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        rvEmergencyContacts = findViewById(R.id.rv_emergency_contacts);
        fabAddContact = findViewById(R.id.fab_add_contact);

        contactList = new ArrayList<>();
        adapter = new EmergencyContactAdapter(contactList);
        adapter.setOnItemClickListener(this);

        rvEmergencyContacts.setLayoutManager(new LinearLayoutManager(this));
        rvEmergencyContacts.setAdapter(adapter);

        loadEmergencyContacts();

        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEditContactDialog(null);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadEmergencyContacts() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId é nulo ao tentar carregar contatos.");
            return;
        }

        Log.d(TAG, "Tentando carregar contatos para o usuário: " + currentUserId);
        db.collection("users")
                .document(currentUserId)
                .collection("emergencyContacts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            contactList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EmergencyContact contact = document.toObject(EmergencyContact.class);
                                contact.setId(document.getId());
                                contactList.add(contact);
                                Log.d(TAG, "Contato carregado: " + contact.getName() + " - " + contact.getPhone());
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Contatos carregados com sucesso. Total: " + contactList.size());

                            if (contactList.isEmpty()) {
                                Toast.makeText(EmergencyContactsActivity.this, "Nenhum contato de emergência. Adicione um!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Erro ao carregar contatos: ", task.getException());
                            Toast.makeText(EmergencyContactsActivity.this, "Erro ao carregar contatos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showAddEditContactDialog(final EmergencyContact contactToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        EditText etName = dialogView.findViewById(R.id.et_dialog_name);
        EditText etPhone = dialogView.findViewById(R.id.et_dialog_phone);
        builder.setView(dialogView);

        if (contactToEdit != null) {
            dialogTitle.setText("Editar Contato");
            etName.setText(contactToEdit.getName());
            etPhone.setText(contactToEdit.getPhone());
            builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                        Toast.makeText(EmergencyContactsActivity.this, "Nome e telefone são obrigatórios.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateContactInFirestore(contactToEdit.getId(), name, phone);
                }
            });
        } else {
            dialogTitle.setText("Adicionar Contato");
            builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                        Toast.makeText(EmergencyContactsActivity.this, "Nome e telefone são obrigatórios.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveContactToFirestore(name, phone);
                }
            });
        }

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void saveContactToFirestore(String name, String phone) {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId é nulo ao tentar salvar contato.");
            return;
        }

        Log.d(TAG, "Tentando salvar contato: " + name + " - " + phone);
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", name);
        contact.put("phone", phone);

        db.collection("users")
                .document(currentUserId)
                .collection("emergencyContacts")
                .add(contact)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Contato adicionado com sucesso! ID: " + documentReference.getId());
                        Toast.makeText(EmergencyContactsActivity.this, "Contato adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                        loadEmergencyContacts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Erro ao adicionar contato: ", e);
                        Toast.makeText(EmergencyContactsActivity.this, "Erro ao adicionar contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateContactInFirestore(String contactId, String newName, String newPhone) {
        if (currentUserId == null || contactId == null) {
            Log.w(TAG, "currentUserId ou contactId é nulo ao tentar atualizar contato.");
            return;
        }

        Log.d(TAG, "Tentando atualizar contato com ID: " + contactId + " para: " + newName + " - " + newPhone);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);

        db.collection("users")
                .document(currentUserId)
                .collection("emergencyContacts")
                .document(contactId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Contato atualizado com sucesso! ID: " + contactId);
                        Toast.makeText(EmergencyContactsActivity.this, "Contato atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        loadEmergencyContacts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Erro ao atualizar contato: ", e);
                        Toast.makeText(EmergencyContactsActivity.this, "Erro ao atualizar contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteClick(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Contato")
                .setMessage("Tem certeza que deseja excluir " + contact.getName() + "?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContactFromFirestore(contact.getId());
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onEditClick(EmergencyContact contact) {
        showAddEditContactDialog(contact);
    }


    private void deleteContactFromFirestore(String contactId) {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId é nulo ao tentar excluir contato.");
            return;
        }

        Log.d(TAG, "Tentando excluir contato com ID: " + contactId);
        db.collection("users")
                .document(currentUserId)
                .collection("emergencyContacts")
                .document(contactId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Contato excluído com sucesso! ID: " + contactId);
                        Toast.makeText(EmergencyContactsActivity.this, "Contato excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        loadEmergencyContacts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Erro ao excluir contato: ", e);
                        Toast.makeText(EmergencyContactsActivity.this, "Erro ao excluir contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCallClick(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION_REQUEST_CODE);
        } else {
            initiateCall(phoneNumber);
        }
    }

    private void initiateCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permissão para fazer chamadas negada.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de chamada concedida. Tente novamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão para fazer chamadas negada.", Toast.LENGTH_LONG).show();
            }
        }
    }
}