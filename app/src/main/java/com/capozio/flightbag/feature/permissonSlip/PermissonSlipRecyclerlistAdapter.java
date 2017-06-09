package com.capozio.flightbag.feature.permissonSlip;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.PDFWrapper;
import com.capozio.flightbag.data.model.PermissonWrapper;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*** ***********************************************************************
 * <p>
 * Pilot Training System CONFIDENTIAL
 * __________________
 * <p>
 * [2015] - [2017] Pilot Training System
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of Pilot Training System,
 * The intellectual and technical concepts contained
 * herein are proprietary to Pilot Training System
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Pilot Training System.
 *
 * Created by Ying Zhang on 11/8/16.
 *
 *
 */

/**
 * This is for the SearchNearby screen, which is a  list of names  addresses where we have
 * signed permission waivers.
 * <p>
 * Some Checklists require signed permission slips, and some don't.
 */
public class PermissonSlipRecyclerlistAdapter extends RecyclerView.Adapter<PermissonSlipViewHolder> {//implements ItemTouchHelperAdapter{
    private Context context;
    //    private List<PDFWrapper> metalist;
    //    private List<String> pdfs;
    private List<PermissonWrapper> metalist;
    private PDFWrapper curlist;
    private EditText addressEdit;
    private EditText radiusEdit;

    /**
     * Constructor for PermissonSlipRecyclerlistAdapter
     *
     */
    public PermissonSlipRecyclerlistAdapter(List<PermissonWrapper> metalist, EditText addressEdit, EditText radiusEdit) {
        this.metalist = metalist;
        this.addressEdit = addressEdit;
        this.radiusEdit = radiusEdit;
    }


    /**
     *  inflate layout.
     */
    @Override
    public PermissonSlipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext(); // get a handle of current context

        // inflate views based on whether in edit mode or read mode

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_permisson_item, parent, false);

        // notify the PermissonSlipViewHolder which kind of items to render
        return new PermissonSlipViewHolder(view);
    }

    /**
     * Controls how the name, address, etc appears on the screen.
     */
    @Override
    public void onBindViewHolder(final PermissonSlipViewHolder holder, final int position) {
 //        curlist = metalist.get(position).getMetaData();
        int pos = holder.getAdapterPosition();
        holder.name.setText(metalist.get(pos).getMetaData().getResident());

        String date = metalist.get(pos).getMetaData().getLocalTimestamp();
        if(date != null) {
            Matcher matcher = Pattern.compile("(\\d+)-(\\d+)-(\\d+)").matcher(date);
            String formattedDate = "";
            while (matcher.find()) {
                formattedDate = matcher.group(2) + "/" + matcher.group(3) + "/" + matcher.group(1);
            }
            holder.date.setText(formattedDate);
        }
        holder.address.setText(metalist.get(pos).getMetaData().getStreetAddress());
        holder.citystate.setText(metalist.get(pos).getMetaData().getCity()+" "+metalist.get(position).getMetaData().getState());

        // open an activity to view the pdf
        holder.viewpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pdfFile = new File(metalist.get(holder.getAdapterPosition()).getPdfPath());
                Intent openpdfIntent = new Intent(Intent.ACTION_VIEW);

                openpdfIntent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
                context.startActivity(openpdfIntent);
            }
        });

        // when clicking on one of the permisson cardview,
        // it will automatically poplulate the address text with the selected permisson address and clear the radius text
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curlist = metalist.get(holder.getAdapterPosition()).getMetaData();
                addressEdit.setText(curlist.getStreetAddress());
                radiusEdit.setText("");
            }
        });
    }


    public PDFWrapper getCurrentlist() {
        return curlist;
    }

    @Override
    public int getItemCount() {
        return metalist.size();
    }

}
