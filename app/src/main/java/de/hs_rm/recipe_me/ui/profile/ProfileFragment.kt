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

    @SuppressLint("SetTextI18n")
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
            binding.profileQuantityRecipesText.text = getRecipeTotalText(it)
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

        binding.versionText.text = "${getString(R.string.version)}: $versionNumber"

        return binding.root
    }

    /**
     * @return Text for describing created recipes with message
     */
    @SuppressLint("SetTextI18n")
    private fun getRecipeTotalText(total: Int): String {
        val firstPart = getString(R.string.recipe_total_text_1)
        val mapStr = getString(R.string.recipe_total_text_map)
        return viewModel.getRecipeTotalText(firstPart, mapStr, total)
    }
}
