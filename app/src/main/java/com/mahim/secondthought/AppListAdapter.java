package com.mahim.secondthought;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/** RecyclerView adapter for the home-screen app list. */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.VH> {

    public interface OnToggle {
        void onToggle(AppInfo app);
    }

    private final List<AppInfo> items = new ArrayList<>();
    private final OnToggle onToggle;

    public AppListAdapter(OnToggle onToggle) {
        this.onToggle = onToggle;
    }

    public void submit(List<AppInfo> apps) {
        items.clear();
        items.addAll(apps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppInfo app = items.get(position);
        h.name.setText(app.name);
        h.usage.setText(UsageHelper.formatDuration(app.todayUsageMs) + " today");
        h.icon.setImageDrawable(app.icon);

        h.toggle.setOnCheckedChangeListener(null);
        h.toggle.setChecked(app.monitored);
        h.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> onToggle.onToggle(app));

        // Tapping the whole row flips the switch too.
        h.itemView.setOnClickListener(v -> h.toggle.toggle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;
        final TextView usage;
        final SwitchMaterial toggle;

        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.app_icon);
            name = v.findViewById(R.id.app_name);
            usage = v.findViewById(R.id.app_usage);
            toggle = v.findViewById(R.id.app_toggle);
        }
    }
}
