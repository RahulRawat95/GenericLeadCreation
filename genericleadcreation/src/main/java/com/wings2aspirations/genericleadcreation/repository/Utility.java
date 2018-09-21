package com.wings2aspirations.genericleadcreation.repository;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.wings2aspirations.genericleadcreation.BuildConfig;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.fragment.MessageDialogFragment;
import com.wings2aspirations.genericleadcreation.network.ApiClient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class Utility {

    public static final String dateFormatToShow = "dd-MM-yyyy";
    public static AlertDialog datePickerDialog;
    private static AlertDialog timePickerDialog;

    public static void showDatePickerDialog(final Context mContext, final EditText showDateContainer, final Date minDate, final Date maxDate, Date dateToSet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        //creating layout inflator for inflating the custom datepicer layout
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        //inflating the layout
        View datePickerLayout = layoutInflater.inflate(R.layout.date_picker_dialog, null);
        //finding the date picker view
        DatePicker scroll_date_picker = datePickerLayout.findViewById(R.id.scroller_date_picker);

        //checking for the min date value
        if (minDate != null) {
            scroll_date_picker.setMinDate(minDate.getTime());
        }
        //checking fot max date value
        if (maxDate != null) {
            scroll_date_picker.setMaxDate(maxDate.getTime());
        }

        //finding the textView to show selected date on date picker layout
        final TextView selected_date_tv = datePickerLayout.findViewById(R.id.show_selected_date);

        //finding cancel and done button
        TextView cancel = datePickerLayout.findViewById(R.id.cancel);
        TextView done = datePickerLayout.findViewById(R.id.done);

        //creting calendar variable to set the date on datePicker View
        final Calendar calendar = Calendar.getInstance();


        //setting current date to the calendar
        if (dateToSet != null) {
            calendar.setTime(dateToSet);
        }

        //creating date format to show current day of week
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        //date format to show current date
        SimpleDateFormat postFormater = new SimpleDateFormat("MMM dd, yyyy");

        //setting date to the textView on dialog layout
        selected_date_tv.setText(sdf.format(calendar.getTime()) + ", " + postFormater.format(calendar.getTime()));

        //initializing the datepicker and calling onDateChangeListener function on it
        scroll_date_picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE");
            SimpleDateFormat postFormater = new SimpleDateFormat("MMM dd, yyyy");

            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                //setting changed date to the calendar
                calendar.set(i, i1, i2);
                //setting date to the textView on dialog layout
                selected_date_tv.setText(sdf.format(calendar.getTime()) + ", " + postFormater.format(calendar.getTime()));
            }
        });


        //setting on clickListener to the done tetView
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setting date to the view
                showDateContainer.setText(toDateToShow(calendar));
                datePickerDialog.dismiss();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.dismiss();
            }
        });

        //setting view to the builder
        builder.setView(datePickerLayout);

        //creating builder for the alertDialog
        datePickerDialog = builder.create();
        //showing alertDilaog
        datePickerDialog.show();

    }

    //to convert string to date check for null
    public static String toDateToShow(Calendar date) {
        return new SimpleDateFormat(dateFormatToShow).format(date.getTime());
    }

    public static void showTimePicker(final Context mContext, final TextView timeContainer) {
        String h, m;

        //creating alertDialog builder for the alert the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        //creating layout inflator for inflating the custom datepicer layout
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        //inflating the layout
        View timePickerLayout = layoutInflater.inflate(R.layout.time_picker_dialog, null);
        //finding the date picker view
        TimePicker scroll_time_picker = timePickerLayout.findViewById(R.id.scroller_time_picker);
        scroll_time_picker.setIs24HourView(true);

        //finding the textView to show selected date on date picker layout
        final TextView selected_time_tv = timePickerLayout.findViewById(R.id.show_selected_time);

        //finding cancel and done button
        TextView cancel = timePickerLayout.findViewById(R.id.cancel);
        TextView done = timePickerLayout.findViewById(R.id.done);


        //creating calendar variable to set the date on datePicker View
        final Calendar calendar = Calendar.getInstance();
        h = checkForLessThanTen(calendar.get(Calendar.HOUR_OF_DAY));
        m = checkForLessThanTen(calendar.get(Calendar.MINUTE));

        selected_time_tv.setText(h + ":" + m);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll_time_picker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            scroll_time_picker.setMinute(calendar.get(Calendar.MINUTE));
        }

        scroll_time_picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            String h, m;

            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), i, i1);

                h = checkForLessThanTen(calendar.get(Calendar.HOUR_OF_DAY));
                m = checkForLessThanTen(calendar.get(Calendar.MINUTE));

                selected_time_tv.setText(h + ":" + m);
            }
        });


        //setting on clickListener to the done tetView
        done.setOnClickListener(new View.OnClickListener() {
            int h, m;

            @Override
            public void onClick(View view) {
                h = calendar.get(Calendar.HOUR_OF_DAY);
                m = calendar.get(Calendar.MINUTE);

                timeContainer.setText(checkForLessThanTen(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + checkForLessThanTen(calendar.get(Calendar.MINUTE)));
                timePickerDialog.dismiss();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.dismiss();
            }
        });

        builder.setView(timePickerLayout);
        timePickerDialog = builder.create();

        timePickerDialog.show();
    }

    private static String checkForLessThanTen(int value) {

        if (value >= 10)
            return String.valueOf(value);
        else {
            String converter = "0" + String.valueOf(value);
            return converter;
        }
    }

    public static String openFile(Context context, File file) {
        if (file == null) {
            ShowToast.showToast(context, "No file selected");
            return null;
        }
        Uri path = getUriType(context, file);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String url = file.getName(), mimeType;

        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            mimeType = "application/msword";
        } else if (url.contains(".pdf")) {
            // PDF file
            mimeType = "application/pdf";
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            mimeType = "application/vnd.ms-powerpoint";
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            mimeType = "application/vnd.ms-excel";
        } else if (url.contains(".zip") || url.contains(".rar")) {
            // WAV audio file
            mimeType = "application/x-wav";
        } else if (url.contains(".rtf")) {
            // RTF file
            mimeType = "application/rtf";
        } else if (url.contains(".wav") || url.contains(".mp3")) {
            // WAV audio file
            mimeType = "audio/x-wav";
        } else if (url.contains(".gif")) {
            // GIF file
            mimeType = "image/gif";
        } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            // JPG file
            mimeType = "image/jpeg";
        } else if (url.contains(".txt")) {
            // Text file
            mimeType = "text/plain";
        } else if (url.contains(".3gp") || url.contains(".mpg") ||
                url.contains(".mpeg") || url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")
                || url.contains(".mkv")) {
            // Video files
            mimeType = "video/*";
        } else {
            mimeType = "*/*";
        }

        intent.setDataAndType(path, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            ShowToast.showToast(context, "No app found to open this file");
        }
        return mimeType;
    }

    public static Uri getUriType(Context context, File file) {
        return FileProvider.getUriForFile(context,
                ApiClient.applicationId + ".provider",
                file);
    }


    private static MessageDialogFragment messageDialogFragment;

    public static void globalMessageDialog(final Context mContext, String message) {


        if (messageDialogFragment != null && messageDialogFragment.isVisible())
            messageDialogFragment.dismiss();

        messageDialogFragment = MessageDialogFragment.newInstance(message);
        messageDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "Message");
    }

}
