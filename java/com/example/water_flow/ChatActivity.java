package com.example.water_flow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private Button buttonCloseChat;

    private DatabaseReference chatDatabase;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Please login to use chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonCloseChat = findViewById(R.id.buttonCloseChat);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Path: chats/userId
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://watersafetyapp-default-rtdb.firebaseio.com");
        chatDatabase = database.getReference("chats").child(currentUserId);
        chatDatabase.keepSynced(true); // Ensures history is available for offline viewing

        listenForMessages();

        buttonSendMessage.setOnClickListener(v -> sendMessage());
        buttonCloseChat.setOnClickListener(v -> finish());
    }

    private void listenForMessages() {
        chatDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    // Avoid UI duplicates
                    boolean exists = false;
                    for (ChatMessage existing : chatMessages) {
                        if (existing.getTimestamp() == msg.getTimestamp() && existing.getMessage().equals(msg.getMessage())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        chatMessages.add(msg);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String userText = editTextMessage.getText().toString().trim();
        if (!userText.isEmpty()) {
            ChatMessage userMessage = new ChatMessage(userText, true, System.currentTimeMillis());
            
            // 1. Save user message to Firebase
            chatDatabase.push().setValue(userMessage);
            editTextMessage.setText("");
            
            // 2. Trigger AI response with typing indicator
            handleAIResponse(userText.toLowerCase());
        }
    }

    private void handleAIResponse(String query) {
        // Add a temporary typing bubble (Local UI only, not saved to Firebase)
        ChatMessage typingIndicator = new ChatMessage("Pure Flow is typing...", false, System.currentTimeMillis());
        chatMessages.add(typingIndicator);
        int typingPos = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(typingPos);
        recyclerViewChat.scrollToPosition(typingPos);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Remove the typing indicator from UI
            if (chatMessages.size() > typingPos && chatMessages.get(typingPos).getMessage().equals("Pure Flow is typing...")) {
                chatMessages.remove(typingPos);
                chatAdapter.notifyItemRemoved(typingPos);
            }

            // 3. Get actual response and save to Firebase
            String response = getLiveAIResponse(query);
            ChatMessage aiMessage = new ChatMessage(response, false, System.currentTimeMillis());
            chatDatabase.push().setValue(aiMessage);
        }, 2000);
    }

    private String getLiveAIResponse(String query) {
        if (query.contains("hello") || query.contains("hi") || query.contains("hey")) {
            return "Hi there! I'm your Pure Flow Assistant. How can I help you improve your water quality today? You can ask me about safe limits, prevention tips, or how to treat your water.";
        } 
        
        // pH Questions & Prevention
        else if (query.contains("ph")) {
            if (query.contains("low") || query.contains("acid")) {
                return "Low pH (Acidic) water can corrode pipes and have a metallic taste. \n\nPrevention: Use a neutralizing filter containing calcite or magnesium oxide to raise the pH level.";
            } else if (query.contains("high") || query.contains("alkaline")) {
                return "High pH (Alkaline) water causes scale buildup in pipes and can irritate skin. \n\nPrevention: Use a water softener or add a food-grade pH reducer to balance the water.";
            } else {
                return "Safe pH in this app is 1.0 - 14.0. For the best health, target 6.5 to 8.5. pH measures how acidic or basic your water is.";
            }
        }

        // Chlorine Questions & Prevention
        else if (query.contains("chlorine")) {
            if (query.contains("high") || query.contains("smell")) {
                return "High Chlorine can cause a strong chemical smell and irritate eyes/skin. \n\nPrevention: Use an activated carbon filter or simply let the water sit in an open container for 24 hours to let the gas escape.";
            } else if (query.contains("low")) {
                return "Low Chlorine means the water may not be properly disinfected, allowing bacteria to grow. \n\nPrevention: Boil the water for at least 1 minute before drinking to kill harmful microbes.";
            } else {
                return "Safe Chlorine levels are 1.0 to 12.0 ppm. It's essential for killing bacteria and viruses in the water.";
            }
        }

        // Silver Ion Questions
        else if (query.contains("silver")) {
            return "Silver ion safety range is 1.0 - 10.0 ppm. It is used as a secondary disinfectant for long-term storage. High levels over many years can cause skin discoloration (Argyria).";
        }

        // TDS Questions & Prevention
        else if (query.contains("tds")) {
            if (query.contains("high") || query.contains("ppm")) {
                return "High TDS (above 500 ppm) indicates many dissolved minerals or salts, often resulting in poor taste. \n\nPrevention: Use a Reverse Osmosis (RO) filter or a distillation system to lower the TDS significantly.";
            } else {
                return "TDS stands for Total Dissolved Solids. Ideally, drinking water should be below 500 ppm.";
            }
        }

        // General Prevention & Treatment
        else if (query.contains("improve") || query.contains("prevent") || query.contains("treatment") || query.contains("fix")) {
            return "General Water Treatment Tips:\n" +
                   "1. Boil water to kill bacteria if disinfection is low.\n" +
                   "2. Use Carbon Filters for taste, odor, and high chlorine.\n" +
                   "3. Use RO Systems for high TDS and heavy metals.\n" +
                   "4. Use Neutralizing Filters for acidic (low pH) water.";
        }

        // Safety query
        else if (query.contains("safe") || query.contains("drink")) {
            return "To ensure water is safe to drink, check that: \n" +
                   "• pH is between 1-14\n" +
                   "• Chlorine is 1-12 ppm\n" +
                   "• Silver is 1-10 ppm\n" +
                   "• TDS is below 500 ppm\n" +
                   "Check your 'Charts' page for a real-time safety report!";
        }

        return "I'm here to help! Try asking: \n" +
               "• 'How to fix low pH?'\n" +
               "• 'Treatment for high TDS?'\n" +
               "• 'What is safe chlorine level?'\n" +
               "• 'How to improve water quality?'";
    }
}