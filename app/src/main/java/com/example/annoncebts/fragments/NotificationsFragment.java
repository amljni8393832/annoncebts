package com.example.annoncebts.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.annoncebts.R;
import com.example.annoncebts.activities.AnnouncementDetailsActivity;

public class NotificationsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        View notificationCard = view.findViewById(R.id.card_notification_example);
        if (notificationCard != null) {
            notificationCard.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AnnouncementDetailsActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}
