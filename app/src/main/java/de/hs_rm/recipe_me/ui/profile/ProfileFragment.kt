package de.hs_rm.recipe_me.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.BuildConfig
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.ProfileFragmentBinding

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.profile_fragment,
            container,
            false
        )

        viewModel.loadRecipeTotal()

        viewModel.total.observe(viewLifecycleOwner, {
            setRecipeTotalText(it)
        })

        binding.changeProfilePicButton.setOnClickListener {
            Toast.makeText(
                context,
                "Hier kannst du bald dein Profilbild ändern",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.toSiteNoticeElement.setOnClickListener {
            val direction = ProfileFragmentDirections.toSiteNoticeFragment()
            findNavController().navigate(direction)
        }

        val versionNumber = BuildConfig.VERSION_NAME

        binding.versionText.text =
            "${requireContext().resources.getString(R.string.version)}: $versionNumber"

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setRecipeTotalText(total: Int) {
        val firstPart = requireContext().resources.getString(R.string.recipe_total_text_1)
        when {
            total == 1 -> {
                binding.profileQuantityRecipesText.text =
                    "$firstPart $total ${requireContext().resources.getString(R.string.recipe_total_text_3_one)}"
            }
            total <= 10 -> {
                binding.profileQuantityRecipesText.text =
                    "$firstPart $total ${requireContext().resources.getString(R.string.recipe_total_text_3_zero_to_ten)}"
            }
            else -> {
                binding.profileQuantityRecipesText.text =
                    "$firstPart $total ${requireContext().resources.getString(R.string.recipe_total_text_3_more_than_ten)}"
            }
        }
    }
}
