package com.example.sprintproject.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.databinding.ActivityCreateAccountBinding;
import com.example.sprintproject.viewmodel.CreateAccountViewModel;

public class CreateAccount extends AppCompatActivity {

    private CreateAccountViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityCreateAccountBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_create_account);

        vm = new ViewModelProvider(this).get(CreateAccountViewModel.class);
        binding.setVm(vm);
        binding.setLifecycleOwner(this);

        vm.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        vm.getNavigateBack().observe(this, goBack -> {
            if (Boolean.TRUE.equals(goBack)) {
                finish();
            }
        });
    }
}
