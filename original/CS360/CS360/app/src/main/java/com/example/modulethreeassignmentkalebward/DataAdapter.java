package com.example.modulethreeassignmentkalebward;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

/**
 * Adapter for displaying inventory data items in a RecyclerView
 */
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private final List<DataItem> dataItems;
    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;
    private final OnQuantityChangeListener quantityChangeListener;
    private final SimpleDateFormat dateFormat;
    private final Date currentDate;

    // Interface for delete item callback
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    // Interface for edit item callback
    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    // Interface for quantity adjustment callback
    public interface OnQuantityChangeListener {
        void onQuantityChanged(int position, String newQuantity);
    }

    // Constructor
    public DataAdapter(List<DataItem> dataItems,
                       OnDeleteClickListener deleteListener,
                       OnEditClickListener editListener,
                       OnQuantityChangeListener quantityChangeListener) {
        this.dataItems = dataItems;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.quantityChangeListener = quantityChangeListener;

        // Initialize date formatter for m/d/y format
        this.dateFormat = new SimpleDateFormat("M/d/yy", Locale.US);

        // Get current date (set time to beginning of day for comparison)
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

        // Set data to views
        holder.tvColumn1.setText(item.getName());
        holder.tvColumn2.setText(item.getQuantity());
        holder.tvColumn3.setText(item.getDate());

        // Check if the item date is in the future
        boolean isFutureDate = isDateInFuture(item.getDate());

        // Update UI based on date status
        if (isFutureDate) {
            // Show future date indicator
            holder.tvFutureDate.setVisibility(View.VISIBLE);

            // Grey out the text
            int greyColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_text);
            holder.tvColumn1.setTextColor(greyColor);
            holder.tvColumn2.setTextColor(greyColor);
            holder.tvColumn3.setTextColor(greyColor);

            // Make entire card background grey
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_background)
            );

            // Change content layout background
            holder.itemContentLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_background)
            );

            // Add content description for accessibility
            holder.tvFutureDate.setContentDescription(
                    holder.itemView.getContext().getString(R.string.future_date_accessibility)
            );

            // Make edit/delete buttons more visible on grey background
            holder.btnEdit.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_accent)
            ));
            holder.btnDelete.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.future_date_accent)
            ));

            // Hide quantity adjustment buttons for future items
            holder.quantityControlsLayout.setVisibility(View.GONE);
        } else {
            // Hide future date indicator for current/past dates
            holder.tvFutureDate.setVisibility(View.GONE);

            // Normal text color
            int normalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_text);
            holder.tvColumn1.setTextColor(normalColor);
            holder.tvColumn2.setTextColor(normalColor);
            holder.tvColumn3.setTextColor(normalColor);

            // Normal card background
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );

            // Normal content layout background
            holder.itemContentLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );

            // Reset button tints
            holder.btnEdit.setImageTintList(null);
            holder.btnDelete.setImageTintList(null);

            // Show quantity adjustment buttons for current/past items
            holder.quantityControlsLayout.setVisibility(View.VISIBLE);
        }

        // Set click listeners for edit/delete
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

        // Set click listeners for quantity adjustment
        holder.btnIncreaseQuantity.setOnClickListener(v -> {
            if (quantityChangeListener != null) {
                try {
                    // Parse current quantity
                    int currentQty = Integer.parseInt(item.getQuantity());
                    // Increase by 1
                    int newQty = currentQty + 1;
                    // Update item
                    item.setQuantity(String.valueOf(newQty));
                    // Update display
                    holder.tvColumn2.setText(String.valueOf(newQty));
                    // Notify callback
                    quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), String.valueOf(newQty));
                    // Show updated indicator briefly
                    showQuantityUpdatedIndicator(holder);
                } catch (NumberFormatException e) {
                    // If quantity is not a number, just use "1" as default
                    item.setQuantity("1");
                    holder.tvColumn2.setText("1");
                    quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), "1");
                    showQuantityUpdatedIndicator(holder);
                }
            }
        });

        holder.btnDecreaseQuantity.setOnClickListener(v -> {
            if (quantityChangeListener != null) {
                try {
                    // Parse current quantity
                    int currentQty = Integer.parseInt(item.getQuantity());
                    // Decrease by 1, but not below 0
                    int newQty = Math.max(0, currentQty - 1);
                    // Update item
                    item.setQuantity(String.valueOf(newQty));
                    // Update display
                    holder.tvColumn2.setText(String.valueOf(newQty));
                    // Notify callback
                    quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), String.valueOf(newQty));
                    // Show updated indicator briefly
                    showQuantityUpdatedIndicator(holder);
                } catch (NumberFormatException e) {
                    // If quantity is not a number, just use "0" as default
                    item.setQuantity("0");
                    holder.tvColumn2.setText("0");
                    quantityChangeListener.onQuantityChanged(holder.getAdapterPosition(), "0");
                    showQuantityUpdatedIndicator(holder);
                }
            }
        });

        // Add accessibility improvements

        // Content description for delete button
        holder.btnDelete.setContentDescription(
                holder.itemView.getContext().getString(R.string.delete_item_accessibility, item.getName())
        );

        // Content description for edit button
        holder.btnEdit.setContentDescription(
                holder.itemView.getContext().getString(R.string.edit_item_accessibility, item.getName())
        );

        // Content descriptions for quantity buttons
        holder.btnIncreaseQuantity.setContentDescription(
                holder.itemView.getContext().getString(R.string.increase_quantity_accessibility, item.getName())
        );

        holder.btnDecreaseQuantity.setContentDescription(
                holder.itemView.getContext().getString(R.string.decrease_quantity_accessibility, item.getName())
        );

        // Set content description for the entire row
        String fullItemDescription = holder.itemView.getContext().getString(
                R.string.item_row_accessibility,
                item.getName(),
                item.getQuantity(),
                item.getDate()
        );

        // Add future date information to description if applicable
        if (isFutureDate) {
            fullItemDescription += " " +
                    holder.itemView.getContext().getString(R.string.future_date_accessibility);
        }

        holder.itemView.setContentDescription(fullItemDescription);

        // Make individual columns focusable for screen readers
        holder.tvColumn1.setContentDescription(
                holder.itemView.getContext().getString(R.string.item_name_accessibility, item.getName())
        );
        holder.tvColumn2.setContentDescription(
                holder.itemView.getContext().getString(R.string.item_quantity_accessibility, item.getQuantity())
        );
        holder.tvColumn3.setContentDescription(
                holder.itemView.getContext().getString(R.string.item_date_accessibility, item.getDate())
        );
    }

    /**
     * Show a brief "Quantity Updated" indicator
     * @param holder ViewHolder containing the indicator
     */
    private void showQuantityUpdatedIndicator(ViewHolder holder) {
        holder.tvQuantityUpdated.setVisibility(View.VISIBLE);

        // Hide after 1.5 seconds
        new Handler().postDelayed(() -> {
            if (holder.tvQuantityUpdated != null) {
                holder.tvQuantityUpdated.setVisibility(View.GONE);
            }
        }, 1500);
    }

    /**
     * Check if a date string is in the future
     * @param dateString Date in m/d/y format
     * @return true if date is in the future, false otherwise
     */
    private boolean isDateInFuture(String dateString) {
        try {
            Date itemDate = dateFormat.parse(dateString);
            return itemDate != null && itemDate.after(currentDate);
        } catch (ParseException e) {
            // If we can't parse the date, assume it's not in the future
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvColumn1, tvColumn2, tvColumn3, tvFutureDate, tvQuantityUpdated;
        ImageButton btnDelete, btnEdit;
        Button btnIncreaseQuantity, btnDecreaseQuantity;
        LinearLayout itemContentLayout, quantityControlsLayout;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            tvColumn1 = itemView.findViewById(R.id.tvColumn1);
            tvColumn2 = itemView.findViewById(R.id.tvColumn2);
            tvColumn3 = itemView.findViewById(R.id.tvColumn3);
            tvFutureDate = itemView.findViewById(R.id.tvFutureDate);
            tvQuantityUpdated = itemView.findViewById(R.id.tvQuantityUpdated);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnIncreaseQuantity = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btnDecreaseQuantity);
            itemContentLayout = itemView.findViewById(R.id.itemContentLayout);
            quantityControlsLayout = itemView.findViewById(R.id.quantityControlsLayout);
            cardView = (CardView) itemView;
        }
    }
}