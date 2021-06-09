package com.tuya.smart.bizubundle.ipc.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.api.MicroContext;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.utils.ProgressUtil;

import java.util.ArrayList;
import java.util.List;

public class IPCPanelActivity extends AppCompatActivity {

    private HomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        Log.i("XWG","进入ipc界面IPCPanelActivity");
        Toolbar toolbar = findViewById(R.id.toolbar_main);  //标题栏
        toolbar.setTitle("IPC Panel");
        RecyclerView homeRecycler = findViewById(R.id.home_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(this));
        homeRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new HomeAdapter();
        homeRecycler.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemBean bean, int position) {
                Log.i("xwg","bean title:"+bean.getTitle()+",getDevId:"+bean.getDevId());
//                AbsPanelCallerService ipc预览rn面板
                try {
                    AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                service.goPanelWithCheckAndTip(IPCPanelActivity.this, bean.getDevId());
//                    service.goPanelWithCheckAndTip(new Activity(), bean.getDevId());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("XWG","click error："+e.toString());
                }
            }
        });
        getCurrentHomeDetail();
    }

    /**
     * you must implementation AbsBizBundleFamilyService
     *
     * @return AbsBizBundleFamilyService
     */
    private AbsBizBundleFamilyService getService() {
        return MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
    }

    private void getCurrentHomeDetail() {
        ProgressUtil.showLoading(this, "Loading...");
        long currentHomeId = getService().getCurrentHomeId();
        if (currentHomeId <= 0) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(currentHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                List<ItemBean> beans = new ArrayList<>(8);
                for (GroupBean groupBean : homeBean.getGroupList()) {
                    beans.add(getItemBeanFromGroup(groupBean));
                }
                for (DeviceBean deviceBean : homeBean.getDeviceList()) {
                    beans.add(getItemBeanFromDevice(deviceBean));
                }
                mAdapter.setData(beans);
                ProgressUtil.hideLoading();
            }

            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                Toast.makeText(IPCPanelActivity.this, s + "\n" + s1, Toast.LENGTH_LONG).show();
            }
        });
    }

    private ItemBean getItemBeanFromGroup(GroupBean groupBean) {
        ItemBean itemBean = new ItemBean();
        itemBean.setGroupId(groupBean.getId());
        itemBean.setTitle(groupBean.getName());
        itemBean.setIconUrl(groupBean.getIconUrl());

        List<DeviceBean> deviceBeans = groupBean.getDeviceBeans();
        if (deviceBeans == null || deviceBeans.isEmpty()) {
            return null;
        } else {
            DeviceBean onlineDev = null;
            for (DeviceBean dev : deviceBeans) {
                if (dev != null) {
                    if (dev.getIsOnline()) {
                        onlineDev = dev;
                        break;
                    } else {
                        onlineDev = dev;
                    }
                }
            }
            itemBean.setDevId(onlineDev.getDevId());
            return itemBean;
        }
    }

    private ItemBean getItemBeanFromDevice(DeviceBean deviceBean) {
        ItemBean itemBean = new ItemBean();
        itemBean.setDevId(deviceBean.getDevId());
        itemBean.setIconUrl(deviceBean.getIconUrl());
        itemBean.setTitle(deviceBean.getName());
        return itemBean;
    }


}
