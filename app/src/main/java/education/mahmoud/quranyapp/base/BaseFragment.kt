package education.mahmoud.quranyapp.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import education.mahmoud.quranyapp.utils.KeyboardHelper
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment : Fragment() {
    interface InitListener {
        fun initViews(view: View)
        fun setClickListeners()
    }

    val TAG: String = javaClass.simpleName
    val bg = CompositeDisposable()

    private lateinit var skeletonScreen: SkeletonScreen

    private var initListener: InitListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initListener?.initViews(view)
        this.initListener?.setClickListeners()
        cleanUp()
    }

    open fun showLoad() {
        if (this::skeletonScreen.isInitialized) {
            this.skeletonScreen.show()
        }
    }

    open fun hideLoad() {
        if (this::skeletonScreen.isInitialized) {
            this.skeletonScreen.hide()
        }
    }

    open fun cleanUp() {
        hideLoad()
        bg.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanUp()
    }

    fun dismissKeyboard() {
        context?.let {
            KeyboardHelper.dissmiss(it)
        }
    }

    protected fun showSkeleton(view: View?, @LayoutRes layoutResource: Int) {
        view?.let {
            this.skeletonScreen = Skeleton.bind(it)
                    .load(layoutResource)
                    .show()
        }
    }

    protected fun showSkeleton(recyclerView: RecyclerView?, @LayoutRes layoutResource: Int,
                               adapter: RecyclerView.Adapter<*>?, count: Int = 10) {
        recyclerView?.let {
            it.postDelayed({
                this.skeletonScreen = Skeleton.bind(it)
                        .adapter(adapter)
                        .load(layoutResource)
                        .count(count)
                        .show()
            }, 500)

        }
    }

    protected fun setOnInitListeners(initListener: InitListener) {
        this.initListener = initListener
    }
}