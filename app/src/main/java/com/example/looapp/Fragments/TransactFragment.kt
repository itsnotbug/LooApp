package com.example.looapp.Fragments

import android.os.Bundle
import android.os.Trace
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.looapp.Model.Toilet
import com.example.looapp.R
import com.example.looapp.databinding.FragmentExploreBinding
import com.example.looapp.databinding.FragmentTransactBinding
import com.example.looapp.databinding.TransactDialogLayoutBinding

class TransactFragment : Fragment() {
    private lateinit var binding: FragmentTransactBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTransactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    private fun showDialog() {
        val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle("Transaction Gateway")
        alertDialogBuilder?.setMessage("Select from the following options to proceed:")

        val dialogCategoryLayout = layoutInflater.inflate(R.layout.transact_dialog_layout, null)
        val dialogBinding = TransactDialogLayoutBinding.bind(dialogCategoryLayout)

        alertDialogBuilder?.setView(dialogCategoryLayout)
        alertDialogBuilder?.setPositiveButton("Continue") { dialog, _ ->
            dialogBinding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radButtonDonate -> {
                        Toast.makeText(context, "Option 1", Toast.LENGTH_SHORT).show()
                    }

                    R.id.radButtonPay -> {
                        Toast.makeText(context, "Option 2", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }
    }
}

