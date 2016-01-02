package edu.bluejack151.gafp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ThemeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ThemeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThemeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Firebase firebase;

    private OnFragmentInteractionListener mListener;

    public ThemeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ThemeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ThemeFragment newInstance(String param1, String param2) {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        firebase.setAndroidContext(getActivity().getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        final Vector<Integer> userMoney = new Vector<Integer>();
        final Vector<String> userOwnedTheme = new Vector<String>();

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Toast.makeText(getActivity().getApplicationContext(), dataSnapshot.getChildrenCount() + "", Toast.LENGTH_LONG).show();

                userMoney.add(Integer.parseInt(dataSnapshot.child("money").getValue().toString()));
                DataSnapshot ownedTheme = dataSnapshot.child("themes");
                for (final DataSnapshot theme : ownedTheme.getChildren()) {
                    userOwnedTheme.add(theme.getKey().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        firebase.child("shops/themes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Toast.makeText(getActivity().getApplicationContext(),dataSnapshot.getChildrenCount()+"",Toast.LENGTH_LONG).show();
                for (final DataSnapshot theme : dataSnapshot.getChildren()) {
                    View layoutTheme = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.theme_layout, null);
                    TextView thTitle = (TextView) layoutTheme.findViewById(R.id.themeTitle);
                    TextView thPrice = (TextView) layoutTheme.findViewById(R.id.themePrice);
                    ImageView thImage = (ImageView) layoutTheme.findViewById(R.id.themeImageView);

                    final String title = theme.child("name").getValue().toString();
                    final String price = theme.child("price").getValue().toString();
                    final String imageName = theme.getKey().toString();

                    int avaID = getResources().getIdentifier(imageName, "drawable", getActivity().getPackageName());
                    thImage.setImageResource(avaID);
                    thPrice.setText(price);
                    thTitle.setText(title);

                    Button btnPurchase = (Button) layoutTheme.findViewById(R.id.btnPurchaseTheme);
                    btnPurchase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                       //     Toast.makeText(getActivity().getApplicationContext(), "Purchase " + v.getId(), Toast.LENGTH_LONG).show();

                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View alertPurchaseLayout = inflater.inflate(R.layout.popup_confirmation, null);
                            //Button purchase = (Button) alertPurchaseLayout.findViewById(R.id.btnPopUpPurchase);
                            // Button cancel = (Button) alertPurchaseLayout.findViewById(R.id.btnPopUpCancel);

                            final AlertDialog.Builder alertPurchase = new AlertDialog.Builder(getActivity().getApplicationContext());
                            alertPurchase.setView(alertPurchaseLayout);

                            int cancelBtnID = getResources().getIdentifier("btnPopUpCancel", "id", getActivity().getPackageName());
                            int purchaseBtnID = getResources().getIdentifier("btnPopUpPurchase", "id", getActivity().getPackageName());
                            alertPurchase.setNegativeButton(cancelBtnID, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Purchase canceled", Toast.LENGTH_SHORT).show();
                                }
                            });

                            alertPurchase.setPositiveButton(purchaseBtnID, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Integer money = userMoney.get(0);
                                    Integer purchasePrice = Integer.parseInt(price);
                                    if (money > purchasePrice) {
                                        money -= purchasePrice;
                                        Map<String, Object> updateMoney = new HashMap<String, Object>();
                                        updateMoney.put("money", money);
                                        firebase.child("users/" + firebase.getAuth().getUid()).updateChildren(updateMoney);

                                        //add
                                        Map<String, Object> addNewTheme = new HashMap<String, Object>();
                                        addNewTheme.put(imageName,true);
                                        firebase.child("users/" + firebase.getAuth().getUid()+"/themes").updateChildren(addNewTheme);

                                        Toast.makeText(getActivity().getApplicationContext(), "Thank you for purchasing", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(), "More money is required", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            alertPurchase.setTitle("Purchase Item");
                            AlertDialog dialog = alertPurchase.create();
                            dialog.show();
                        }
                    });

                    if (userOwnedTheme.contains(imageName)) {
                        btnPurchase.setText("PURCHASED");
                        btnPurchase.setEnabled(false);
                        btnPurchase.setBackgroundColor(Color.GRAY);
                    } else {

                    }


                    ((LinearLayout) getActivity().findViewById(R.id.themeLinearLayout)).addView(layoutTheme);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_theme, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
