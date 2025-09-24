package com.example.modulethreeassignmentkalebward;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private final List<DataItem> dataItems;
    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;
    private final OnQuantityChangeListener quantityChangeListener;
    private final SimpleDateFormat dateFormat;
    private final Date currentDate;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(int position, String newQuantity);
    }

    public DataAdapter(List<DataItem> dataItems,
                       OnDeleteClickListener deleteListener,
                       OnEditClickListener editListener,
                       OnQuantityChangeListener quantityChangeListener) {
        this.dataItems = dataItems;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.quantityChangeListener = quantityChangeListener;

        this.dateFormat = new SimpleDateFormat("M/d/yy", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.currentDate = cal.getTime();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_data_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem item = dataItems.get(position);

        holder.tvColumn1.setText(item.getName());
        holder.tvColumn2.setText(item.getQuantity());
        holder.tvColumn3.setText(item.getDate());

        boolean isFutureDate = isDateInFuture(item.getDate());

        if (isFutureDate) {
            if (holder.tvFutureDate != null) {
                holder.tvFutureDate.setVisibility(View.VISIBLE);
            }

            int greyColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_text);
            holder.tvColumn1.setTextColor(greyColor);
            holder.tvColumn2.setTextColor(greyColor);
            holder.tvColumn3.setTextColor(greyColor);

            if (holder.cardView != null) {
                holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_background)
                );
                holder.cardView.setCardElevation(4f);
                holder.cardView.setRadius(8f);
            }

            if (holder.itemContentLayout != null) {
                holder.itemContentLayout.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_background)
                );
            }

            if (holder.quantityControlsLayout != null) {
                holder.quantityControlsLayout.setVisibility(View.GONE);
            }
        } else {
            if (holder.tvFutureDate != null) {
                holder.tvFutureDate.setVisibility(View.GONE);
            }

            int normalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_text);
            holder.tvColumn1.setTextColor(normalColor);
            holder.tvColumn2.setTextColor(normalColor);
            holder.tvColumn3.setTextColor(normalColor);

            if (holder.cardView != null) {
                int cardColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background_light);
                holder.cardView.setCardBackgroundColor(cardColor);
                holder.cardView.setCardElevation(2f);
                holder.cardView.setRadius(8f);
            }

            if (holder.itemContentLayout != null) {
                holder.itemContentLayout.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background_light)
                );
            }

            if (holder.quantityControlsLayout != null) {
                holder.quantityControlsLayout.setVisibility(View.VISIBLE);
            }
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(holder.getAdapterPosition());
            }
        });

        if (holder.btnIncreaseQuantity != null) {
            holder.btnIncreaseQuantity.setOnClickListener(v -> {
                if (quantityChangeListener != null) {
                    try {
                        int currentQty = Integer.parseInt(item.getQuantity());
                        int newQty = currentQty + 1;
                        item.setQuantity(String.valueOf(newQty));
                        holder.tvColumn2.setText(String.valueOf(newQty));
                        quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), String.valueOf(newQty));
                        showQuantityUpdatedIndicator(holder);
                    } catch (NumberFormatException e) {
                        item.setQuantity("1");
                        holder.tvColumn2.setText("1");
                        quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), "1");
                        showQuantityUpdatedIndicator(holder);
                    }
                }
            });
        }

        if (holder.btnDecreaseQuantity != null) {
            holder.btnDecreaseQuantity.setOnClickListener(v -> {
                if (quantityChangeListener != null) {
                    try {
                        int currentQty = Integer.parseInt(item.getQuantity());
                        int newQty = Math.max(0, currentQty - 1);
                        item.setQuantity(String.valueOf(newQty));
                        holder.tvColumn2.setText(String.valueOf(newQty));
                        quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), String.valueOf(newQty));
                        showQuantityUpdatedIndicator(holder);
                    } catch (NumberFormatException e) {
                        item.setQuantity("0");
                        holder.tvColumn2.setText("0");
                        quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), "0");
                        showQuantityUpdatedIndicator(holder);
                    }
                }
            });
        }
    }

    private void showQuantityUpdatedIndicator(ViewHolder holder) {
        if (holder.tvQuantityUpdated != null) {
            holder.tvQuantityUpdated.setVisibility(View.VISIBLE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (holder.tvQuantityUpdated != null) {
                    holder.tvQuantityUpdated.setVisibility(View.GONE);
                }
            }, 1500);
        }
    }

    private boolean isDateInFuture(String dateString) {
        try {
            Date itemDate = dateFormat.parse(dateString);
            return itemDate != null && itemDate.after(currentDate);
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvColumn1, tvColumn2, tvColumn3, tvFutureDate, tvQuantityUpdated;
        ImageButton btnDelete, btnEdit;
        TextView btnIncreaseQuantity, btnDecreaseQuantity;
        LinearLayout itemContentLayout, quantityControlsLayout;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            tvColumn1 = itemView.findViewById(R.id.tvColumn1);
            tvColumn2 = itemView.findViewById(R.id.tvColumn2);
            tvColumn3 = itemView.findViewById(R.id.tvColumn3);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);

            tvFutureDate = itemView.findViewById(R.id.tvFutureDate);
            tvQuantityUpdated = itemView.findViewById(R.id.tvQuantityUpdated);
            btnIncreaseQuantity = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btnDecreaseQuantity);
            itemContentLayout = itemView.findViewById(R.id.itemContentLayout);
            quantityControlsLayout = itemView.findViewById(R.id.quantityControlsLayout);

            try {
                cardView = (CardView) itemView;
            } catch (Exception e) {
                cardView = null;
            }
        }
    }
}