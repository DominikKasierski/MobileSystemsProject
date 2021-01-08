package pl.ks.dk.covidapp;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.ks.dk.covidapp.Adapter.ViewPagerAdapter;
import pl.ks.dk.covidapp.Fragments.ChatsFragment;
import pl.ks.dk.covidapp.Fragments.DecisionTreeFragment;
import pl.ks.dk.covidapp.Fragments.ProfileFragment;
import pl.ks.dk.covidapp.Fragments.UsersFragment;
import pl.ks.dk.covidapp.Fragments.WithoutDecisionTreeFragment;
import pl.ks.dk.covidapp.Model.Chat;
import pl.ks.dk.covidapp.Model.User;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    String role;
    String isWaiting;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    ViewPager viewPager;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

//TODO: JAK LEKARZ NAPISZ TO MOZNA ZNOWU WYSLAC PYTANIA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    role = user.getRole();
                    isWaiting = user.getWaitingForDiagnosis();
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                    if (viewPagerAdapter.getCount() != 0) {
                        if (isWaiting.equals("true")) {
                            setFragment(new WithoutDecisionTreeFragment(), "Diagnosis", 1);
                        } else {
                            setFragment(new DecisionTreeFragment(), "Diagnosis", 1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null && chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }
                }

                if (viewPagerAdapter.getCount() == 0) {
                    if (unread == 0) {
                        addFragment(new ChatsFragment(), "Chats");
                    } else {
                        addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                    }

                    if (role.equals("doctor")) {
                        addFragment(new UsersFragment(), "Patients");
                    } else {
                        if (isWaiting.equals("false")) {
                            addFragment(new DecisionTreeFragment(), "Diagnosis");
                        } else {
                            addFragment(new WithoutDecisionTreeFragment(), "Diagnosis");
                        }
                    }
                    addFragment(new ProfileFragment(), "Profile");
                } else {
                    if (unread == 0) {
                        setFragment(new ChatsFragment(), "Chats", 0);
                    } else {
                        setFragment(new ChatsFragment(), "(" + unread + ") Chats", 0);
                    }
                }


                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    public void setFragment(Fragment fragment, String title, int position) {
        viewPagerAdapter.setFragment(fragment, title, position);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public void addFragment(Fragment fragment, String title) {
        viewPagerAdapter.addFragment(fragment, title);
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

}