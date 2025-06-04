package com.example.vozdelas;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EducationalTopicAdapter extends RecyclerView.Adapter<EducationalTopicAdapter.EducationalTopicViewHolder> {

    private final Context context;
    private final List<EducationalContent> contentList;

    public EducationalTopicAdapter(Context context, List<EducationalContent> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    @NonNull
    @Override
    public EducationalTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_educational_content, parent, false);
        return new EducationalTopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EducationalTopicViewHolder holder, int position) {
        EducationalContent content = contentList.get(position);
        holder.tvContentTitle.setText(content.getTitle());
        holder.tvContentSubtitle.setText(content.getSubtitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContentDetailActivity.class);
            intent.putExtra("contentId", content.getId());
            intent.putExtra("contentTitle", content.getTitle());
            intent.putExtra("contentSubtitle", content.getSubtitle());
            intent.putExtra("contentBody", content.getContent());
            intent.putExtra("imageUrl", content.getImageUrl());
            intent.putExtra("videoUrl", content.getVideoUrl());
            intent.putExtra("externalLink", content.getExternalLink());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class EducationalTopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvContentTitle, tvContentSubtitle;

        public EducationalTopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContentTitle = itemView.findViewById(R.id.tv_content_title);
            tvContentSubtitle = itemView.findViewById(R.id.tv_content_subtitle);
        }
    }
}