package com.example.modulethreeassignmentkalebward;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DataDisplayFragment extends Fragment implements
        DataAdapter.OnDeleteClickListener,
        DataAdapter.OnEditClickListener,
        DataAdapter.OnQuantityChangeListener {

    private RecyclerView recyclerViewData;
    private FloatingActionButton btnAddData;
    private List<DataItem> dataItems;
    private DataAdapter dataAdapter;
    private String username = "Guest";
    private long userId = -1;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_data_display, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        if (getArguments() != null) {
            username = getArguments().getString("username", "Guest");
            userId = getArguments().getLong("userId", -1);
        }

        recyclerViewData = view.findViewById(R.id.recyclerViewData);
        btnAddData = view.findViewById(R.id.btnAddData);
        Button btnSettings = view.findViewById(R.id.btnSettings);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        TextView tvLoggedInUser = view.findViewById(R.id.tvLoggedInUser);

        tvLoggedInUser.setText("Logged in as: " + username);

        loadInventoryData();

        recyclerViewData.setLayoutManager(new LinearLayoutManager(getContext()));
        dataAdapter = new DataAdapter(dataItems, this, this, this);
        recyclerViewData.setAdapter(dataAdapter);

        btnAddData.setOnClickListener(v -> showAddDataDialog());
        btnSettings.setOnClickListener(v -> navigateToSettings());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    @Override
    public void onQuantityChanged(int position, String newQuantity) {
        if (position >= 0 && position < dataItems.size()) {
            DataItem item = dataItems.get(position);
            item.setQuantity(newQuantity);

            if (item.getId() != -1) {
                databaseHelper.updateInventoryItem(item.getId(), item.getName(), newQuantity, item.getDate());
            }
        }
    }

    private void loadInventoryData() {
        if (userId != -1) {
            dataItems = databaseHelper.getAllInventoryItems(userId);
            if (dataItems == null) {
                dataItems = new ArrayList<>();
            }
        } else {
            dataItems = new ArrayList<>();
        }
    }

    private void logout() {
        LoginFragment.clearLoginSession(requireContext());

        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.setReorderingAllowed(true);
        transaction.disallowAddToBackStack();
        transaction.commit();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        deleteItem(position);
    }

    @Override
    public void onEditClick(int position) {
        editItem(position);
    }

    private void showAddDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);

        final TextInputEditText etField1 = dialogView.findViewById(R.id.etField1);
        final TextInputEditText etField2 = dialogView.findViewById(R.id.etField2);
        final TextInputEditText etField3 = dialogView.findViewById(R.id.etField3);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        etField3.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    android.R.style.Theme_DeviceDefault_Light_Dialog,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format(Locale.getDefault(),
                                "%d/%d/%d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear % 100);
                        etField3.setText(date);
                    }, year, month, day);

            
            datePickerDialog.setOnShowListener(dialog -> {
                Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
                Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);

                if (positiveButton != null) {
                    positiveButton.setTextColor(getResources().getColor(android.R.color.black, null));
                }
                if (negativeButton != null) {
                    negativeButton.setTextColor(getResources().getColor(android.R.color.black, null));
                }
            });

            datePickerDialog.show();
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String field1 = etField1.getText() != null ? etField1.getText().toString() : "";
            String field2 = etField2.getText() != null ? etField2.getText().toString() : "";
            String field3 = etField3.getText() != null ? etField3.getText().toString() : "";

            if (field1.isEmpty() || field2.isEmpty() || field3.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId != -1) {
                long itemId = databaseHelper.addInventoryItem(field1, field2, field3, userId);

                if (itemId != -1) {
                    DataItem newItem = new DataItem(field1, field2, field3);
                    newItem.setId(itemId);

                    dataItems.add(0, newItem);
                    dataAdapter.notifyItemInserted(0);
                    recyclerViewData.scrollToPosition(0);

                    dialog.dismiss();
                    Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error adding item", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void editItem(int position) {
        DataItem item = dataItems.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        dialogTitle.setText("Edit Item");

        final TextInputEditText etField1 = dialogView.findViewById(R.id.etField1);
        final TextInputEditText etField2 = dialogView.findViewById(R.id.etField2);
        final TextInputEditText etField3 = dialogView.findViewById(R.id.etField3);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        etField1.setText(item.getName());
        etField2.setText(item.getQuantity());
        etField3.setText(item.getDate());

        btnSave.setText("Update");

        etField3.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    android.R.style.Theme_DeviceDefault_Light_Dialog,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format(Locale.getDefault(),
                                "%d/%d/%d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear % 100);
                        etField3.setText(date);
                    }, year, month, day);

            
            datePickerDialog.setOnShowListener(dialog -> {
                Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
                Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);

                if (positiveButton != null) {
                    positiveButton.setTextColor(getResources().getColor(android.R.color.black, null));
                }
                if (negativeButton != null) {
                    negativeButton.setTextColor(getResources().getColor(android.R.color.black, null));
                }
            });

            datePickerDialog.show();
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String field1 = etField1.getText() != null ? etField1.getText().toString() : "";
            String field2 = etField2.getText() != null ? etField2.getText().toString() : "";
            String field3 = etField3.getText() != null ? etField3.getText().toString() : "";

            if (field1.isEmpty() || field2.isEmpty() || field3.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (item.getId() != -1) {
                int result = databaseHelper.updateInventoryItem(item.getId(), field1, field2, field3);

                if (result > 0) {
                    item.setName(field1);
                    item.setQuantity(field2);
                    item.setDate(field3);

                    dataAdapter.notifyItemChanged(position);
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error updating item", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error: Item not in database", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void deleteItem(int position) {
        DataItem item = dataItems.get(position);

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (item.getId() != -1) {
                        int result = databaseHelper.deleteInventoryItem(item.getId());

                        if (result > 0) {
                            dataItems.remove(position);
                            dataAdapter.notifyItemRemoved(position);
                            dataAdapter.notifyItemRangeChanged(position, dataItems.size());
                            Toast.makeText(getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error deleting item", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        dataItems.remove(position);
                        dataAdapter.notifyItemRemoved(position);
                        dataAdapter.notifyItemRangeChanged(position, dataItems.size());
                        Toast.makeText(getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToSettings() {
        SmsNotificationFragment smsNotificationFragment = new SmsNotificationFragment();

        Bundle args = new Bundle();
        args.putLong("userId", userId);
        smsNotificationFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, smsNotificationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}