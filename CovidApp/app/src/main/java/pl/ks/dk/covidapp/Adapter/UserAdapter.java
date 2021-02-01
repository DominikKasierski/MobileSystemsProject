package pl.ks.dk.covidapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.ks.dk.covidapp.MessageActivity;
import pl.ks.dk.covidapp.Model.Chat;
import pl.ks.dk.covidapp.Model.User;
import pl.ks.dk.covidapp.R;
import pl.ks.dk.covidapp.RegisterActivity;
import pl.ks.dk.covidapp.ResetPasswordActivity;
import pl.ks.dk.covidapp.ShowDiagnosis;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context mContext;
    private final List<User> mUsers;
    private final boolean ischat;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat) {
            lastMessage(user.getId(), holder.last_msg);
            holder.show_answers.setVisibility(View.GONE);
        } else {
            holder.last_msg.setVisibility(View.GONE);
            holder.show_answers.setVisibility(View.VISIBLE);
        }

        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (checkIfStillWaiting(user.getId())) {
                                    updateWaitingForDiagnosis(user.getId());
                                    Intent intent = new Intent(mContext, MessageActivity.class);
                                    intent.putExtra("userid", user.getId());
                                    mContext.startActivity(intent);
                                } else {
                                    Toast.makeText(mContext, "Unfortunately, this ticket has been already taken.", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getResources().getString(R.string.confirmation))
                        .setMessage(mContext.getResources().getString(R.string.take_patient))
                        .setPositiveButton(mContext.getResources().getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(mContext.getResources().getString(R.string.no), dialogClickListener).show();
            }
        });

        holder.show_answers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> answers = new ArrayList<>();
                ArrayList<String> questions = new ArrayList<>();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diagnosis").child(user.getId());
                Query query = reference.orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            answers.add((String) child.getValue());
                            questions.add(child.getKey());
                        }
                        Intent intent = new Intent(mContext, ShowDiagnosis.class);
                        intent.putStringArrayListExtra("ANSWERS", answers);
                        intent.putStringArrayListExtra("QUESTIONS", questions);
                        intent.putExtra("NAME", user.getName());
                        intent.putExtra("SURNAME", user.getSurname());
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profile_image;
        private final ImageView img_on;
        private final ImageView img_off;
        private final TextView last_msg;
        private final Button show_answers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            show_answers = itemView.findViewById(R.id.answer_button);
        }
    }

    private void lastMessage(String userid, TextView last_msg) {
        theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateWaitingForDiagnosis(String patientId) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Users").child(patientId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("waitingForDiagnosis", "false");
        patientRef.updateChildren(hashMap);
    }

    private boolean checkIfStillWaiting(String patientId) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Users").child(patientId);
        final boolean[] isWaiting = new boolean[1];
        Query query = patientRef.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.child("waitingForDiagnosis").getValue().equals("true")) {
                        isWaiting[0] = true;
                    } else {
                        isWaiting[0] = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return isWaiting[0];
    }
}
