package com.example.pintask.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pintask.R
import com.example.pintask.appfunctions.FirestoreFunctions
import com.example.pintask.appfunctions.NotificationFunctions
import com.example.pintask.databinding.CardTaskItemBinding
import com.example.pintask.model.TaskModel
import com.google.firebase.firestore.DocumentSnapshot

private const val TAG = "TaskListAdapter tag"

class TaskListAdapter(private val context: Context, private val refreshList: () -> (Unit)) :
    ListAdapter<DocumentSnapshot, TaskListAdapter.TaskViewHolder>(DiffCallBack) {
    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(
                oldItem: DocumentSnapshot,
                newItem: DocumentSnapshot
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DocumentSnapshot,
                newItem: DocumentSnapshot
            ): Boolean {
                return oldItem.toObject(TaskModel::class.java) == newItem.toObject(TaskModel::class.java)
            }

        }
    }


    class TaskViewHolder(private val binding: CardTaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun getPinImage(pinValue: Boolean): Int {
            if (pinValue) return R.drawable.pushpin_selected
            return R.drawable.pushpin_unselected
        }

        fun bind(documentSnapshot: DocumentSnapshot, context: Context, refreshList: () -> Unit) {
            //  bind data
            binding.apply {
                val currentTask = documentSnapshot.toObject(TaskModel::class.java)

                titleText.text = currentTask!!.taskTitle
                taskText.text = currentTask.task

                pin.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        getPinImage(currentTask.pinned!!)
                    )
                )

                pin.setOnClickListener {
                    val updatePinValue = !currentTask.pinned
                    FirestoreFunctions.updatePinStatus(
                        binding.root.context,
                        documentSnapshot.id,
                        updatePinValue,
                        refreshList = {
                            refreshList()
                        }, manageNotification = {
                            // remove from notification if un-pinned
                            if (updatePinValue) {
                                // Create or update the notification when pinned
                                NotificationFunctions.buildNotification(
                                    context,
                                    documentSnapshot.id,
                                    currentTask.taskTitle.toString(),
                                    currentTask.task.toString()
                                )
                            } else {
                                // Remove the notification if un-pinned
                                NotificationFunctions.removeFromNotification(
                                    context,
                                    documentSnapshot.id.toInt()
                                )
                            }
                        })
                }

                deleteTask.setOnClickListener {
                    FirestoreFunctions.deleteTask(
                        context,
                        documentSnapshot.id,
                        onSuccessFunction = {
                            refreshList()
                        })
                }
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            CardTaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position), context, refreshList)
    }

}