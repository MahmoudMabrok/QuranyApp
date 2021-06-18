package education.mahmoud.quranyapp.feature.home_Activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import education.mahmoud.quranyapp.feature.bookmark_fragment.BookmarkFragment
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListFragment

class HomeVPAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SuraListFragment()
            else -> BookmarkFragment()
        }
    }
}
