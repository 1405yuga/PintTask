package com.example.pintask.mainfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.pintask.R
import com.example.pintask.constants.AppConstants
import com.example.pintask.databinding.FragmentAddTaskBinding
import com.example.pintask.appfunctions.FirestoreFunctions
import com.example.pintask.model.TaskModel
import com.example.pintask.model.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddTaskFragment : Fragment() {

    private lateinit var binding: FragmentAddTaskBinding
    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddTaskBinding.inflate(layoutInflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isPinned.observe(viewLifecycleOwner, Observer {
            binding.pinButton.setImageResource(
                if (it) R.drawable.pushpin_selected
                else R.drawable.pushpin_unselected
            )
        })

        binding.apply {

            pinButton.setOnClickListener {
                viewModel.setPinnedStatus(!viewModel.isPinned.value!!)
            }

            saveTask.setOnClickListener {

                FirestoreFunctions.addTask(
                TaskModel(
                    if(titleEditText.text.toString().trim().isNullOrEmpty()) AppConstants.DEFAULT_TASK_TITLE
                    else titleEditText.text.toString().trim(),
                    taskEditText.text.toString(),
                    viewModel.isPinned.value), requireContext()) {
                    //navigate
                    findNavController().apply {
                        popBackStack(R.id.displayTaskFragment, true)
                        navigate(R.id.displayTaskFragment)
                    }
                }
            }
        }
    }

}