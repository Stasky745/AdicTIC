package com.example.adictic.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Informe extends AppCompatActivity {

    TodoApi mTodoService;
    PieChart pieChart;
    TextView TV_pieApp;
    LineChart lineChart;
    BarChart barChart;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informe_layout);

        pieChart = (PieChart) findViewById(R.id.Ch_Pie);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        TV_pieApp = (TextView) findViewById(R.id.TV_PieChart);

        mTodoService = ((TodoApp) getApplication()).getAPI();

        Long idChild = getIntent().getLongExtra("idChild",-1);
        if(idChild != -1) getStats(idChild, 30);
    }

    private void getStats(Long idChild, Integer xDays){
        Call<Collection<GeneralUsage>> call = mTodoService.getAppUsage(idChild,xDays);

        Calendar cal = Calendar.getInstance();

        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(Call<Collection<GeneralUsage>> call, Response<Collection<GeneralUsage>> response) {
                if (response.isSuccessful()) {

                    makeGraphs(response.body());
                } else {
                    /** TextView que indiqui que no s'ha rebut res del servidor **/
                }
            }

            @Override
            public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {
                /** TextView que indiqui que no s'ha rebut res del servidor **/
            }
        });
    }

    private void makeGraphs(Collection<GeneralUsage> col){
        barChart = (BarChart) findViewById(R.id.Ch_Line);

        Map<String,Long> mapUsage = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        List<GeneralUsage> llista = getUsageMonths(cal.get(Calendar.MONTH)+1,cal.get(Calendar.YEAR),col);

        List<BarEntry> barEntries = new ArrayList<>();

        long totalUsageTime = 0;

        for(GeneralUsage gu : llista){
            totalUsageTime+=gu.totalTime;
            for(AppUsage au: gu.usage){
                if(mapUsage.containsKey(au.appName)) mapUsage.put(au.appName,mapUsage.get(au.appName)+au.totalTime);
                else mapUsage.put(au.appName,au.totalTime);
            }
            barEntries.add(new BarEntry(gu.day+(gu.month*100),gu.totalTime/3600000));
        }

        setBarChart(barEntries);
        setPieChart(mapUsage,totalUsageTime);
    }

    private void setPieChart(Map<String,Long> mapUsage, long totalUsageTime){
        ArrayList<PieEntry> yValues = new ArrayList<>();
        long others = 0;
        for(Map.Entry<String,Long> entry : mapUsage.entrySet()){
            if(entry.getValue() >= totalUsageTime*0.05) yValues.add(new PieEntry(entry.getValue(),entry.getKey()));
            else{
                others+=entry.getValue();
            }
        }

        yValues.add(new PieEntry(others,"Altres"));

        PieDataSet pieDataSet = new PieDataSet(yValues, "Ús d'apps");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());

        pieChart.setData(pieData);

        pieChart.animateY(1500);

        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                float minuts = e.getY()/(1000);
                int hores = 0;
                PieEntry pe = (PieEntry) e;

                TV_pieApp.setText(pe.getLabel());

                while(minuts>=60){
                    hores++;
                    minuts /= 60;
                }

                if(hores==0) pieChart.setCenterText((int)minuts + " min.");
                else pieChart.setCenterText(hores + " h. " + (int)minuts + " min.");

            }

            @Override
            public void onNothingSelected() {
                TV_pieApp.setText(getResources().getString(R.string.press_pie_chart));
                pieChart.setCenterText("");
            }
        });
        pieChart.invalidate();
    }

    private void setBarChart(List<BarEntry> entries){
        BarDataSet barDataSet = new BarDataSet(entries,getResources().getString(R.string.daily_usage));
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);

        barChart.animateY(1500);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new MyXAxisBarFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-65);
        xAxis.setDrawLabels(true);

        barChart.setExtraBottomOffset(30);

        barChart.getAxisLeft().setValueFormatter(new MyYAxisBarFormatter());
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        barChart.invalidate();
    }

    class MyXAxisBarFormatter extends ValueFormatter {
        List<String> mesos = Arrays.asList(" Gen"," Feb"," Mar"," Abr"," Maig"," Jun"," Jul"," Ago"," Set"," Oct"," Nov"," Des");

        @Override
        public String getFormattedValue(float value){
            int mes = (int) value/100;
            int dia = (int) value%100;
            return dia+mesos.get(mes);
        }
    }

    class MyYAxisBarFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value){
            return Math.round(value)+"h.";
        }
    }

    private List<GeneralUsage> getUsageMonths(Integer month, Integer year, Collection<GeneralUsage> col){
        List<GeneralUsage> resultat = new ArrayList<>();

        for(GeneralUsage gu : col){
            if(gu.month.equals(month) && gu.year.equals(year)) resultat.add(gu);
        }

        return resultat;
    }

}
