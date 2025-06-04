package com.example.vozdelas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnonymousReportAdapter extends RecyclerView.Adapter<AnonymousReportAdapter.ReportViewHolder> {

    private Context context;
    private List<AnonymousReport> reportList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AnonymousReportAdapter(Context context, List<AnonymousReport> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anonymous_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        AnonymousReport report = reportList.get(position);
        holder.tvReportContent.setText(report.getText());
        if (report.getTimestamp() != null) {
            holder.tvReportTimestamp.setText(dateFormat.format(report.getTimestamp().toDate()));
        } else {
            holder.tvReportTimestamp.setText("Data desconhecida");
        }
        // Não exibe o senderUid, mantendo o anonimato para o público
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportContent;
        TextView tvReportTimestamp;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportContent = itemView.findViewById(R.id.tv_report_content);
            tvReportTimestamp = itemView.findViewById(R.id.tv_report_timestamp);
        }
    }
}