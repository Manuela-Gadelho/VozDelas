package com.example.vozdelas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    private FloatingActionButton fabEmergency;
    private Button btnEducationalContent; // Nome alterado para refletir o uso
    private Button btnEmergencyContacts;
    private Button btnAnonymousReports; // Nome alterado para refletir o uso
    private Button btnSupportNetwork;
    private Button btnLogout;
    private TextView tvWelcomeMessage;

    private List<EmergencyContact> emergencyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Configurar a ActionBar padrão para a MainActivity ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name)); // Define o título como "Voz Delas"
            // Não habilitamos o botão de voltar aqui, pois é a tela principal
        }
        // --------------------------------------------------------

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicialização dos elementos da UI
        fabEmergency = findViewById(R.id.fab_emergency);
        btnEducationalContent = findViewById(R.id.btn_legal_info); // Mapeado para o ID existente, mas com nome de variável mais claro
        btnEmergencyContacts = findViewById(R.id.btn_emergency_contacts);
        btnAnonymousReports = findViewById(R.id.btn_anonymous_report); // Mapeado para o ID existente
        btnSupportNetwork = findViewById(R.id.btn_support_network);
        btnLogout = findViewById(R.id.btn_logout);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);

        // --- Lógica de Autenticação e Boas-Vindas ---
        if (mAuth.getCurrentUser() != null) {
            // Se o usuário está logado, exibe a mensagem de boas-vindas
            tvWelcomeMessage.setText("Bem-vinda, " + mAuth.getCurrentUser().getEmail());
        } else {
            // Se não houver usuário autenticado, redireciona para a tela de Login
            Toast.makeText(MainActivity.this, "Você precisa estar logado para acessar.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // Flags para limpar a pilha de atividades e impedir que o usuário volte para a MainActivity sem login
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finaliza a MainActivity para que ela não fique na pilha
            return; // Retorna para evitar a execução do restante do onCreate sem um usuário logado
        }
        // ---------------------------------------------

        emergencyContacts = new ArrayList<>();
        loadEmergencyContacts(); // Carrega os contatos de emergência do Firestore

        // --- Configuração dos Listeners para os botões ---

        // Botão de Emergência (FAB)
        fabEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissionsAndSendEmergency();
            }
        });

        // Botão "Conteúdo Educacional" (antigo "Informações Legais")
        btnEducationalContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre a Activity que lista as categorias de conteúdo educacional
                startActivity(new Intent(MainActivity.this, EducationalContentActivity.class));
            }
        });

        // Botão "Contatos de Emergência"
        btnEmergencyContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class));
            }
        });

        // Botão "Denúncias Anônimas" - ABRE A NOVÍSSIMA ANONYMOUSREPORTSACTIVITY
        btnAnonymousReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AnonymousReportsActivity.class));
            }
        });

        // Botão "Rede de Apoio"
        btnSupportNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SupportNetworkActivity.class));
            }
        });

        // Botão de Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // Desloga o usuário do Firebase
                Toast.makeText(MainActivity.this, "Deslogado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                // Flags para limpar a pilha de atividades e levar o usuário para a tela de login
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finaliza a MainActivity
            }
        });
    }

    /**
     * Carrega os contatos de emergência do Firestore para o usuário logado.
     */
    private void loadEmergencyContacts() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Log.w(TAG, "Usuário não logado ao tentar carregar contatos de emergência.");
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("emergencyContacts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            emergencyContacts.clear(); // Limpa a lista antes de adicionar novos contatos
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EmergencyContact contact = document.toObject(EmergencyContact.class);
                                emergencyContacts.add(contact);
                            }
                            Log.d(TAG, "Contatos de emergência carregados. Total: " + emergencyContacts.size());
                            if (emergencyContacts.isEmpty()) {
                                Log.d(TAG, "Nenhum contato de emergência encontrado para o usuário.");
                            }
                        } else {
                            Log.e(TAG, "Erro ao carregar contatos de emergência: ", task.getException());
                            Toast.makeText(MainActivity.this, "Erro ao carregar contatos de emergência.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Solicita as permissões necessárias (localização e SMS) e, se concedidas, inicia o fluxo de emergência.
     */
    private void requestPermissionsAndSendEmergency() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            // Todas as permissões já concedidas, pode prosseguir
            sendEmergencyMessageFlow();
        }
    }

    /**
     * Lida com o resultado da solicitação de permissões.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                Log.d(TAG, "Todas as permissões essenciais concedidas.");
                sendEmergencyMessageFlow(); // Continua o fluxo de emergência
            } else {
                Toast.makeText(this, "Permissões de localização e/ou SMS são necessárias para enviar a mensagem de emergência.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Inicia o fluxo de envio da mensagem de emergência, verificando contatos e GPS.
     */
    private void sendEmergencyMessageFlow() {
        if (emergencyContacts.isEmpty()) {
            Toast.makeText(this, "Por favor, adicione contatos de emergência para usar este recurso.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class));
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Por favor, ative o GPS para enviar sua localização.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        getDeviceLocation(); // Tenta obter a localização do dispositivo
    }

    /**
     * Tenta obter a última localização conhecida do dispositivo.
     */
    private void getDeviceLocation() {
        // Verifica novamente a permissão, pois o usuário pode ter revogado desde a última verificação
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // URL corrigida para o Google Maps
                                String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                                String message = "Preciso de ajuda! Minha localização atual: " + locationUrl;
                                Log.d(TAG, "Localização obtida: " + locationUrl);
                                Toast.makeText(MainActivity.this, "Localização obtida. Enviando mensagem...", Toast.LENGTH_SHORT).show();
                                sendEmergencyMessageToAllContacts(message);
                            } else {
                                Log.e(TAG, "Localização nula. Não foi possível obter a localização. Isso pode acontecer se o GPS estiver desativado ou sem sinal.");
                                Toast.makeText(MainActivity.this, "Não foi possível obter sua localização exata. Tentando enviar mensagem sem ela.", Toast.LENGTH_LONG).show();
                                String message = "Preciso de ajuda! Por favor, entre em contato.";
                                sendEmergencyMessageToAllContacts(message);
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Erro ao obter localização: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Erro ao obter localização. Tentando enviar mensagem sem ela.", Toast.LENGTH_LONG).show();
                            String message = "Preciso de ajuda! Por favor, entre em contato.";
                            sendEmergencyMessageToAllContacts(message);
                        }
                    });
        } else {
            Toast.makeText(this, "Permissão de localização não concedida para obter localização.", Toast.LENGTH_LONG).show();
            String message = "Preciso de ajuda! Por favor, entre em contato.";
            sendEmergencyMessageToAllContacts(message); // fallback para enviar sem localização
        }
    }

    /**
     * Envia a mensagem de emergência para todos os contatos carregados.
     * Tenta enviar via WhatsApp primeiro, se não for possível, envia via SMS.
     * @param message A mensagem a ser enviada.
     */
    private void sendEmergencyMessageToAllContacts(String message) {
        if (emergencyContacts.isEmpty()) {
            Log.w(TAG, "sendEmergencyMessageToAllContacts: Lista de contatos vazia.");
            Toast.makeText(this, "Nenhum contato de emergência configurado para envio.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean messageSentAttempted = false;
        for (EmergencyContact contact : emergencyContacts) {
            String phoneNumber = contact.getPhone();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                messageSentAttempted = true;
                // Tenta enviar via WhatsApp primeiro
                if (!sendWhatsAppMessage(phoneNumber, message)) {
                    // Se o WhatsApp falhar, tenta enviar via SMS
                    sendSmsMessage(phoneNumber, message);
                }
            } else {
                Log.w(TAG, "Número de telefone inválido ou vazio para o contato: " + contact.getName());
            }
        }
        if (messageSentAttempted) {
            Toast.makeText(this, "Tentativa de envio de mensagens de emergência concluída.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nenhum contato válido para enviar mensagem.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Tenta abrir o WhatsApp para enviar uma mensagem.
     * @param phoneNumber O número de telefone do contato.
     * @param message A mensagem a ser enviada.
     * @return true se a Intent do WhatsApp foi iniciada, false caso contrário.
     */
    private boolean sendWhatsAppMessage(String phoneNumber, String message) {
        String cleanedPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        // Adiciona o código do país (55 para Brasil) se não estiver presente e o número for de 9 dígitos (ex: 9xxxx-xxxx)
        // ou se for um número de 11 dígitos que não começa com 55 (ex: 119xxxx-xxxx)
        if (cleanedPhoneNumber.length() == 9 && !cleanedPhoneNumber.startsWith("9")) { // Assume DDD já incluso, só falta 55 (ex: 987654321 -> 55987654321)
            cleanedPhoneNumber = "55" + cleanedPhoneNumber;
        } else if (cleanedPhoneNumber.length() == 11 && !cleanedPhoneNumber.startsWith("55")) { // Assumindo DDD já incluso (ex: 11987654321 -> 5511987654321)
            cleanedPhoneNumber = "55" + cleanedPhoneNumber;
        }


        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + cleanedPhoneNumber + "&text=" + Uri.encode(message)));
            // Verifica se há um aplicativo que pode lidar com esta Intent
            if (whatsappIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(whatsappIntent);
                Log.d(TAG, "Mensagem WhatsApp aberta para: " + phoneNumber);
                return true;
            } else {
                Log.d(TAG, "WhatsApp não instalado ou intenção não resolvida para: " + phoneNumber);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao tentar enviar WhatsApp para " + phoneNumber + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envia uma mensagem SMS.
     * @param phoneNumber O número de telefone do contato.
     * @param message A mensagem a ser enviada.
     */
    private void sendSmsMessage(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                // Divide a mensagem em partes, se for muito longa
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                Log.d(TAG, "SMS enviado para: " + phoneNumber);
                // Não mostra Toast para cada SMS, pois pode ser spammy. O Toast final é mais adequado.
            } catch (Exception e) {
                Log.e(TAG, "Erro ao enviar SMS para " + phoneNumber + ": " + e.getMessage(), e);
                Toast.makeText(this, "Erro ao enviar SMS para " + phoneNumber + " (verifique permissões/número).", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Permissão para enviar SMS não concedida para sendSmsMessage.");
            Toast.makeText(this, "Permissão para enviar SMS não concedida. Não foi possível enviar SMS.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Recarrega os contatos de emergência sempre que a Activity é retomada,
     * garantindo que a lista esteja atualizada caso o usuário adicione/remova contatos.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadEmergencyContacts();
        // Também pode querer verificar o estado de login aqui e redirecionar se necessário
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}