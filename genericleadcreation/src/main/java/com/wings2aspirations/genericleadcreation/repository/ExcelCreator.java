package com.wings2aspirations.genericleadcreation.repository;

import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;


import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class ExcelCreator {
    public interface ExcelCallBack {
        void excelCreated(boolean hasExcelBeenCreated, String filePath);
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    public static void createExcel(final String[][] data, final String fileName, final Context context,
                                   final boolean shouldShare, final ExcelCallBack callBack) {
        new AsyncTask<Void, Void, Void>() {
            private boolean hasExcelBeenCreated = false;
            private String filePath;

            @Override
            protected Void doInBackground(Void... voids) {
                String filePath = Environment.getExternalStorageDirectory() + File.separator + (shouldShare ? "LeadShare" : "LeadDownload");
                File file = new File(filePath);
                if (!file.exists())
                    file.mkdir();
                filePath = filePath + File.separator + fileName + ".xls";
                this.filePath = filePath;
                try {
                    File myFile = new File(filePath);
                    HSSFWorkbook myWorkBook = new HSSFWorkbook();

                    //Return first sheet from the XLSX workbook
                    myWorkBook.createSheet();
                    HSSFSheet mySheet = myWorkBook.getSheetAt(0);
                    //Get iterator to all the rows in current sheet

                    for (int i = 0; i < data.length; i++) {
                        // Creating a new Row in existing XLSX sheet
                        HSSFRow row = mySheet.createRow(i);
                        int cellnum = 0;
                        for (int j = 0; j < data[i].length; j++) {
                            HSSFCell cell = row.createCell(cellnum++);
                            try {
                                cell.setCellValue(Double.parseDouble(data[i][j]));
                            } catch (Exception e) {
                                cell.setCellValue(data[i][j]);
                            }
                        }
                    }

                    // open an OutputStream to save written data into XLSX file
                    FileOutputStream os = new FileOutputStream(myFile);
                    myWorkBook.write(os);

                    if (myFile.exists()) {
                        if (shouldShare) {
                            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                            intentShareFile.setType("application/vnd.ms-excel");
                            intentShareFile.putExtra(Intent.EXTRA_STREAM, Utility.getUriType(context, myFile));
                            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Lead Report");
                            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Lead Report");
                            if (intentShareFile.resolveActivity(context.getPackageManager()) != null)
                                context.startActivity(Intent.createChooser(intentShareFile, "SHARE via"));
                        } else {

                        }
                    }
                    hasExcelBeenCreated = true;
                } catch (Exception e) {
                    hasExcelBeenCreated = false;
                } catch (Error e) {
                    hasExcelBeenCreated = false;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                callBack.excelCreated(hasExcelBeenCreated, filePath);
            }
        }.execute();
    }
}