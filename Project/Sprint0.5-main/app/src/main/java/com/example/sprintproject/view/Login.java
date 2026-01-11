package com.example.sprintproject.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.databinding.ActivityLoginBinding;
import com.example.sprintproject.viewmodel.LoginViewModel;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_login);
        LoginViewModel viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
    }
}
