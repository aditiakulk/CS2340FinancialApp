package com.example.sprintproject.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.ProfileViewModel;

import java.util.Locale;

public class Profile extends AppCompatActivity {
    private ImageButton profileImage;

    private final String[] profilePictures = {"alli_pfp", "bunny_pfp", "cat_pfp",
                                              "dino_pfp", "dog_pfp", "duck_pfp" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        TextView emailText = findViewById(R.id.profile_email);
        TextView expenseText = findViewById(R.id.expense_stats);
        TextView budgetText = findViewById(R.id.budget_stats);
        TextView circleText = findViewById(R.id.circle_stats);

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.getProfileSummary().observe(this, summary -> {
            emailText.setText(String.format("Email: %s", summary.getEmail()));
            expenseText.setText(String.format(Locale.US,
                    "Total expenses logged: %d", summary.getTotalExpenses()));
            budgetText.setText(String.format(Locale.US,
                    "Total budgets created: %d", summary.getTotalBudgets()));
            circleText.setText(String.format(Locale.US,
                    "Total Circles created: %d", summary.getTotalCircles()));
        });

        profileViewModel.getProfilePicture().observe(this, this::setImage);

        profileImage.setOnClickListener(v -> showProfilePictureDialog());
    }

    private void setImage(String pictureID) {
        int resId = getResources().getIdentifier(pictureID, "drawable", getPackageName());

        if (resId != 0) {
            profileImage.setImageResource(resId);
        } else {
            profileImage.setImageResource(R.drawable.profile_default);
        }
    }

    private void showProfilePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile_pictures, null);
        GridView gridView = dialogView.findViewById(R.id.picture_grid);

        // Adapter for grid
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.item_profile_picture, profilePictures) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                ImageView imageView;
                if (convertView == null) {
                    imageView = (ImageView) LayoutInflater.from(getContext())
                            .inflate(R.layout.item_profile_picture, parent, false);
                } else {
                    imageView = (ImageView) convertView;
                }
                int resId = getResources().getIdentifier(profilePictures[position],
                        "drawable", getPackageName());
                imageView.setImageResource(resId);
                return imageView;
            }
        };
        gridView.setAdapter(adapter);

        final AlertDialog dialog = builder.setView(dialogView).create();
        dialog.show();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPicture = profilePictures[position];

            ProfileViewModel profileViewModel =
                    new ViewModelProvider(this).get(ProfileViewModel.class);
            profileViewModel.setProfilePicture(selectedPicture);

            dialog.dismiss();
        });
    }
}
