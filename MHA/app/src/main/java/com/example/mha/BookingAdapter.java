package com.example.mha;

import android.content.Context;
import android.content.Intent;
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
import com.example.mha.repository.AppointmentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ADAPTER FOR DISPLAYING BOOKINGS
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final Context context;
    private final List<AppointmentWithId> bookings;
    private final int userID;

    public BookingAdapter(Context context, List<AppointmentWithId> bookings, int userID) {
        this.context = context;
        this.bookings = bookings;
        this.userID = userID;
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

        holder.tvAppointmentInfo.setText(
                CryptClass.decrypt(booking.appointment.appointmentDate)
                        + " at " +
                        CryptClass.decrypt(booking.appointment.appointmentTime)
        );

        // ✅ HOSPITAL NAME
        if (booking.appointment.hospitalName != null) {
            holder.tvHospitalName.setText(
                    CryptClass.decrypt(booking.appointment.hospitalName)
                            + " - " +
                            CryptClass.decrypt(booking.appointment.hospitalCity)
            );
        } else {
            holder.tvHospitalName.setText("Loading hospital...");
            fetchHospitalInfo(booking, holder);
        }

        // SAFE CANCEL (SERVER ID ONLY)
        holder.btnCancel.setOnClickListener(v ->
                cancelBooking(booking.appointmentID, position)
        );

        // RESCHEDULE
        holder.btnReschedule.setOnClickListener(v -> {
            Intent intent = new Intent(context, RescheduleActivity.class);
            intent.putExtra("AppointmentID", booking.appointmentID);
            intent.putExtra("HospitalID", booking.appointment.hospitalID);
            intent.putExtra("UserID", userID);
            intent.putExtra("OldDate", booking.appointment.appointmentDate);
            intent.putExtra("OldTime", booking.appointment.appointmentTime);
            context.startActivity(intent);
        });
    }


    //  ONLINE HOSPITAL FETCH

    private void fetchHospitalInfo(AppointmentWithId booking, BookingViewHolder holder) {

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getHospitalById(booking.appointment.hospitalID)
                .enqueue(new Callback<HospitalRequest>() {

                    @Override
                    public void onResponse(Call<HospitalRequest> call,
                                           Response<HospitalRequest> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            HospitalRequest hospital = response.body();

                            holder.tvHospitalName.setText(
                                    CryptClass.decrypt(hospital.name)
                                            + " - " +
                                            CryptClass.decrypt(hospital.city)
                            );

                        } else {
                            holder.tvHospitalName.setText("Hospital not found");
                        }
                    }

                    @Override
                    public void onFailure(Call<HospitalRequest> call, Throwable t) {
                        holder.tvHospitalName.setText("Offline hospital");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }


    // SAFE CANCEL (ONLINE + OFFLINE)

    private void cancelBooking(int appointmentID, int position) {

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> body = new HashMap<>();
        body.put("appointmentID", appointmentID);

        api.cancelAppointment(body).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(context,
                            "Booking canceled",
                            Toast.LENGTH_SHORT).show();

                    bookings.remove(position);
                    notifyItemRemoved(position);

                    // ALSO CANCEL LOCALLY (SERVER ID)
                    new Thread(() -> {
                        AppointmentRepository repo =
                                new AppointmentRepository(context);
                        repo.cancelAppointmentOffline(appointmentID);
                    }).start();

                } else {
                    Toast.makeText(context,
                            "Failed to cancel booking",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                //  OFFLINE CANCEL
                new Thread(() -> {

                    AppointmentRepository repo =
                            new AppointmentRepository(context);

                    boolean success =
                            repo.cancelAppointmentOffline(appointmentID);

                    ((android.app.Activity) context).runOnUiThread(() -> {

                        if (success) {

                            Toast.makeText(context,
                                    "Offline cancel saved locally",
                                    Toast.LENGTH_SHORT).show();

                            bookings.remove(position);
                            notifyItemRemoved(position);

                        } else {

                            Toast.makeText(context,
                                    "Offline cancel failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }).start();
            }
        });
    }


    // VIEW HOLDER

    static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView tvHospitalName, tvAppointmentInfo;
        Button btnCancel, btnReschedule;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvAppointmentInfo = itemView.findViewById(R.id.tvAppointmentInfo);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
        }
    }


    // APPOINTMENT WRAPPER

    public static class AppointmentWithId {

        public int appointmentID;              //  SERVER ID
        public AppointmentRequest appointment;

        public AppointmentWithId(int appointmentID,
                                 AppointmentRequest appointment) {
            this.appointmentID = appointmentID;
            this.appointment = appointment;
        }
    }
}
