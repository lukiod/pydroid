package com.py.pydroid_mohak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.ViewHolder> {

    private List<Instructor> instructorList;

    public InstructorAdapter(Context context, List<Instructor> instructorList) {
        this.instructorList = instructorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Instructor instructor = instructorList.get(position);
        holder.nameTextView.setText(instructor.getName());
        holder.courseTextView.setText(instructor.getCourse());
    }

    @Override
    public int getItemCount() {
        return instructorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView courseTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            courseTextView = itemView.findViewById(R.id.courseTextView);
        }
    }
}
