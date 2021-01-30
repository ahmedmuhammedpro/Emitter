package com.ahmed.emitter.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmed.emitter.R
import com.ahmed.emitter.databinding.FragmentEmitterBinding
import com.ahmed.emitter.databinding.UserItemBinding
import com.ahmed.emitter.network.User
import com.ahmed.emitter.viewmodels.EmitterViewModel

class EmitterFragment : Fragment() {

    private val viewModel: EmitterViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }

        ViewModelProvider(this, EmitterViewModel.Factory(activity.application))
            .get(EmitterViewModel::class.java)
    }

    private var userViewAdapter: UserViewAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUsers().observe(viewLifecycleOwner, {
            userViewAdapter?.users = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        val viewBinding: FragmentEmitterBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_emitter,
            container,
            false
        )

        viewBinding.viewModel = viewModel
        viewBinding.lifecycleOwner = viewLifecycleOwner

        userViewAdapter = UserViewAdapter(UserClick {
            showAlertDialog(it)
        })

        viewBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userViewAdapter
        }

        viewModel.eventErrorNetwork.observe(viewLifecycleOwner, {
            if (it) {
                onNetworkError()
            }
        })

        return viewBinding.root
    }

    private fun onNetworkError() {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkError()
        }
    }

    private fun showAlertDialog(user: User) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmation_dialog_title)
            .setMessage(getString(R.string.confirmation_dialog_message_1) + user.name + getString(R.string.confirmation_dialog_message_2))
            .setPositiveButton(R.string.ok_button) { dialog, which ->
                val intent = Intent().apply {
                    action = MIDDLE_MAN_APP_ACTION
                    flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                    putExtra(EXTRA_USER_KEY, viewModel.convertUserToJson(user))
                }

                context?.sendBroadcast(intent)
            }
            .setNegativeButton(R.string.cancel_button) { dialog, which -> }
            .create()

        alertDialog.show()
    }

    class UserClick(private val block: (User) -> Unit) {
        fun onClick(user: User) = block(user)
    }

    class UserViewAdapter(private val userClick: UserClick) : RecyclerView.Adapter<UserViewHolder>() {

        var users: List<User> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val viewBinding: UserItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                UserViewHolder.LAYOUT,
                parent,
                false
            )

            return UserViewHolder(viewBinding)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.viewBinding.also {
                it.user = users[position]
                it.userClick = userClick
            }
        }

        override fun getItemCount() = users.size

    }

    class UserViewHolder(val viewBinding: UserItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        companion object {
            @JvmStatic
            @LayoutRes
            val LAYOUT = R.layout.user_item
        }
    }

    companion object {
        const val MIDDLE_MAN_APP_ACTION = "com.ahmed.middleman.ACTION"
        const val EXTRA_USER_KEY = "com.ahmed.emitter.EXTRA_USER"
    }
}