package com.example.mha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Adapter for displaying appointments
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final Context context;
    private final List<AppointmentWithId> bookings; // Use wrapper class to store appointmentID

    public BookingAdapter(Context context, List<AppointmentWithId> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        AppointmentWithId booking = bookings.get(position);

        // Show basic appointment info
        holder.tvAppointmentInfo.setText(booking.appointment.appointmentDate + " at " + booking.appointment.appointmentTime);
        holder.tvHospitalName.setText("Loading hospital...");

        // Fetch hospital info
        fetchHospitalInfo(booking, holder);

        // Cancel button
        holder.btnCancel.setOnClickListener(v -> cancelBooking(booking.appointmentID, position));
    }

    private void fetchHospitalInfo(AppointmentWithId booking, BookingViewHolder holder) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getHospitalById(booking.appointment.hospitalID).enqueue(new Callback<HospitalRequest>() {
            @Override
            public void onResponse(Call<HospitalRequest> call, Response<HospitalRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HospitalRequest hospital = response.body();
                    holder.tvHospitalName.setText(hospital.getName() + " - " + hospital.getCity());
                } else {
                    holder.tvHospitalName.setText("Hospital not found");
                }
            }

            @Override
            public void onFailure(Call<HospitalRequest> call, Throwable t) {
                holder.tvHospitalName.setText("Error loading hospital");
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private void cancelBooking(int appointmentID, int position) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("appointmentID", appointmentID);

        api.cancelAppointment(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(context, "Booking canceled", Toast.LENGTH_SHORT).show();
                    bookings.remove(position);
                    notifyItemRemoved(position);
                } else {
                    Toast.makeText(context, "Failed to cancel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvAppointmentInfo;
        Button btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvAppointmentInfo = itemView.findViewById(R.id.tvAppointmentInfo);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    // Wrapper class to store appointment along with server-generated appointmentID
    public static class AppointmentWithId {
        public int appointmentID;
        public AppointmentRequest appointment;

        public AppointmentWithId(int appointmentID, AppointmentRequest appointment) {
            this.appointmentID = appointmentID;
            this.appointment = appointment;
        }
    }

}
