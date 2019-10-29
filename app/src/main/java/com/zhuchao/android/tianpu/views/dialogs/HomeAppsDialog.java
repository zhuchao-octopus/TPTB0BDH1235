package com.zhuchao.android.tianpu.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.zhuchao.android.tianpu.R;
import com.zhuchao.android.tianpu.activities.MainActivity;
import com.zhuchao.android.tianpu.adapter.AppAdapter;
import com.zhuchao.android.tianpu.data.App;
import com.zhuchao.android.tianpu.databinding.ChooseactivityLayoutBinding;

/**
 * Created by Oracle on 2017/12/3.
 */

public class HomeAppsDialog extends Dialog {

    private static final String TAG = HomeAppsDialog.class.getSimpleName();
    private ChooseactivityLayoutBinding binding;
    private Context context;
    private SparseArray<App> appSparseArray = null;
    private AppAdapter appAdapter;
    private int vId;

    public HomeAppsDialog(@NonNull Context context) {
        this(context, 0, 0);
    }

    public HomeAppsDialog(@NonNull Context context, int themeResId, int vId) {
        super(context, themeResId);
        this.context = context;
        this.vId = vId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.chooseactivity_layout, null, false);
        setContentView(binding.getRoot());

        if (context instanceof MainActivity) {
            ((MainActivity) context).scan();
        }

        binding.chooseappListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, String.format("onItemClick %s", appSparseArray.get(position)));
//                Intent intent = new Intent(AppHandler.ADD_ACTION);
//                intent.putExtra(AppHandler.SEND_APP, appSparseArray.get(position));
//                intent.putExtra("vId", vId);
//                intent.setData(Uri.parse("package:www"));
//                context.sendBroadcast(intent);
                if (context instanceof MainActivity) {
                    App app = appSparseArray.get(position);
                    MainActivity mainActivity = ((MainActivity) context);
                    mainActivity.updateBottom(vId, app);
                    mainActivity.addRemove(vId, app);
                    mainActivity.appHandler.addAppToShort(app.getPackageName(), vId);
                }
                dismiss();
            }
        });
    }

    public void loadAppData(SparseArray<App> appSparseArray) {

        this.appSparseArray = appSparseArray.clone();
        appAdapter = new AppAdapter(context);
        appAdapter.setListMode(1);
        appAdapter.setApps(appSparseArray);
        binding.chooseappListview.setAdapter(appAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.appSparseArray != null) {
            this.appSparseArray.clear();
            this.appSparseArray = null;
        }
        appAdapter.release();
        appAdapter = null;
        context = null;
    }

    public static HomeAppsDialog showHomeAppDialog(Context context, int vId) {
        HomeAppsDialog homeAppsDialog = new HomeAppsDialog(context, R.style.MenuDialog, vId);
        homeAppsDialog.show();
        return homeAppsDialog;
    }
}
