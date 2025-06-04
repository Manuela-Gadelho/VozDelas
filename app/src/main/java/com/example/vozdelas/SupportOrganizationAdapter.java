package com.example.vozdelas;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SupportOrganizationAdapter extends RecyclerView.Adapter<SupportOrganizationAdapter.ViewHolder> {

    private List<SupportOrganization> organizations;

    public SupportOrganizationAdapter(List<SupportOrganization> organizations) {
        this.organizations = organizations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_organization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportOrganization organization = organizations.get(position);
        holder.tvName.setText(organization.getName());
        holder.tvDescription.setText(organization.getDescription());
        holder.tvAddress.setText(organization.getAddress());
        holder.tvPhone.setText(organization.getPhone());
        holder.tvWebsite.setText(organization.getWebsite());

        // Torna o telefone clicável para ligar
        holder.tvPhone.setOnClickListener(v -> {
            String phoneNumber = organization.getPhone();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                v.getContext().startActivity(dialIntent);
            }
        });

        // Torna o site clicável para abrir no navegador
        holder.tvWebsite.setOnClickListener(v -> {
            String websiteUrl = organization.getWebsite();
            if (websiteUrl != null && !websiteUrl.isEmpty()) {
                if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                    websiteUrl = "http://" + websiteUrl; // Garante que o URL tenha um esquema
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                v.getContext().startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvAddress, tvPhone, tvWebsite;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_organization_name);
            tvDescription = itemView.findViewById(R.id.tv_organization_description);
            tvAddress = itemView.findViewById(R.id.tv_organization_address);
            tvPhone = itemView.findViewById(R.id.tv_organization_phone);
            tvWebsite = itemView.findViewById(R.id.tv_organization_website);
        }
    }
}