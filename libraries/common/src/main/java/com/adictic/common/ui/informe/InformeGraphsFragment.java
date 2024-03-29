package com.adictic.common.ui.informe;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.adictic.common.R;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.ui.DayUsageActivity;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformeGraphsFragment extends Fragment {

    private static final String LIST_ARG = "list_arg";
    private static final String ID_ARG = "ID_arg";

    private PieChart pieChart;
    private TextView TV_pieApp;
    private BarChart barChart;
    private Long idChild;
    private boolean pieCategory;

    private List<GeneralUsage> genericAppUsage;

    private int currentYear;

    private ChipGroup chipGroup;
    private Chip CH_appName;

    public InformeGraphsFragment(){ }

    public static InformeGraphsFragment newInstance(long id, Collection<GeneralUsage> col) {
        final InformeGraphsFragment informeGraphsFragment = new InformeGraphsFragment();

        final Bundle args = new Bundle(2);

        args.putParcelableArrayList(LIST_ARG, new ArrayList<>(col));
        args.putLong(ID_ARG, id);

        informeGraphsFragment.setArguments(args);
        return informeGraphsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.informe_layout, viewGroup, false);

        getBundle();

        pieChart = root.findViewById(R.id.Ch_Pie);
        barChart = root.findViewById(R.id.Ch_Line);
        TV_pieApp = root.findViewById(R.id.TV_PieChart);

        chipGroup = root.findViewById(R.id.CG_category);
        CH_appName = root.findViewById(R.id.CH_appName);
        chipGroup.check(CH_appName.getId());

        pieCategory = false;

        makeGraphs(true);

        return root;
    }

    private void getBundle() {
        final Bundle arguments = getArguments();

        if(arguments == null)
            return;

        genericAppUsage = arguments.getParcelableArrayList(LIST_ARG);

        if (genericAppUsage.isEmpty())
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
        else
            currentYear = genericAppUsage.get(0).year;

        idChild = arguments.getLong(ID_ARG);
    }

    private void makeGraphs(boolean barGraph) {
        Map<String, Long> mapUsage = new HashMap<>();

        chipGroup.setVisibility(View.VISIBLE);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            pieCategory = checkedId != CH_appName.getId();
            makeGraphs(false);
        });
        chipGroup.setSelectionRequired(true);

        List<BarEntry> barEntries = new ArrayList<>();

        long totalUsageTime = 0;

        if (pieCategory) {
            for (GeneralUsage gu : genericAppUsage) {
                currentYear = gu.year;
                if (gu.totalTime > 0) {
                    totalUsageTime += gu.totalTime;
                    for (AppUsage au : gu.usage) {
                        String category = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            if (au.app.category == -1) category = getString(R.string.other);
                            else
                                category = ApplicationInfo.getCategoryTitle(requireActivity().getApplicationContext(), au.app.category).toString();
                        }

                        if (mapUsage.containsKey(category))
                            mapUsage.put(category, mapUsage.get(category) + au.totalTime);
                        else mapUsage.put(category, au.totalTime);
                    }
                    barEntries.add(new BarEntry(gu.day + (gu.month * 100), gu.totalTime / (float) 3600000));
                }
            }
        } else {
            for (GeneralUsage gu : genericAppUsage) {
                if (gu.totalTime > 0) {
                    totalUsageTime += gu.totalTime;
                    for (AppUsage au : gu.usage) {
                        if (mapUsage.containsKey(au.app.appName))
                            mapUsage.put(au.app.appName, mapUsage.get(au.app.appName) + au.totalTime);
                        else mapUsage.put(au.app.appName, au.totalTime);
                    }
                    barEntries.add(new BarEntry(gu.day + (gu.month * 100), gu.totalTime / (float) 3600000));
                }
            }
        }

        if(barGraph)
            setBarChart(barEntries);

        setPieChart(mapUsage, totalUsageTime);
    }

    private void setPieChart(Map<String, Long> mapUsage, long totalUsageTime) {
        ArrayList<PieEntry> yValues = new ArrayList<>();
        long others = 0;
        for (Map.Entry<String, Long> entry : mapUsage.entrySet()) {
            // Si hi ha poques entrades no crear "Altres"
            if(mapUsage.size() < 5)
                yValues.add(new PieEntry(entry.getValue(), entry.getKey()));
            else{
                if (entry.getValue() >= totalUsageTime * 0.05 && !entry.getKey().equals(getString(R.string.other)))
                    yValues.add(new PieEntry(entry.getValue(), entry.getKey()));
                else {
                    others += entry.getValue();
                }
            }
        }

        if(!(mapUsage.size() < 5))
            yValues.add(new PieEntry(others, getString(R.string.other)));

        PieDataSet pieDataSet = new PieDataSet(yValues, "Ús d'apps");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(Constants.GRAPH_COLORS);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterTextSize(25);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.animateY(1000);

        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                final PieEntry pe = (PieEntry) e;

                TV_pieApp.setText(pe.getLabel());
                TV_pieApp.setTextSize(20);
                TV_pieApp.setTypeface(Typeface.DEFAULT_BOLD);

                Pair<Integer, Integer> appTime = Funcions.millisToString(e.getY());

                if (appTime.first == 0)
                    pieChart.setCenterText(getResources().getString(R.string.mins, appTime.second));
                else
                    pieChart.setCenterText(getResources().getString(R.string.hours_endl_minutes, appTime.first, appTime.second));
            }

            @Override
            public void onNothingSelected() {
                TV_pieApp.setText(getResources().getString(R.string.press_pie_chart));
                TV_pieApp.setTextSize(14);
                TV_pieApp.setTypeface(Typeface.DEFAULT);
                pieChart.setCenterText("");
            }
        });
        pieChart.invalidate();
    }

    private void setBarChart(List<BarEntry> entries) {
        BarDataSet barDataSet = new BarDataSet(entries, getResources().getString(R.string.daily_usage));
        barDataSet.setColors(Constants.GRAPH_COLORS);
        barDataSet.setDrawValues(false);

        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);

        barChart.animateY(1000);

        barChart.setFitBars(true);

        barChart.getAxisLeft().setGranularityEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        //xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
//        xAxis.setSpaceMin(barData.getBarWidth() / 2f);
//        xAxis.setSpaceMax(barData.getBarWidth() / 2f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        //xAxis.setLabelCount(xAxis.mEntryCount-1);
        xAxis.setValueFormatter(new MyXAxisBarFormatter());

        barChart.getAxisLeft().setValueFormatter(new MyYAxisBarFormatter());
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleYEnabled(false);

        barChart.dispatchSetSelected(false);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int day = (int) e.getX() % 100;
                int month = (int) e.getX() / 100;

                Intent i = new Intent(getActivity(), DayUsageActivity.class);
                i.putExtra("idChild", idChild);
                i.putExtra("day", day);
                i.putExtra("month", month);
                i.putExtra("year", currentYear);
                startActivity(i);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        barChart.invalidate();
    }

    static class MyXAxisBarFormatter extends ValueFormatter {
        //List<String> mesos = Arrays.asList(" Gen"," Feb"," Mar"," Abr"," Maig"," Jun"," Jul"," Ago"," Set"," Oct"," Nov"," Des");

        @Override
        public String getFormattedValue(float value) {
            if (value > 0) {
                //int mes = (int) value/100;
                return String.valueOf((int) value % 100);
            } else return "";

        }
    }

    static class MyYAxisBarFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return Math.round(value) + "h.";
        }
    }
}
