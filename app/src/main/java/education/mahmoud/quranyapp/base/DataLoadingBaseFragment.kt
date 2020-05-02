package education.mahmoud.quranyapp.base

import android.os.Bundle
import android.view.View
import io.reactivex.disposables.CompositeDisposable

abstract class DataLoadingBaseFragment : BaseFragment() {

    val bg = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startLoadingData()
        startObserving()
    }

    open fun startLoadingData() {
        showLoad()
    }

    open fun startObserving() {}

    open fun defaultState() {}
    open fun noDataState() {}


    override fun onDestroyView() {
        super.onDestroyView()
        cleanUp()
        bg.clear()
    }

}