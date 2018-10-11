package com.wings2aspirations.genericleadcreation.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wings2aspirations.genericleadcreation.R;


public class MessageDialogFragment extends BottomSheetDialogFragment {
    private TextView globalMessage;
    private String messageToShow;

    public static MessageDialogFragment newInstance(String messageToShow) {

        Bundle args = new Bundle();

        MessageDialogFragment fragment = new MessageDialogFragment();

        fragment.messageToShow = messageToShow;

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_dialog_layout, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        globalMessage = view.findViewById(R.id.globalMessage);

        globalMessage.setText(messageToShow);
    }
}
