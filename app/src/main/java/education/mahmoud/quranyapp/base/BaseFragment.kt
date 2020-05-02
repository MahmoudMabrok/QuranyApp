package education.mahmoud.quranyapp.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ethanhua.skeleton.ViewSkeletonScreen

abstract class BaseFragment : Fragment() {

    private val skeletonList by lazy { arrayListOf<ViewSkeletonScreen>() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setClickListeners()
        cleanUp()
    }

    open fun initViews(view: View) {}

    open fun setClickListeners() {}

    open fun showLoad() {
        skeletonList.forEach {
            it.show()
        }
    }

    open fun hideLoad() {
        skeletonList.forEach {
            it.hide()
        }
    }

    open fun cleanUp() {
        hideLoad()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanUp()
    }

}