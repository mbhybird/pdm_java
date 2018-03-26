package com.besthings.pdm.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import com.besthings.bean.CostAccountsRet;
import com.besthings.pdm.R;
import com.besthings.pdm.adapter.CostAccountsAdapter;

import com.besthings.pdm.utils.Constant;
import com.besthings.pdm.utils.ListUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class CostAccountsActivity extends BaseActivity {
    private static final String TAG = CostAccountsActivity.class.getSimpleName();
    Button btnDayOne;
    Button btnDayThr;
    Button btnDaySev;
    Button btnDayDef;
    EditText etSearch;
    Button btnSearch;
    Button btnFirstPrice;
    Button btnColorPricr;
    Button btnProductPrice;
    Button btnAll;
    Spinner spFilter;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    RefreshLayout refreshLayout;
    String mJson;
    CostAccountsAdapter mAdapter;
    JSONObject jsonObject = new JSONObject();
    List<CostAccountsRet> mDataList = new ArrayList<>();
    Calendar mCalendar = Calendar.getInstance();
    Calendar mGCalendar = new GregorianCalendar();
    DateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd");
    String mStartDate = "";
    String mEndDate = "";
    String mYear = "";
    String mSeason = "";
    String mStyle = "";
    String mSoSearch = "";
    int mPriceType = -2;
    int mPageStart;
    int mPageEnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_accounts);

        setCustomActionBar(true);

        btnDayOne = (Button) findViewById(R.id.cost_accounts_btnDayOne);
        btnDayThr = (Button) findViewById(R.id.cost_accounts_btnDayThr);
        btnDaySev = (Button) findViewById(R.id.cost_accounts_btnDaySev);
        btnDayDef = (Button) findViewById(R.id.cost_accounts_btnDayDef);
        btnAll = (Button) findViewById(R.id.cost_accounts_btnAll);
        etSearch = (EditText) findViewById(R.id.cost_accounts_etSearch);
        btnSearch = (Button) findViewById(R.id.cost_accounts_btnSearch);
        spFilter = (Spinner) findViewById(R.id.cost_accounts_spFilter);
        mRecyclerView = (RecyclerView) findViewById(R.id.cost_accounts_recyclerView);
        btnFirstPrice = (Button) findViewById(R.id.cost_accounts_btnFirstPrice);
        btnColorPricr = (Button) findViewById(R.id.cost_accounts_btnColorPrice);
        btnProductPrice = (Button) findViewById(R.id.cost_accounts_btnProductPrice);
        refreshLayout = (RefreshLayout) findViewById(R.id.cost_accounts_refreshLayout);
        btnAll.setSelected(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshlayout) {
                ((View) refreshlayout).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSoSearch = etSearch.getText().toString();
                        mPageStart = 1;
                        mPageEnd = 10;
                        initJson();
                        try {
                            mDataList.clear();
                            mDataList = myApp.NetHelperEx.getCostAccountsBean(mJson).getRet();
                            mAdapter = new CostAccountsAdapter(R.layout.item_cost_accounts, mDataList);
                            mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
                            mRecyclerView.setAdapter(mAdapter);
                            if (mDataList.size() >= 10) {
                                mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                                    @Override
                                    public void onLoadMoreRequested() {
                                        mRecyclerView.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mPageStart = mPageEnd + 1;
                                                mPageEnd += 10;
                                                initJson();
                                                try {
                                                    mDataList = myApp.NetHelperEx.getCostAccountsBean(mJson).getRet();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                if (mDataList.size() > 0) {
                                                    mAdapter.addData(mDataList);
                                                    mAdapter.notifyDataSetChanged();
                                                    mAdapter.loadMoreComplete();

                                                } else {
                                                    mAdapter.loadMoreEnd();
                                                }
                                            }
                                        }, 500);

                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        refreshlayout.finishRefresh();
                    }
                }, 2000);
            }
        });

        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                return new ClassicsHeader(context);
            }
        });
        refreshLayout.setDisableContentWhenRefresh(true);
        refreshLayout.autoRefresh();

        btnDayOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDayClear();
                btnDayOne.setSelected(true);
                mCalendar.setTime(new Date());
                mEndDate = mSDF.format(mCalendar.getTime());
                mCalendar.add(Calendar.DAY_OF_MONTH, 0);
                mStartDate = mSDF.format(mCalendar.getTime());
                btnSearch.callOnClick();
            }
        });

        btnDayThr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDayClear();
                btnDayThr.setSelected(true);
                mCalendar.setTime(new Date());
                mEndDate = mSDF.format(mCalendar.getTime());
                mCalendar.add(Calendar.DAY_OF_MONTH, -2);
                mStartDate = mSDF.format(mCalendar.getTime());
                btnSearch.callOnClick();
            }
        });

        btnDaySev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDayClear();
                btnDaySev.setSelected(true);
                mCalendar.setTime(new Date());
                mEndDate = mSDF.format(mCalendar.getTime());
                mCalendar.add(Calendar.DAY_OF_MONTH, -6);
                mStartDate = mSDF.format(mCalendar.getTime());
                btnSearch.callOnClick();
            }
        });

        btnDayDef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();

            }
        });
        String[] costAccountsFilterType = getResources().getStringArray(R.array.costAccountsFilterType);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, costAccountsFilterType);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilter.setAdapter(spAdapter);
        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mYear = "";
                        mSeason = "";
                        mStyle  = "";
                        break;
                    case 1:
                        startActivityForFilterResult(Constant.REQUEST_CODE_YEAR);
                        break;
                    case 2:
                        startActivityForFilterResult(Constant.REQUEST_CODE_SEASON);
                        break;
                    case 3:
                        startActivityForFilterResult(Constant.REQUEST_CODE_STYLE);
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spFilter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedRowId");
                    field.setAccessible(true);
                    field.setInt(spFilter, Integer.MIN_VALUE);
                } catch(Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshLayout.autoRefresh();
            }
        });

        btnFirstPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPriceClear();
                btnFirstPrice.setSelected(true);
                mPriceType = 0;
                btnSearch.callOnClick();
            }
        });

        btnColorPricr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPriceClear();
                btnColorPricr.setSelected(true);
                mPriceType = 1;
                btnSearch.callOnClick();
            }
        });

        btnProductPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPriceClear();
                btnProductPrice.setSelected(true);
                mPriceType = 2;
                btnSearch.callOnClick();
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPriceClear();
                btnAll.setSelected(true);
                mPriceType = -2;
                btnSearch.callOnClick();
            }
        });

    }

    @Override
    protected void doSearchAction(int actionCode, List<String> parameter) {
        String result;
        mYear = "";
        mSeason = "";
        mStyle  = "";
        if (parameter != null && parameter.size() > 0) {
            result = ListUtil.listToString(parameter, ';');
            switch (actionCode) {
                case 8001:
                    mYear = result;
                    break;
                case 8002:
                    mSeason = result;
                    break;
                case 8006:
                    String style = ListUtil.symbolReplace(result, "&");
                    mStyle = style;
                    break;
                default:
                    break;
            }
        }
        btnSearch.callOnClick();
    }


    public void btnDayClear() {
        btnDayOne.setSelected(false);
        btnDayThr.setSelected(false);
        btnDaySev.setSelected(false);
        btnDayDef.setSelected(false);
    }

    public void btnPriceClear() {
        btnFirstPrice.setSelected(false);
        btnColorPricr.setSelected(false);
        btnProductPrice.setSelected(false);
        btnAll.setSelected(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return true;
    }


    private void initJson() {
        mJson = "";
        jsonObject = new JSONObject();
        try {
            jsonObject.put("ID", 18001);
            jsonObject.put("SeriesID", myApp.mSeriesID);
            jsonObject.put("Time", "time2000");
            jsonObject.put("Type", "");
            jsonObject.put("StartDate", mStartDate);
            jsonObject.put("EndDate", mEndDate);
            jsonObject.put("SrchFld", "");
            jsonObject.put("NumPerPg", 1000);
            if (!mYear.equals("")) {
                jsonObject.put("Year", mYear);
            }
            if (!mSeason.equals("")) {
                jsonObject.put("Season", mSeason);
            }
            if (!mStyle.equals("")) {
                jsonObject.put("Style", mStyle);
            }
            if (!mSoSearch.equals("")) {
                jsonObject.put("sosearch", mSoSearch);
            }
            jsonObject.put("PriceType", mPriceType);
            jsonObject.put("fPage", 1);
            jsonObject.put("PageCnt", "1000");
            jsonObject.put("PageStart", mPageStart);
            jsonObject.put("PageEnd", mPageEnd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mJson = jsonObject.toString();
    }


    private void showDatePickerDialog() {
        final View dpLayout = getLayoutInflater().inflate(R.layout.item_datepicker, null);
        final DatePicker dpStart = (DatePicker) dpLayout.findViewById(R.id.item_datepicker_dpStartDate);
        final DatePicker dpEnd = (DatePicker) dpLayout.findViewById(R.id.item_datepicker_dpEndDate);

        if (!TextUtils.isEmpty(mStartDate)) {
            String[] dtArray = mStartDate.split("-");
            dpStart.updateDate(Integer.valueOf(dtArray[0]), Integer.valueOf(dtArray[1]) - 1, Integer.valueOf(dtArray[2]));
        }

        if (!TextUtils.isEmpty(mEndDate)) {
            String[] dtArray = mEndDate.split("-");
            dpEnd.updateDate(Integer.valueOf(dtArray[0]), Integer.valueOf(dtArray[1]) - 1, Integer.valueOf(dtArray[2]));
        }

        new AlertDialog.Builder(this)
                .setTitle("自定义日期")
                .setView(dpLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnDayClear();
                        btnDayDef.setSelected(true);
                        mGCalendar.set(dpStart.getYear(), dpStart.getMonth(), dpStart.getDayOfMonth());
                        mStartDate = mSDF.format(mGCalendar.getTime());
                        mGCalendar.set(dpEnd.getYear(), dpEnd.getMonth(), dpEnd.getDayOfMonth());
                        mEndDate = mSDF.format(mGCalendar.getTime());
                        btnSearch.callOnClick();
                    }
                })
                .setNegativeButton("返回",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .show();
    }
}


