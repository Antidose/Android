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


public class CancelRequestFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CancelRequestListener {
        public void onDialogPositiveClickCancelRequest(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    CancelRequestListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CancelRequestListener) {
            mListener = (CancelRequestListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement confirmHelpDialogListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setMessage(R.string.cancel_request)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClickCancelRequest(CancelRequestFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
