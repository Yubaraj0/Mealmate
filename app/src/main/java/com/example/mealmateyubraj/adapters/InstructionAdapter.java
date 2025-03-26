package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.AddEditMealActivity;

import java.util.List;

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.ViewHolder> {
    private final Context context;
    private final List<String> instructions;
    private final OnInstructionClickListener listener;

    public interface OnInstructionClickListener {
        void onDeleteInstruction(int position);
    }

    public InstructionAdapter(Context context, List<String> instructions, OnInstructionClickListener listener) {
        this.context = context;
        this.instructions = instructions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String instruction = instructions.get(position);
        holder.tvInstruction.setText(String.format("%d. %s", position + 1, instruction));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteInstruction(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstruction;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tv_instruction);
            btnDelete = itemView.findViewById(R.id.btn_delete_instruction);
        }
    }
} 