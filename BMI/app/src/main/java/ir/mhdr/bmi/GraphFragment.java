package ir.mhdr.bmi;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ir.mhdr.bmi.blDao.HistoryBL;
import ir.mhdr.bmi.blDao.UserBL;
import ir.mhdr.bmi.dao.History;
import ir.mhdr.bmi.dao.User;
import ir.mhdr.bmi.lib.CustomMarkerView;
import ir.mhdr.bmi.lib.DateAxisValueFormatter;
import ir.mhdr.bmi.lib.FirebaseUtils;
import ir.mhdr.bmi.lib.ProfileChangedListener;
import ir.mhdr.bmi.lib.Statics;
import ir.mhdr.bmi.lib.TimeDiff;

public class GraphFragment extends Fragment implements ProfileChangedListener {

    LineChart lineChartWeight;
    AppCompatButton buttonGraphShowOneMonth;
    AppCompatButton buttonGraphShowOneWeek;
    AppCompatButton buttonGraphShowAll;
    AppCompatButton buttonGraphShowOneYear;
    AppCompatButton buttonGraphShowThreeMonth;

    DateTime start;
    DateTime end;

    int defaultColorForText;
    int activeColorForText;
    View.OnClickListener buttonGraphShowAll_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonGraphShowAll.setTextColor(activeColorForText);
            buttonGraphShowThreeMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneWeek.setTextColor(defaultColorForText);
            buttonGraphShowOneYear.setTextColor(defaultColorForText);

            drawChart(Period.All);
        }
    };
    View.OnClickListener buttonGraphShowThreeMonth_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonGraphShowAll.setTextColor(defaultColorForText);
            buttonGraphShowThreeMonth.setTextColor(activeColorForText);
            buttonGraphShowOneMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneWeek.setTextColor(defaultColorForText);
            buttonGraphShowOneYear.setTextColor(defaultColorForText);

            drawChart(Period.ThreeMonth);
        }
    };
    View.OnClickListener buttonGraphShowOneMonth_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonGraphShowAll.setTextColor(defaultColorForText);
            buttonGraphShowThreeMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneMonth.setTextColor(activeColorForText);
            buttonGraphShowOneWeek.setTextColor(defaultColorForText);
            buttonGraphShowOneYear.setTextColor(defaultColorForText);

            drawChart(Period.OneMonth);
        }
    };
    View.OnClickListener buttonGraphShowOneWeek_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonGraphShowAll.setTextColor(defaultColorForText);
            buttonGraphShowThreeMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneWeek.setTextColor(activeColorForText);
            buttonGraphShowOneYear.setTextColor(defaultColorForText);

            drawChart(Period.OneWeek);
        }
    };
    View.OnClickListener buttonGraphShowOneYear_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonGraphShowAll.setTextColor(defaultColorForText);
            buttonGraphShowThreeMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneMonth.setTextColor(defaultColorForText);
            buttonGraphShowOneWeek.setTextColor(defaultColorForText);
            buttonGraphShowOneYear.setTextColor(activeColorForText);

            drawChart(Period.OneYear);
        }
    };
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        if (FirebaseUtils.checkPlayServices(getContext())) {

            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
            mFirebaseAnalytics.setCurrentScreen(this.getActivity(), "GraphFragment", this.getClass().getSimpleName());
            mFirebaseAnalytics.setUserProperty(FirebaseUtils.UserProperty.InstallSource, Statics.InstallSource);
        }
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        lineChartWeight = (LineChart) view.findViewById(R.id.lineChartWeight);

        buttonGraphShowAll = (AppCompatButton) view.findViewById(R.id.buttonGraphShowAll);
        buttonGraphShowThreeMonth = (AppCompatButton) view.findViewById(R.id.buttonGraphShowThreeMonth);
        buttonGraphShowOneMonth = (AppCompatButton) view.findViewById(R.id.buttonGraphShowOneMonth);
        buttonGraphShowOneWeek = (AppCompatButton) view.findViewById(R.id.buttonGraphShowOneWeek);
        buttonGraphShowOneYear = (AppCompatButton) view.findViewById(R.id.buttonGraphShowOneYear);


        buttonGraphShowAll.setOnClickListener(buttonGraphShowAll_OnClickListener);
        buttonGraphShowThreeMonth.setOnClickListener((buttonGraphShowThreeMonth_OnClickListener));
        buttonGraphShowOneMonth.setOnClickListener(buttonGraphShowOneMonth_OnClickListener);
        buttonGraphShowOneWeek.setOnClickListener(buttonGraphShowOneWeek_OnClickListener);
        buttonGraphShowOneYear.setOnClickListener(buttonGraphShowOneYear_OnClickListener);

        defaultColorForText = getResources().getColor(android.R.color.black);
        activeColorForText = getResources().getColor(R.color.colorPrimary);

        return view;
    }

    private void drawChart(Period period) {

        this.calculateStartAndEndTime(period);

        lineChartWeight.clear();

        List<Entry> entries = new ArrayList<Entry>();

        UserBL userBL = new UserBL();
        HistoryBL historyBL = new HistoryBL();

        User user = userBL.getActiveUser();
        List<History> historyList = historyBL.getHistory(user);

        if (historyList.size() == 0) {
            return;
        }

        for (History h : historyList) {
            if (period == Period.All) {
                TimeDiff timeDiff = new TimeDiff(h.getDatetime2());
                float value = timeDiff.getMinutes();

                Entry entry = new Entry(value, Float.parseFloat(h.getValue()));
                entries.add(entry);
            } else {

                DateTime dateTime = h.getDatetime2();

                if (dateTime.isAfter(start) && dateTime.isBefore(end)) {
                    TimeDiff timeDiff = new TimeDiff(h.getDatetime2());
                    float value = timeDiff.getMinutes();

                    Entry entry = new Entry(value, Float.parseFloat(h.getValue()));
                    entries.add(entry);
                }
            }
        }

        if (entries.size() == 0) {
            return;
        }

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/BYekan.ttf");

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black));
        dataSet.setCircleColor(getResources().getColor(R.color.colorGreenDark));
        dataSet.setLineWidth(4);
        dataSet.setHighlightEnabled(true);
        dataSet.setValueTypeface(typeface);
        dataSet.setValueTextSize(10);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setValueFormatter(new DefaultValueFormatter(1)); // like 80.1

        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(true);

        Description description = new Description();
        description.setText("");
        lineChartWeight.setDescription(description);
        lineChartWeight.getLegend().setEnabled(false);
        lineChartWeight.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        lineChartWeight.animateX(300, Easing.EasingOption.EaseInExpo);
        lineChartWeight.setDoubleTapToZoomEnabled(false);
        lineChartWeight.setPinchZoom(false);
        lineChartWeight.setScaleYEnabled(false);
        lineChartWeight.setNoDataText(getResources().getString(R.string.no_chart_data));
        lineChartWeight.resetZoom();
        lineChartWeight.resetTracking();
        lineChartWeight.resetViewPortOffsets();
        lineChartWeight.fitScreen();

        IMarker customMarkerView = new CustomMarkerView(getContext(), R.layout.custom_marker_view_layout);
        lineChartWeight.setMarker(customMarkerView);

        XAxis xAxis = lineChartWeight.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter(lineChartWeight));
        xAxis.setGranularity(1f); // restrict interval to 1 (minimum)
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(3);
        xAxis.setTypeface(typeface);

        YAxis yAxisLeft = lineChartWeight.getAxisLeft();
        yAxisLeft.setTypeface(typeface);
        //yAxisLeft.setAxisMinimum(0);
        //yAxisLeft.setAxisMaximum(average *2);

        YAxis yAxisRight = lineChartWeight.getAxisRight();
        yAxisRight.setTypeface(typeface);
        //yAxisRight.setAxisMinimum(0);
        //yAxisRight.setAxisMaximum(average *2);

        lineChartWeight.setData(lineData);
        lineChartWeight.invalidate();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isResumed()) {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }

        buttonGraphShowAll.setTextColor(defaultColorForText);
        buttonGraphShowThreeMonth.setTextColor(defaultColorForText);
        buttonGraphShowOneMonth.setTextColor(activeColorForText);
        buttonGraphShowOneWeek.setTextColor(defaultColorForText);
        buttonGraphShowOneYear.setTextColor(defaultColorForText);

        drawChart(Period.OneMonth);
    }

    private void calculateStartAndEndTime(Period period) {

        DateTime dateTime = new DateTime();

        switch (period) {
            case OneMonth:

                this.end = dateTime;
                this.start = dateTime.minusMonths(1);

                break;
            case ThreeMonth:
                this.end = dateTime;
                this.start = dateTime.minusMonths(3);

                break;
            case OneWeek:

                this.end = dateTime;
                this.start = dateTime.minusWeeks(1);

                break;
            case OneYear:

                this.end = dateTime;
                this.start = dateTime.minusYears(1);

                break;
        }
    }

    @Override
    public void onProfileChanged() {
        onResume();
    }

    private enum Period {
        All,
        OneWeek,
        OneMonth,
        ThreeMonth,
        OneYear
    }
}
