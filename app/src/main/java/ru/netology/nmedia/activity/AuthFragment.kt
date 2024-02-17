package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()
    private var pressBtn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater)
        with(binding) {
            fieldLogin.setText(getString(R.string.student))
            fieldPass.setText(getString(R.string.secret))
            btnSignIn.setOnClickListener {
                if (fieldLogin.text.isEmpty() && fieldPass.text.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Все поля должны быть заполнены!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                } else {
//AppAuth.getInstance().setAuth(5, "x-token")
                    pressBtn = true
                    val login = fieldLogin.text.toString()
                    val pass = fieldPass.text.toString()
                    viewModel.getAuthFromServer(login, pass)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.data.collect()
        }

        viewModel.authState.observe(viewLifecycleOwner) {
            if (pressBtn) {
                if (it.id != 0L && it.token != null) {
                    Snackbar.make(binding.root, "Выполнен вход в аккаунт", Snackbar.LENGTH_LONG)
                        .show()
                    findNavController().popBackStack()

                } else {
                    AuthViewModel.userAuth = false
                    Snackbar.make(
                        binding.root,
                        "Такого пользователя нет!",
                        Snackbar.LENGTH_LONG
                    )
                        .show()

                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.error) {
                Snackbar.make(
                    binding.root,
                    "Проверьте ваше подключение к сети!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
            binding.statusAuth.isVisible = it.loading
        }
        return binding.root
    }


}