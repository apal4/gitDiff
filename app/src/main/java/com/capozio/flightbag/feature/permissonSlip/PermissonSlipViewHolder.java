package com.capozio.flightbag.feature.permissonSlip;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.capozio.flightbag.R;

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

 */

/**
    *  Represents a single entry in the list of homes where we have a signed permission waiver.
 */
public class PermissonSlipViewHolder extends RecyclerView.ViewHolder {
//    public LinearLayout layout;
    public Button viewpdf;
    public TextView name;
    public TextView date;
    public TextView address;
    public TextView citystate;
    public CardView cardView;

    public PermissonSlipViewHolder(View itemView) {
        super(itemView);

//        layout = (LinearLayout) itemView.findViewById(R.id.layout_showpdf);
        viewpdf = (Button) itemView.findViewById(R.id.button_viewpermisson);
        name = (TextView) itemView.findViewById(R.id.text_name);
        date = (TextView) itemView.findViewById(R.id.text_date);
        address = (TextView) itemView.findViewById(R.id.text_addr);
        citystate = (TextView) itemView.findViewById(R.id.text_citystate);
        cardView = (CardView) itemView.findViewById(R.id.cardview_permissonitem);
    }
}
