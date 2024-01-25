package ru.netology.nmedia.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentDialogBinding
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nmedia.viewmodel.AuthViewModel.Companion.DIALOG_OUT

class DialogAuth : DialogFragment() {
    private var sel = 0

    companion object {
        private const val SEL_DIALOG = "SEL_DIALOG"
        val args = Bundle()
        fun newInstance(select: Int): DialogAuth {
            args.putInt(SEL_DIALOG, select)
            return DialogAuth()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentDialogBinding.inflate(layoutInflater)
        sel = args.getInt(SEL_DIALOG)
        with(binding) {
            when (sel) {
                DIALOG_IN -> {
                    textDialog.text = "Для установки лайков нужна авторизация, выполнить вход?"
                    btnYes.setOnClickListener {
                        findNavController().navigate(R.id.authFragment)
                        dismiss()
                    }
                }

                DIALOG_OUT -> {
                    textDialog.text = "Вы хотите удалить регистрацию?"
                    btnYes.setOnClickListener {
                        backValue?.returnDialogValue(DIALOG_OUT)
                        dismiss()
                    }

                }
            }

            btnNo.setOnClickListener {
                dismiss()
            }


        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        println("onStart()")
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private var backValue: ReturnSelection? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            backValue = context as ReturnSelection
        } catch (e: ClassCastException) {
            throw UnknownError
        }
    }

    override fun onDetach() {
        super.onDetach()
        backValue = null
    }


    interface ReturnSelection {
        fun returnDialogValue(select: Int)
    }
}