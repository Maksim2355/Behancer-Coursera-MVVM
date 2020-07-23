package com.elegion.test.behancer.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elegion.test.behancer.BuildConfig;
import com.elegion.test.behancer.adapters.ProjectsAdapter;
import com.elegion.test.behancer.common.BaseRefreshViewModel;
import com.elegion.test.behancer.data.Storage;
import com.elegion.test.behancer.data.model.project.Project;
import com.elegion.test.behancer.utils.ApiUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ProjectsListViewModel extends BaseRefreshViewModel {


    private MutableLiveData<List<Project>> mProjects = new MutableLiveData<>();

    private ProjectsAdapter.OnItemClickListener mOnItemClickListener;


    public ProjectsListViewModel(Storage storage, ProjectsAdapter.OnItemClickListener onItemClickListener){
        mStorage = storage;
        mOnItemClickListener = onItemClickListener;
        update();
    }


    @Override
    public void update() {
        mDisposable = ApiUtils.getApiService().getProjects(BuildConfig.API_QUERY)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(mStorage::insertProjects)
                .onErrorReturn(throwable ->
                        ApiUtils.NETWORK_EXCEPTIONS.contains(throwable.getClass()) ? mStorage.getProjects() : null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mIsLoading.postValue(true))
                .doFinally(() -> mIsLoading.postValue(false))
                .subscribe(
                        response -> {
                            mIsListVisible.postValue(true);
                            mProjects.postValue(response.getProjects());
                        },
                        throwable -> mIsListVisible.postValue(false));
    }

    public MutableLiveData<List<Project>> getProjects() {
        return mProjects;
    }

    public ProjectsAdapter.OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

}
