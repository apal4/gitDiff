package com.capozio.flightbag.feature.preflight;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.capozio.flightbag.Communication;
import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.Entry;
import com.capozio.flightbag.data.model.ItemViewHolder;

import java.util.Collections;
import java.util.List;

import static com.capozio.flightbag.util.Configs.TAG_CHECKLIST_EDIT;
import static com.capozio.flightbag.util.Configs.TAG_TEMPLATE_EDIT;
import static com.capozio.flightbag.util.KeyBoardUtil.hideKeyBoard;

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
 * Created by Ying Zhang on 8/29/16.
 *
 * RecyclerListAdapter for populating the checklist items either in the edit mode or read mode.
 */

/**
 *  RecyclerListAdapter for populating the checklist items either in the edit mode or read mode.
 *  Called by Android when it want to redisplay at list of checklist items.
 *  Associated with the RecyclerListView.
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> {//implements ItemTouchHelperAdapter{
    private List<Entry> list; // list of checklist items
    private boolean isEditMode; // whether currently in editmode or not
    private FragmentManager fm;
    private Communication cm; // handler for receiving and sending checklist data
    private Context context;
    private int severity;
    private int likelyhood;
    private int score;
    private AlertDialog riskDialog;
    //private ItemViewHolder holder;

    /**
     * Constructor for RecyclerListAdapter
     * <p>
     * Initialize the variables.
     *
     * @param isEditMode to determine whether the app is on edit mode.
     */

    public RecyclerListAdapter(FragmentManager fm, List<Entry> list, boolean isEditMode) {
        this.list = list;
        this.isEditMode = isEditMode;
        this.fm = fm;
    }

    public void setList(List<Entry> list) {
        this.list = list;
    }

    /**
     * set the appropriate layout id for current view layout: either edit mode or read mode.
     */
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext(); // get a handle of current context
        cm = (Communication) context; // get a handle of communication interface

        // inflate views based on whether in edit mode or read mode
        int mlayout = isEditMode ? R.layout.checklist_item_editmode : R.layout.checklist_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(mlayout, parent, false);

        // notify the ItemViewHolder which kind of items to render
        return new ItemViewHolder(view, isEditMode);
    }

    /**
     * viewHolder that used two different layouts: edit mode and read mode
     * manage how delete, edit, upArrow, and downArrow work.
     *
     * When the user clicked outside of the view, then the buttons for "up" and "down" disappears,
     * and the keyboard also disappears.
     *
     */
    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (isEditMode) { // we are in edit mode
            // define deleteButton behavior
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int pos = holder.getAdapterPosition();

                    // resolve the bug when tapping delete button too frequently
                    if (pos >= 0 && pos < list.size()) {
                        // remove the entry from the list
                        list.remove(pos);
                        // notify the change to ItemViewHolder to render the change
                        notifyItemRemoved(pos);
                        // clear focus to avoid some bug
                        View curView = ((Activity) context).getWindow().getCurrentFocus();
                        if (curView != null)
                            curView.clearFocus();
                    }
                }
            });

            // render the checklistmsg
            holder.editText.setText(list.get(position).getChecklistmsg());

            // When the user clicked outside of the view, then the buttons for "up" and "down" disappears,
            // and the keyboard also disappears.
            holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    PreFlightEditModeFragment frag = (PreFlightEditModeFragment) fm.
                            findFragmentByTag(cm.getEditMode() ? TAG_CHECKLIST_EDIT : TAG_TEMPLATE_EDIT);
                    final ImageButton upArrow = (ImageButton) frag.getActivity().findViewById(R.id.imageButton_moveup);
                    final ImageButton downArrow = (ImageButton) frag.getActivity().findViewById(R.id.imageButton_movedown);
                    upArrow.setVisibility(View.VISIBLE);
                    downArrow.setVisibility(View.VISIBLE);
                    upArrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideKeyBoard(view);
                            // get the current position of the focused entry
                            int pos = holder.getAdapterPosition();

                            // swap the two entries
                            Collections.swap(list, pos, pos - 1);
                            notifyItemMoved(pos, pos - 1);
                            // if at the top, the "up" button is disabled.
                            if (pos == 1) {
                                upArrow.setEnabled(false);
                            } else if (pos == list.size() - 1) {
                                // if it is located second position from the top and "up" button is pressed,
                                // then the "up" button  is disabled.
                                downArrow.setEnabled(true);
                            }
                        }
                    });

                    downArrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideKeyBoard(view);
                            int pos = holder.getAdapterPosition();

                            // when the user clicked "down" button, then swap the checklist.
                            Collections.swap(list, pos, pos + 1);
                            notifyItemMoved(pos, pos + 1);
                            // if it is located second position from the bottom and "down" button is pressed,
                            // then the "down" button is disabled.
                            if (pos == list.size() - 2)
                                downArrow.setEnabled(false);

                            else if (pos == 0) // if it is located at most bottom, the "down" button  is disabled.
                                upArrow.setEnabled(true);
                        }
                    });

                    int pos = holder.getAdapterPosition();
                    if (!b) { // out of focus
                        // hide keyboard and the arrows
                        hideKeyBoard(view);
                        upArrow.setVisibility(View.INVISIBLE);
                        downArrow.setVisibility(View.INVISIBLE);

                        // update the checklist msg
                        if (pos >= 0 && pos < list.size())
                            list.get(pos).setChecklistmsg(holder.editText.getText().toString());
                    } else { // on focus
                        // if at the top, disable "up" arrow
                        if (pos == 0)
                            upArrow.setEnabled(false);
                        else
                            upArrow.setEnabled(true);

                        // if at bottom, disable "down" arrow
                        if (pos == list.size() - 1)
                            downArrow.setEnabled(false);
                        else
                            downArrow.setEnabled(true);

                    }
                }
            });
        } else {
            // in read mode (PreflightFragment), simply render all the elements
            holder.checklistmsg.setText(list.get(position).getChecklistmsg());
            holder.checkBox.setChecked(list.get(position).getisChecked());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    list.get(holder.getAdapterPosition()).setChecked(compoundButton.isChecked());
                }
            });
        }

        // notesButton appears in both modes
        holder.notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Entry currentEntry = list.get(holder.getAdapterPosition());
                severity = currentEntry.getSeverity();
                likelyhood = currentEntry.getLikelyhood();
                score = currentEntry.getRisk_score();
                final View notesview = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null);
                final EditText editText = (EditText)notesview.findViewById(R.id.editText);
                TextView titleView = (TextView) notesview.findViewById(R.id.note_title);
                titleView.setText(currentEntry.getChecklistmsg());
                editText.setText(currentEntry.getNotes());

                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus) {
                            hideKeyBoard(notesview);
                        }
                    }
                });

                // handle "risk assessment" related behavior
//                final RadioGroup severityGroup = (RadioGroup)notesview.findViewById(R.id.radiogroup_severity);
//                final RadioGroup likelyGroup = (RadioGroup)notesview.findViewById(R.id.radiogroup_likelyhood);
//                ((RadioButton) severityGroup.getChildAt(severity-1)).setChecked(true);
//                ((RadioButton) likelyGroup.getChildAt(likelyhood-1)).setChecked(true);
                final TextView riskAssessment = (TextView) notesview.findViewById(R.id.text_risk_assessment_result);

                updateRiskAssessment(riskAssessment, view.getContext());


                // create the note dialog
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setView(notesview)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Entry tobeSavedEntry = list.get(holder.getAdapterPosition());
                                tobeSavedEntry.setNotes(editText.getText().toString());
//                                tobeSavedEntry.setRiskAssessement(severity, likelyhood);
                                hideKeyBoard(notesview);
                                dialog.dismiss();

                                View riskview = LayoutInflater.from(context).inflate(R.layout.dialog_risk_assessment,null);
                                Rect displayRect = new Rect();
//                                View riskTable = ((AppCompatActivity) context).findViewById(R.id.tableLayout_risk);
                                ((AppCompatActivity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
                                // inflate and adjust layout to take up 90% of the width of the screen

                                riskview.setMinimumWidth((int) (displayRect.width() * 0.95));

                                riskDialog = new AlertDialog.Builder(context)
                                        .setCancelable(false)
                                        .setView(riskview)
                                        .create();

                                setupRiskButtonClickListeners(riskview, riskDialog, tobeSavedEntry);

                                riskDialog.show();



                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideKeyBoard(notesview);
                                dialog.dismiss();
                            }
                        }).create().show();


            }
        });
    }

    private void updateRiskAssessment(TextView riskAssessment, Context context) {
        if(score < 5) {
            riskAssessment.setText("Acceptable");
            riskAssessment.setTextColor(ContextCompat.getColor(context, R.color.risk_green));
        } else if(score < 13) {
            riskAssessment.setText("Acceptable with Mitigation");
            riskAssessment.setTextColor(ContextCompat.getColor(context, R.color.risk_dark_yellow));
        } else {
            riskAssessment.setText("Unacceptable");
            riskAssessment.setTextColor(ContextCompat.getColor(context, R.color.risk_red));
        }
    }


    public void clearNotesAndCheckBoxes() {
        for (Entry entry: list) {
            entry.setNotes("");
            entry.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    private void setupRiskButtonClickListeners(View riskview,final AlertDialog riskDialog, final Entry entry) {
        riskview.findViewById(R.id.textView11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(1,1);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(1,2);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView13).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(1,3);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView14).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(1,4);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView15).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(1,5);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView21).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(2,1);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView22).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(2,2);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView23).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(2,3);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView24).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(2,4);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView25).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(2,5);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView31).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(3,1);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView32).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(3,2);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView33).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(3,3);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView34).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(3,4);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView35).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(3,5);
                riskDialog.dismiss();
            }
        });


        riskview.findViewById(R.id.textView41).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(4,1);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView42).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(4,2);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView43).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(4,3);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView44).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(4,4);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView45).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(4,5);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView51).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(5,1);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView52).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(5,2);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView53).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(5,3);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView54).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(5,4);
                riskDialog.dismiss();
            }
        });

        riskview.findViewById(R.id.textView55).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.setRiskAssessement(5,5);
                riskDialog.dismiss();
            }
        });

    }


//    public void onItemAdd() {
//        notifyItemInserted(list.size() - 1);
//    }


}
