package antidose.antidose;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class confirmHelpFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ConfirmHelpListener {
        public void onDialogPositiveClickHelp(DialogFragment dialog);
        public void onDialogNegativeClickHelp(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    ConfirmHelpListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConfirmHelpListener) {
            mListener = (ConfirmHelpListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement confirmHelpDialogListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setMessage(R.string.confirm_help)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClickHelp(confirmHelpFragment.this);
                    }
                })
                .setNegativeButton("Not Yet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClickHelp(confirmHelpFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
