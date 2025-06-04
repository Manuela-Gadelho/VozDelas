package com.example.vozdelas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<String> categoryNames;
    private List<String> categoryDescriptions; // Adicionado para descrições
    // --- ADICIONE ESTAS DUAS LINHAS ---
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String categoryName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // -----------------------------------

    public CategoryAdapter(Context context, List<String> categoryNames, List<String> categoryDescriptions) {
        this.context = context;
        this.categoryNames = categoryNames;
        this.categoryDescriptions = categoryDescriptions;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_educational_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName = categoryNames.get(position);
        String categoryDescription = categoryDescriptions.get(position); // Obtém a descrição

        holder.tvCategoryName.setText(categoryName);
        holder.tvCategoryDescription.setText(categoryDescription); // Define a descrição

        // --- ADICIONE ESTE BLOCO ---
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(categoryName);
                }
            }
        });
        // ---------------------------
    }

    @Override
    public int getItemCount() {
        return categoryNames.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        TextView tvCategoryDescription; // Adicionado para a descrição

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryDescription = itemView.findViewById(R.id.tv_category_description); // Encontra a TextView da descrição
        }
    }
}