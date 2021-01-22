package de.hs_rm.recipe_me.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.databinding.SiteNoticeFragmentBinding

@AndroidEntryPoint
class SiteNoticeFragment : Fragment() {

    private lateinit var binding: SiteNoticeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.site_notice_fragment,
            container,
            false
        )

        return binding.root
    }

}
